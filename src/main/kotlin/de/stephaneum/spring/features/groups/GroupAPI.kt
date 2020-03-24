package de.stephaneum.spring.features.groups

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/groups")
class GroupAPI {

    @Autowired
    private lateinit var userGroupRepo: UserGroupRepo

    @Autowired
    private lateinit var groupRepo: GroupRepo

    @Autowired
    private lateinit var userRepo: UserRepo

    @Autowired
    private lateinit var fileRepo: FileRepo

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var logService: LogService

    @GetMapping
    fun getMyProjects(): List<GroupInfo> {
        val user = Session.get().user ?: throw ErrorCode(401, "login")
        return userGroupRepo.findByUserOrderByGroupName(user).map { it.group.toGroupInfo() }
    }

    @GetMapping("/all")
    fun getAllProjects(): List<GroupInfo> {
        val user = Session.get().user ?: throw ErrorCode(401, "login")
        if(user.code.role != ROLE_ADMIN)
            throw ErrorCode(403, "Admin only")

        return groupRepo.findByOrderByName().map { it.toGroupInfo() }
    }

    @GetMapping("/{id}")
    fun getProject(@PathVariable id: Int): GroupInfoDetailed {
        val user = Session.get().user ?: throw ErrorCode(401, "login")

        val group: Group
        if (user.code.role == ROLE_ADMIN) {
            group = groupRepo.findByIdOrNull(id) ?: throw ErrorCode(404, "not found")
        } else {
            group = userGroupRepo.findByUserAndGroup(user, id.obj())?.group ?: throw ErrorCode(403, "forbidden")
        }
        val membersRaw = userGroupRepo.findByGroupOrderByUserFirstNameAscUserLastNameAsc(group)
        val members = membersRaw.map { GroupUser(it.user.id, it.user.firstName, it.user.lastName, it.teacher, it.chat) }
        return GroupInfoDetailed(group.id, group.name, group.leader.toSimpleUser(), group.accepted, group.chat, members)
    }

    @PostMapping("/create")
    fun create(@RequestBody request: CreateGroup) {
        val user = Session.get().user ?: throw ErrorCode(401, "login")

        if(request.name.isNullOrBlank())
            throw ErrorCode(400, "missing name")

        val teachers: List<User>
        if(user.code.role == ROLE_STUDENT) {
            if(request.teachers.isNullOrEmpty())
                throw ErrorCode(400, "missing teachers")

            teachers = userRepo.findByIdIn(request.teachers)

            if(teachers.size != request.teachers.size)
                throw ErrorCode(404, "at least one teacher not found")

            if(teachers.any { it.code.role == ROLE_STUDENT })
                throw ErrorCode(409, "at least one user is not teacher")
        } else {
            teachers = emptyList()
        }

        // actual group creation
        val group = groupRepo.save(Group(0, request.name.trim(), user, user.code.role != ROLE_STUDENT, true, false))

        // add connections
        val connections = mutableListOf<UserGroup>()
        connections.add(UserGroup(0, user, group, false, true))
        connections.addAll(teachers.map { UserGroup(0, it, group, true, false) })
        userGroupRepo.saveAll(connections)

        logService.log(EventType.CREATE_GROUP, user, group.name)
        logService.log(EventType.JOIN_GROUP, connections.map { it.user }, group.name)
    }

    @PostMapping("/{id}/chat/{chat}")
    fun updateChat(@PathVariable id: Int, @PathVariable chat: Int) {
        val user = Session.get().user ?: throw ErrorCode(401, "login")

        val group: Group
        if (user.code.role == ROLE_ADMIN) {
            group = groupRepo.findByIdOrNull(id) ?: throw ErrorCode(404, "not found")
        } else {
            group = userGroupRepo.findByUserAndGroup(user, id.obj())?.group ?: throw ErrorCode(403, "forbidden")
        }

        group.chat = chat == 1
        groupRepo.save(group)
    }

    @PostMapping("/{id}/delete")
    fun delete(@PathVariable id: Int) {
        val user = Session.get().user ?: throw ErrorCode(401, "login")

        if(!checkAdminPermission(user, id))
            throw ErrorCode(403, "you are not teacher or (group) admin")

        val group = groupRepo.findByIdOrNull(id) ?: throw ErrorCode(404, "not found")

        val files = fileRepo.findByGroup(group)
        files.forEach { fileService.deleteFileStephaneum(user, it) } // delete all files

        groupRepo.delete(group) // folder will be deleted also
        logService.log(EventType.DELETE_GROUP, user, group.name)
    }

    @PostMapping("/{id}/accept")
    fun accept(@PathVariable id: Int) {
        val user = Session.get().user ?: throw ErrorCode(401, "login")
        val connection = userGroupRepo.findByUserAndGroup(user, id.obj()) ?: throw ErrorCode(404, "no user-group connection found")
        if(!connection.teacher)
            throw ErrorCode(403, "you are not teacher")

        connection.group.accepted = true
        groupRepo.save(connection.group)
        logService.log(EventType.APPROVE_GROUP, user, connection.group.name)
    }

    @PostMapping("/{id}/reject")
    fun reject(@PathVariable id: Int) {
        val user = Session.get().user ?: throw ErrorCode(401, "login")
        val connection = userGroupRepo.findByUserAndGroup(user, id.obj()) ?: throw ErrorCode(404, "no user-group connection found")
        if(!connection.teacher)
            throw ErrorCode(403, "you are not teacher")

        groupRepo.delete(connection.group)
        logService.log(EventType.REJECT_GROUP, user, connection.group.name)
    }

    @PostMapping("/{groupID}/add-user/{userID}")
    fun addUser(@PathVariable groupID: Int, @PathVariable userID: Int) {
        val user = Session.get().user ?: throw ErrorCode(401, "login")

        if(!checkAdminPermission(user, groupID))
            throw ErrorCode(403, "you are not teacher or (group) admin")

        val targetUser = userRepo.findByIdOrNull(userID) ?: throw ErrorCode(404, "user not found")
        val group = groupRepo.findByIdOrNull(groupID) ?: throw ErrorCode(404, "group not found")

        if(userGroupRepo.existsByUserAndGroup(targetUser, group))
            throw ErrorCode(400, "target user-group already exists")

        userGroupRepo.save(UserGroup(0, targetUser, group, false, true, true))
        logService.log(EventType.JOIN_GROUP, targetUser, group.name)
    }

    @PostMapping("/{groupID}/toggle-chat/{userID}")
    fun toggleChatUser(@PathVariable groupID: Int, @PathVariable userID: Int) {
        val user = Session.get().user ?: throw ErrorCode(401, "login")
        if(!checkAdminPermission(user, groupID))
            throw ErrorCode(403, "you are not teacher or (group) admin")

        val targetConnection = userGroupRepo.findByUserAndGroup(userID.obj(), groupID.obj()) ?: throw ErrorCode(404, "no target user-group connection found")
        if(targetConnection.hasAdminPermissions())
            throw ErrorCode(403, "this user has admin rights")
        targetConnection.chat = !targetConnection.chat
        userGroupRepo.save(targetConnection)
    }

    @PostMapping("/{groupID}/kick/{userID}")
    fun kickUser(@PathVariable groupID: Int, @PathVariable userID: Int) {
        val user = Session.get().user ?: throw ErrorCode(401, "login")
        if(!checkAdminPermission(user, groupID))
            throw ErrorCode(403, "you are not teacher or (group) admin")

        val targetConnection = userGroupRepo.findByUserAndGroup(userID.obj(), groupID.obj()) ?: throw ErrorCode(404, "no target user-group connection found")
        if(targetConnection.hasAdminPermissions())
            throw ErrorCode(403, "this user has admin rights")

        removeUserFromGroup(targetConnection)
        logService.log(EventType.QUIT_GROUP, targetConnection.user, targetConnection.group.name)
    }

    @PostMapping("/{groupID}/leave")
    fun leave(@PathVariable groupID: Int) {
        val user = Session.get().user ?: throw ErrorCode(401, "login")

        val connection = userGroupRepo.findByUserAndGroup(user, groupID.obj()) ?: throw ErrorCode(404, "you are not member of this group")
        if(user.id == connection.group.id || connection.teacher)
            throw ErrorCode(409, "you are group admin or teacher")

        removeUserFromGroup(connection)
        logService.log(EventType.QUIT_GROUP, connection.user, connection.group.name)
    }

    private fun removeUserFromGroup(userGroup: UserGroup) {
        val files = fileRepo.findByUserAndGroup(userGroup.user, userGroup.group)
        files.forEach { fileService.deleteFileStephaneum(userGroup.user, it) }
        userGroupRepo.delete(userGroup)
    }

    private fun Group.toGroupInfo(): GroupInfo {
        return GroupInfo(id, name, leader.toSimpleUser(), accepted, chat, userGroupRepo.countByGroup(this))
    }

    private fun UserGroup.hasAdminPermissions(): Boolean {
        return user.code.role == ROLE_ADMIN || user.id == group.leader.id || teacher
    }

    private fun checkAdminPermission(user: User, groupID: Int): Boolean {
        if(user.code.role == ROLE_ADMIN)
            return true

        val connection = userGroupRepo.findByUserAndGroup(user, groupID.obj()) ?: throw ErrorCode(404, "no origin user-group connection found")
        return connection.hasAdminPermissions()
    }
}