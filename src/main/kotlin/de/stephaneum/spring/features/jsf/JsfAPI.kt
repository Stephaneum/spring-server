package de.stephaneum.spring.features.jsf

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.UserRepo
import de.stephaneum.spring.security.JwtService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class JsfAPI {

    // WIP

    @Autowired
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var userRepo: UserRepo

    @PostMapping("/api/jsf")
    fun listen(data: String) {
        val d = jwtService.getData(data) ?: return
        when(d["type"]) {
            "LOGIN" -> Session.get().user = userRepo.findByIdOrNull(d["userID"] as Int)
            "LOGOUT" -> Session.logout()
        }
    }

}