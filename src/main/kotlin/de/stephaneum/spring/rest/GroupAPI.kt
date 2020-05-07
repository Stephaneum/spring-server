package de.stephaneum.spring.rest

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.*
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.*

data class UpdateGroup(val name: String?, val teachers: List<Int>?, val parent: Int?, val members: List<Int>?)

data class GroupInfo(val id: Int, val name: String, val leader: SimpleUser, val accepted: Boolean, val chat: Boolean, val members: Int)
data class GroupUser(val id: Int, val firstName: String, val lastName: String, val teacher: Boolean, val chat: Boolean, val writeBoard: Boolean)
data class GroupInfoDetailed(val id: Int, val name: String, val leader: SimpleUser, val accepted: Boolean, val chat: Boolean, val showBoardFirst: Boolean, val members: List<GroupUser>, val children: List<GroupInfoDetailed>)

@RestController
@RequestMapping("/api/groups")
class GroupAPI (
        private val userGroupRepo: UserGroupRepo,
        private val groupRepo: GroupRepo,
        private val userRepo: UserRepo,
        private val fileRepo: FileRepo,
        private val fileService: FileService,
        private val logService: LogService
) {

    @GetMapping
    fun getMyProjects(): List<GroupInfo> {
        val user = Session.get().user ?: throw ErrorCode(401, "login")
        return userGroupRepo.findByUserAndGroupParentOrderByGroupName(user, null).map { it.group.toGroupInfo() }
    }

    @GetMapping("/all")
    fun getAllProjects(): List<GroupInfo> {
        val user = Session.get().user ?: throw ErrorCode(401, "login")
        if(user.code.role != ROLE_ADMIN)
            throw ErrorCode(403, "Admin only")

        return groupRepo.findByParentOrderByName(null).map { it.toGroupInfo() }
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
        val members = userGroupRepo
                .findByGroupOrderByUserFirstNameAscUserLastNameAsc(group)
                .map { GroupUser(it.user.id, it.user.firstName, it.user.lastName, it.teacher, it.chat, it.writeBoard) }

        val childrenRaw = if (user.code.role == ROLE_ADMIN) groupRepo.findByParent(group) else userGroupRepo.findByUserAndGroupParent(user, group).map { it.group }
        val children = childrenRaw.map { child ->
            val childMembers = userGroupRepo
                    .findByGroupOrderByUserFirstNameAscUserLastNameAsc(child)
                    .map { GroupUser(it.user.id, it.user.firstName, it.user.lastName, it.teacher, it.chat, it.writeBoard) }
            GroupInfoDetailed(child.id, child.name, child.leader.toSimpleUser(), child.accepted, child.chat, child.showBoardFirst, childMembers, emptyList())
        }
        return GroupInfoDetailed(group.id, group.name, group.leader.toSimpleUser(), group.accepted, group.chat, group.showBoardFirst, members, children)
    }

    @PostMapping("/create")
    fun create(@RequestBody request: UpdateGroup) {
        val user = Session.get().user ?: throw ErrorCode(401, "login")

        if(request.name.isNullOrBlank())
            throw ErrorCode(400, "missing name")

        // parent
        val parent: Group?
        val members: List<User>
        if(request.parent != null && request.members != null) {
            if(!checkAdminPermission(user, request.parent))
                throw ErrorCode(403, "you cannot specify sub groups")

            parent = groupRepo.findByIdOrNull(request.parent) ?: throw ErrorCode(404, "parent not found")
            if(parent.parent != null)
                throw ErrorCode(409, "you cannot create nested sub groups")

            members = userRepo.findByIdIn(request.members)
            if(members.any { it.id == user.id })
                throw ErrorCode(400, "member list should not contain yourself")
        } else {
            parent = null
            members = emptyList()
        }

        // teachers
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
        val group = groupRepo.save(Group(0, request.name.trim(), user, parent != null || user.code.role != ROLE_STUDENT, true, false, false, true, parent))

        // add connections
        val connections = mutableListOf<UserGroup>()
        connections.add(UserGroup(0, user, group, false, true, true, true))
        connections.addAll(teachers.map { UserGroup(0, it, group, true, false, true, true) })
        connections.addAll(members.map { UserGroup(0, it, group, false, true, true, false) })
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

    @PostMapping("/{id}/show-board-first/{show}")
    fun updateShowBoardFirst(@PathVariable id: Int, @PathVariable show: Int) {
        val user = Session.get().user ?: throw ErrorCode(401, "login")

        val group: Group
        if (user.code.role == ROLE_ADMIN) {
            group = groupRepo.findByIdOrNull(id) ?: throw ErrorCode(404, "not found")
        } else {
            group = userGroupRepo.findByUserAndGroup(user, id.obj())?.group ?: throw ErrorCode(403, "forbidden")
        }

        group.showBoardFirst = show == 1
        groupRepo.save(group)
    }

    @PostMapping("/{id}/update")
    fun renameGroup(@PathVariable id: Int, @RequestBody request: UpdateGroup) {
        val user = Session.get().user ?: throw ErrorCode(401, "login")

        if(request.name.isNullOrBlank())
            throw ErrorCode(400, "missing name")

        if(!checkAdminPermission(user, id))
            throw ErrorCode(403, "you are not teacher or (group) admin")

        val group = groupRepo.findByIdOrNull(id) ?: throw ErrorCode(404, "group not found")

        group.name = request.name
        groupRepo.save(group)
    }

    @PostMapping("/{id}/delete")
    fun delete(@PathVariable id: Int) {
        val user = Session.get().user ?: throw ErrorCode(401, "login")

        if(!checkAdminPermission(user, id))
            throw ErrorCode(403, "you are not teacher or (group) admin")

        val group = groupRepo.findByIdOrNull(id) ?: throw ErrorCode(404, "group not found")

        deleteFilesGroupRecursive(user, group)

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

    @PostMapping("/{groupID}/toggle-write-board/{userID}")
    fun toggleWriteBoardUser(@PathVariable groupID: Int, @PathVariable userID: Int) {
        val user = Session.get().user ?: throw ErrorCode(401, "login")
        if(!checkAdminPermission(user, groupID))
            throw ErrorCode(403, "you are not teacher or (group) admin")

        val targetConnection = userGroupRepo.findByUserAndGroup(userID.obj(), groupID.obj()) ?: throw ErrorCode(404, "no target user-group connection found")
        if(targetConnection.hasAdminPermissions())
            throw ErrorCode(403, "this user has admin rights")
        targetConnection.writeBoard = !targetConnection.writeBoard
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

        removeUserFromGroupRecursive(targetConnection)
        logService.log(EventType.QUIT_GROUP, targetConnection.user, targetConnection.group.name)
    }

    @PostMapping("/{groupID}/leave")
    fun leave(@PathVariable groupID: Int) {
        val user = Session.get().user ?: throw ErrorCode(401, "login")

        val connection = userGroupRepo.findByUserAndGroup(user, groupID.obj()) ?: throw ErrorCode(404, "you are not member of this group")
        if(user.id == connection.group.id || connection.teacher)
            throw ErrorCode(409, "you are group admin or teacher")

        removeUserFromGroupRecursive(connection)
        logService.log(EventType.QUIT_GROUP, connection.user, connection.group.name)
    }

    private fun removeUserFromGroupRecursive(userGroup: UserGroup) {
        val files = fileRepo.findByUserAndGroup(userGroup.user, userGroup.group)
        files.forEach { fileService.deleteFileStephaneum(userGroup.user, it) }
        userGroupRepo.delete(userGroup)

        userGroupRepo.findByUserAndGroupParent(userGroup.user, userGroup.group).forEach { removeUserFromGroupRecursive(it) }
    }

    private fun deleteFilesGroupRecursive(user: User, group: Group) {
        val files = fileRepo.findByGroup(group)
        files.forEach { fileService.deleteFileStephaneum(user, it) } // delete all files

        groupRepo.findByParent(group).forEach { deleteFilesGroupRecursive(user, it) }
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