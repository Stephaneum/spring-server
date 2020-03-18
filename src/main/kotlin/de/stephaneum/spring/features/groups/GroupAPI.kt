package de.stephaneum.spring.features.groups

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.Group
import de.stephaneum.spring.database.GroupRepo
import de.stephaneum.spring.database.ROLE_ADMIN
import de.stephaneum.spring.database.UserGroupRepo
import de.stephaneum.spring.helper.ErrorCode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
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
    fun getMyProjects(): List<Group> {
        val user = Session.get().user ?: throw ErrorCode(401, "login")
        return userGroupRepo.findByUserOrderByGroupName(user).map { it.group }
    }

    @GetMapping("/all")
    fun getAllProjects(): List<Group> {
        val user = Session.get().user ?: throw ErrorCode(401, "login")
        if(user.code.role != ROLE_ADMIN)
            throw ErrorCode(403, "Admin only")

        return groupRepo.findByAccepted(true)
    }
}