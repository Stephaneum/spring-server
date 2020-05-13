package de.stephaneum.spring.rest

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.UserRepo
import de.stephaneum.spring.helper.ErrorCode
import de.stephaneum.spring.rest.objects.Request
import de.stephaneum.spring.security.CryptoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class AuthAPI {

    @Autowired
    private lateinit var cryptoService: CryptoService

    @Autowired
    private lateinit var userRepo: UserRepo

    @ExperimentalUnsignedTypes
    @PostMapping("/login")
    fun login(@RequestBody request: Request.Login) {
        val user = userRepo.findByEmail(request.email ?: "") ?: throw ErrorCode(403, "Login failed")
        if(!cryptoService.checkPassword(request.password ?: "", user.password)) {
            Thread.sleep(2000)
            throw ErrorCode(403, "Login failed")
        }

        Session.get().user = user
    }

    @PostMapping("/logout")
    fun logout() {
        Session.logout()
    }
}