package de.stephaneum.spring.features.search

import de.stephaneum.spring.database.SimpleUser
import de.stephaneum.spring.database.User
import de.stephaneum.spring.database.UserRepo
import de.stephaneum.spring.helper.ErrorCode
import de.stephaneum.spring.helper.toSimpleUser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/search")
class SearchAPI {

    @Autowired
    private lateinit var userRepo: UserRepo

    @PostMapping("/user")
    fun search(@RequestBody request: SearchRequest): List<SimpleUser> {
        val users: List<User>
        if(request.role != null) {
            if(request.firstName.isNullOrBlank() && request.lastName.isNullOrBlank())
                users = userRepo.findByCodeRoleOrderBySchoolClassGradeAscSchoolClassSuffixAscFirstNameAscLastNameAsc(request.role)
            else
                users = userRepo.findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCaseAndCodeRoleOrderBySchoolClassGradeAscSchoolClassSuffixAscFirstNameAscLastNameAsc(request.firstName ?: "", request.lastName ?: "", request.role)
        } else {
            if(request.firstName.isNullOrBlank() && request.lastName.isNullOrBlank())
                throw ErrorCode(400, "missing arguments")
            else
                users = userRepo.findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCaseOrderBySchoolClassGradeAscSchoolClassSuffixAscFirstNameAscLastNameAsc(request.firstName ?: "", request.lastName ?: "")
        }
        return users.map { it.toSimpleUser() }
    }
}