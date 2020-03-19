package de.stephaneum.spring.features.groups

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.ErrorCode
import de.stephaneum.spring.helper.obj
import de.stephaneum.spring.helper.toSimpleUser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/groups")
class GroupAPI {

    @Autowired
    private lateinit var userGroupRepo: UserGroupRepo

    @Autowired
    private lateinit var groupRepo: GroupRepo

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

        return groupRepo.findByAccepted(true).map { it.toGroupInfo() }
    }

    @GetMapping("/{id}")
    fun getProject(@PathVariable id: Int): GroupInfoDetailed {
        val user = Session.get().user ?: throw ErrorCode(401, "login")
        val group = userGroupRepo.findByUserAndGroup(user, id.obj())?.group ?: throw ErrorCode(403, "forbidden")
        val membersRaw = userGroupRepo.findByGroupOrderByUserFirstNameAscUserLastNameAsc(group)
        val members = membersRaw.map { it.user.toSimpleUser() }
        val teachers = membersRaw.filter { it.teacher }.map { it.user.toSimpleUser() }
        return GroupInfoDetailed(group.id, group.name, group.leader.toSimpleUser(), group.accepted, group.chat, members, teachers)
    }

    private fun Group.toGroupInfo(): GroupInfo {
        return GroupInfo(id, name, leader.toSimpleUser(), accepted, chat, userGroupRepo.countByGroup(this))
    }
}

data class GroupInfo(val id: Int, val name: String, val leader: SimpleUser, val accepted: Boolean, val chat: Boolean, val members: Int)
data class GroupInfoDetailed(val id: Int, val name: String, val leader: SimpleUser, val accepted: Boolean, val chat: Boolean, val members: List<SimpleUser>, val teachers: List<SimpleUser>)