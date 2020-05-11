package de.stephaneum.spring.rest

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.helper.GroupService
import de.stephaneum.spring.rest.objects.Request
import de.stephaneum.spring.security.CryptoService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserAPI (
        private val cryptoService: CryptoService,
        private val fileService: FileService,
        private val groupService: GroupService,
        private val userRepo: UserRepo,
        private val postRepo: PostRepo,
        private val userGroupRepo: UserGroupRepo,
        private val codeRepo: CodeRepo
) {

    @ExperimentalUnsignedTypes
    @PostMapping("/password")
    fun updatePassword(@RequestBody request: Request.ChangePasswordBatch) {
        Session.getUser(adminOnly = true)

        val users = userRepo.findByCodeRole(request.role)
        users.forEach { user -> user.password = cryptoService.hashPassword(request.password) }
        userRepo.saveAll(users)
    }

    @PostMapping("/delete/batch")
    fun deleteUsers(@RequestBody request: Request.Role) {
        val me = Session.getUser(adminOnly = true)

        val users = userRepo.findByCodeRole(request.role)
        users.forEach { user ->
            deleteUser(initiator = me, user = user)
        }
    }

    @PostMapping("/quotas/batch")
    fun updateQuotas(@RequestBody request: Request.UpdateQuotasBatch) {
        Session.getUser(adminOnly = true)

        val users = userRepo.findBySchoolClassGrade(request.grade)
        users.forEach { user -> user.storage = request.storage }
        userRepo.saveAll(users)
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

        // delete code this will also delete the user
        codeRepo.deleteByCode(user.code.code)
    }
}