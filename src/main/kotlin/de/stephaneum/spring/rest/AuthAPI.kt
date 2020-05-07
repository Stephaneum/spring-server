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

    private val PEPPER = "A43w8pa0M245qga4293zt9o4mc3z98TA3nQ9mzvTa943cta43mTaoz47tz3loIhbiKh"

    @Autowired
    private lateinit var cryptoService: CryptoService

    @Autowired
    private lateinit var userRepo: UserRepo

    @ExperimentalUnsignedTypes
    @PostMapping("/login")
    fun login(@RequestBody request: Request.Login): Response.Feedback {
        val user = userRepo.findByEmail(request.email ?: "") ?: return Response.Feedback(false)
        val salt = user.password.substring(32)
        if(user.password == cryptoService.hashMD5(request.password+salt+PEPPER)+salt) {
            Session.get().user = user;
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