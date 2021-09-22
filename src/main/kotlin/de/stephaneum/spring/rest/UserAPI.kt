package de.stephaneum.spring.rest

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.*
import de.stephaneum.spring.rest.dto.Request
import de.stephaneum.spring.rest.dto.Response
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.scheduler.Element
import de.stephaneum.spring.security.CryptoService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.*

private const val emailSuffix = "@stephaneum.de"

@RestController
@RequestMapping("/api/users")
class UserAPI (
        private val cryptoService: CryptoService,
        private val fileService: FileService,
        private val groupService: GroupService,
        private val codeService: CodeService,
        private val classService: ClassService,
        private val configScheduler: ConfigScheduler,
        private val userRepo: UserRepo,
        private val postRepo: PostRepo,
        private val userGroupRepo: UserGroupRepo,
        private val codeRepo: CodeRepo,
        private val schoolClassRepo: SchoolClassRepo
) {

    @ExperimentalStdlibApi
    @ExperimentalUnsignedTypes
    @PostMapping("/batch/password")
    fun updatePassword(@RequestBody request: Request.ChangePasswordBatch) {
        Session.getUser(adminOnly = true)

        val users = userRepo.findByCodeRole(request.role)
        users.forEach { user -> user.password = cryptoService.hashPassword(request.password) }
        userRepo.saveAll(users)
    }

    @ExperimentalUnsignedTypes
    @PostMapping("/batch/delete")
    fun deleteUsers(@RequestBody request: Request.DeleteByRole) {
        val me = Session.getUser(adminOnly = true)

        if(!cryptoService.checkPassword(request.password, me.password))
            throw ErrorCode(403, "wrong password")

        val users = userRepo.findByCodeRole(request.role)
        users.forEach { user ->
            deleteUser(initiator = me, user = user)
        }
    }

    @PostMapping("/batch/quotas")
    fun updateQuotas(@RequestBody request: Request.UpdateQuotasBatch) {
        Session.getUser(adminOnly = true)

        val users = userRepo.findBySchoolClassGrade(request.grade)
        users.forEach { user -> user.storage = request.storage }
        userRepo.saveAll(users)
    }

    @ExperimentalUnsignedTypes
    @PostMapping("/batch/delete-storage")
    fun clearStorage(@RequestBody request: Request.DeleteByRole) {
        val me = Session.getUser(adminOnly = true)

        if(!cryptoService.checkPassword(request.password, me.password))
            throw ErrorCode(403, "wrong password")

        val users = userRepo.findByCodeRole(request.role)
        users.forEach { user ->
            fileService.clearStorage(user)
        }
    }

    @GetMapping("/{id}")
    fun getUserInfo(@PathVariable id: Int): Response.UserInfo {
        Session.getUser(adminOnly = true)

        val user = userRepo.findByIdOrNull(id) ?: throw ErrorCode(404, "user not found")
        return Response.UserInfo(user.firstName, user.lastName, user.email, user.code.role, user.storage, !user.banned, user.managePlans)
    }

    @ExperimentalUnsignedTypes
    @ExperimentalStdlibApi
    @PostMapping("/update")
    fun updateUser(@RequestBody request: Request.UpdateUser) {
        Session.getUser(adminOnly = true)
        val user = userRepo.findByIdOrNull(request.user) ?: throw ErrorCode(404, "user not found")
        user.firstName = request.firstName
        user.lastName = request.lastName
        user.email = request.email
        if(!request.password.isNullOrBlank())
            user.password = cryptoService.hashPassword(request.password)
        user.storage = request.storage
        user.banned = !request.permissionLogin
        user.managePlans = request.permissionPlan
        userRepo.save(user)
    }

    @PostMapping("/change-account/{id}")
    fun changeAccount(@PathVariable id: Int) {
        Session.getUser(adminOnly = true)

        val user = userRepo.findByIdOrNull(id) ?: throw ErrorCode(404, "user not found")
        Session.get().user = user
    }

    @ExperimentalUnsignedTypes
    @ExperimentalStdlibApi
    @PostMapping("/import")
    fun importUsers(@RequestBody request: Request.ImportUsers) {
        Session.getUser(adminOnly = true)

        // parse user data
        val newUsers = parseData(request)

        // validate email
        val allUsers = userRepo.findAll().toMutableList()
        allUsers.addAll(newUsers)
        if (allUsers.distinctBy { it.email }.size != allUsers.size)
            throw ErrorCode(409, "duplicate emails")

        // generate missing school classes
        val classes = schoolClassRepo.findAll()
        newUsers.filter { user -> user.schoolClass != null }
                .forEach { user ->
                    val currClass = user.schoolClass!!
                    val actualClass = classes.firstOrNull { c -> currClass.grade == c.grade && currClass.suffix == c.suffix } ?: schoolClassRepo.save(currClass)
                    user.schoolClass = actualClass
                }

        // generate missing codes
        val codes = codeRepo.findByUsedAndRole(false, request.role).toMutableList()
        if (newUsers.size > codes.size) {
            val missing = newUsers.size - codes.size
            val newCodes = codeService.generateCodes(request.role, missing)
            codes.addAll(newCodes)
        }

        val storage = when (request.role) {
            ROLE_STUDENT -> configScheduler.get(Element.storageStudent)?.toIntOrNull() ?: 0
            else -> configScheduler.get(Element.storageTeacher)?.toIntOrNull() ?: 0
        }

        // assign values
        newUsers.forEachIndexed { index, user ->
            codes[index].used = true // code is now used
            user.code = codes[index] // assign codes
            user.storage = storage // assign storage
            user.password = cryptoService.hashPassword(user.password) // hash passwords
        }

        // register
        codeRepo.saveAll(codes)
        userRepo.saveAll(newUsers)
    }

    private fun deleteUser(initiator: User, user: User) {
        // posts
        postRepo.deleteByUserAndApproved(user, false)
        val posts = postRepo.findByUserAndApproved(user, true)
        posts.forEach { post -> post.user = initiator } // set author to admin
        postRepo.saveAll(posts)

        // files
        fileService.clearStorage(user)

        // groups
        val groupConnections = userGroupRepo.findByUserAndGroupParent(user, null)
        groupConnections.forEach { connection ->
            if (connection.teacher || connection.group.leader.id == user.id) {
                // delete the whole group
                groupService.deleteGroupAndChildren(user, connection.group)
            } else {
                // just leave
                groupService.removeUserFromGroupRecursive(connection)
            }
        }

        // delete code, this will also delete the user
        codeRepo.deleteByCode(user.code.code)
    }

    /**
     * generates user objects from data
     * - first name
     * - last name
     * - email
     * - password (not hashed)
     * - school class (without id)
     *
     * needed to be filled:
     * - id of school class
     * - storage
     * - permissions
     *
     * needed to be checked:
     * - distinct emails
     */
    private fun parseData(import: Request.ImportUsers): List<User> {
        val raw = import.data
                .lines()
                .filter { row -> !row.isBlank() }
                .map { row -> row.trim().split(import.separator) }

        val emailMap: MutableMap<String, Int> = mutableMapOf()

        return when (import.format) {
            0 -> {
                // firstName - lastName - emailPrefix - password - class
                if (raw.any { row -> row.size != 5 })
                    throw ErrorCode(410, "syntax error")

                if (raw.any { row -> row[3].isBlank() })
                    throw ErrorCode(417, "missing password")

                raw.map { row ->
                    val email = row[2].formatEmail() + emailSuffix
                    User(firstName = row[0], lastName = row[1], email = digestEmail(email, emailMap), password = row[3], schoolClass = classService.parse(row[4]))
                }
            }
            1 -> {
                // emailPrefix - salutation - lastName - firstName (no student)
                if (import.role == ROLE_STUDENT)
                    throw ErrorCode(412, "no student")

                if (import.password.isNullOrBlank())
                    throw ErrorCode(417, "missing password")

                if (raw.any { row -> row.size != 4 })
                    throw ErrorCode(410, "syntax error")

                raw.map { row ->
                    val firstName = row[3]
                    val lastName = row[2]
                    val gender = when (row[1]) {
                        "Herr" -> SEX_MALE
                        "Frau" -> SEX_FEMALE
                        else -> SEX_UNKNOWN
                    }
                    val email = row[0].formatEmail() + emailSuffix
                    User(firstName = firstName, lastName = lastName, email = digestEmail(email, emailMap), password = import.password, gender = gender)
                }
            }
            2 -> {
                // class - lastName - firstName
                if (import.password.isNullOrBlank())
                    throw ErrorCode(417, "missing password")

                if (raw.any { row -> row.size != 3 })
                    throw ErrorCode(410, "syntax error")

                raw.map { row ->
                    val firstName = row[2]
                    val lastName = row[1]
                    val email = firstName.take(1).formatEmail() + "." + lastName.formatEmail() + emailSuffix
                    User(firstName = firstName, lastName = lastName, email = digestEmail(email, emailMap), password = import.password, schoolClass = classService.parse(row[0]))
                }
            }
            else -> throw ErrorCode(400, "invalid format id")
        }
    }

    /**
     * appends to the email a number if it already exists
     */
    private fun digestEmail(email: String, emailMap: MutableMap<String, Int>): String {
        val count = emailMap[email]
        if(count != null) {
            emailMap[email] = count + 1
            val split = email.split("@")
            return split[0] + count + emailSuffix
        } else {
            emailMap[email] = 1
            return email
        }
    }

    /**
     * make the string suitable for using in email
     * removes special characters and spaces and keeps the string in lowercase
     */
    private fun String.formatEmail(): String {
        return this
            .replace(" ", "")
            .lowercase()
            .replace("ä", "ae")
            .replace("ö", "oe")
            .replace("ü", "ue")
            .replace("ß", "ss")
    }
}