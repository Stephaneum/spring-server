package de.stephaneum.spring.rest

import de.stephaneum.spring.database.UserRepo
import de.stephaneum.spring.helper.toMiniUser
import de.stephaneum.spring.helper.toSimpleUser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

data class SearchRequest(val firstName: String?, val lastName: String?, val role: Int?)

@RestController
@RequestMapping("/api/search")
class SearchAPI {

    @Autowired
    private lateinit var userRepo: UserRepo

    @PostMapping("/user")
    fun search(@RequestBody request: SearchRequest, @RequestParam(required = false) detailed: Boolean = false): List<*> {
        val users = if(request.role != null) {
            if(request.firstName.isNullOrBlank() && request.lastName.isNullOrBlank())
                userRepo.findByCodeRoleOrderBySchoolClassGradeAscSchoolClassSuffixAscFirstNameAscLastNameAsc(request.role)
            else
                userRepo.findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCaseAndCodeRoleOrderBySchoolClassGradeAscSchoolClassSuffixAscFirstNameAscLastNameAsc(request.firstName ?: "", request.lastName ?: "", request.role)
        } else {
            if(request.firstName.isNullOrBlank() && request.lastName.isNullOrBlank())
                userRepo.findByOrderBySchoolClassGradeAscSchoolClassSuffixAscFirstNameAscLastNameAsc()
            else
                userRepo.findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCaseOrderBySchoolClassGradeAscSchoolClassSuffixAscFirstNameAscLastNameAsc(request.firstName ?: "", request.lastName ?: "")
        }

        return if(detailed)
            users.map { it.toSimpleUser() }
        else
            users.map { it.toMiniUser() }
    }
}