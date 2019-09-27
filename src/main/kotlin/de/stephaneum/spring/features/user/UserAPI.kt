package de.stephaneum.spring.features.user

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.Code
import de.stephaneum.spring.database.ROLE_NO_LOGIN
import de.stephaneum.spring.database.User
import de.stephaneum.spring.database.UserRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class UserAPI {

    @Autowired
    private lateinit var userRepo: UserRepo

    @GetMapping("/me")
    fun get(): User {
        return Session.get().user ?: User(-1, Code(-1, "", ROLE_NO_LOGIN))
    }
}