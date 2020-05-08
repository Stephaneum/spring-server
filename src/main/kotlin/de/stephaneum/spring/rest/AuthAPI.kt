package de.stephaneum.spring.rest

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.UserRepo
import de.stephaneum.spring.rest.objects.Request
import de.stephaneum.spring.rest.objects.Response
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
    fun login(@RequestBody request: Request.Login): Response.Feedback {
        val user = userRepo.findByEmail(request.email ?: "") ?: return Response.Feedback(false)
        if(cryptoService.checkPassword(request.password ?: "", user.password)) {
            Session.get().user = user
            return Response.Feedback(true)
        } else {
            Thread.sleep(2000)
            return Response.Feedback(false)
        }
    }

    @PostMapping("/logout")
    fun logout() {
        Session.logout()
    }
}