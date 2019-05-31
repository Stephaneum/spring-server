package de.stephaneum.backend.blackboard

import de.stephaneum.backend.Session
import de.stephaneum.backend.Toast
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/blackboard")
class Auth {

    @Value("\${blackboard.password}")
    private lateinit var password: String

    @GetMapping("/login")
    fun login(model: Model, @RequestParam error: Boolean = false): String {
        if(Session.get().loggedIn)
            return REDIRECT_ADMIN

        model["loginFailed"] = error
        if(error) {
            model["toast"] = Toast("Login gescheitert")
            println("here")
        }

        return "blackboard/login"
    }

    @PostMapping("/login")
    fun login(password: String): Any? {
        if(password == this.password) {
            Session.login()
            return REDIRECT_ADMIN
        } else {
            return "redirect:/blackboard/login?error=true"
        }
    }

    @GetMapping("/logout")
    fun logout(): String {
        Session.logout()

        return "redirect:/blackboard"
    }
}