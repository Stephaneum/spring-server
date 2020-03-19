package de.stephaneum.spring.features.groups

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.ErrorCode
import de.stephaneum.spring.helper.LogService
import de.stephaneum.spring.helper.obj
import de.stephaneum.spring.helper.toSimpleUser
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
        val members = membersRaw.map { it.user.toSimpleUser() }
        val teachers = membersRaw.filter { it.teacher }.map { it.user.toSimpleUser() }
        return GroupInfoDetailed(group.id, group.name, group.leader.toSimpleUser(), group.accepted, group.chat, members, teachers)
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

        val group: Group
        if (user.code.role == ROLE_ADMIN) {
            group = groupRepo.findByIdOrNull(id) ?: throw ErrorCode(404, "not found")
        } else {
            group = userGroupRepo.findByUserAndGroup(user, id.obj())?.group ?: throw ErrorCode(403, "forbidden")
        }
        groupRepo.delete(group)
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

    private fun Group.toGroupInfo(): GroupInfo {
        return GroupInfo(id, name, leader.toSimpleUser(), accepted, chat, userGroupRepo.countByGroup(this))
    }
}