package de.stephaneum.spring.blackboard

import de.stephaneum.spring.Permission
import de.stephaneum.spring.Session
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.*


@Controller
@RequestMapping("/blackboard")
class BlackboardAuth {

    @Value("\${blackboard.password}")
    private lateinit var password: String

    @GetMapping("/login")
    fun login(model: Model, @RequestParam error: Boolean = false): String {
        if(Session.get().permission == Permission.BLACKBOARD)
            return REDIRECT_ADMIN

        model["title"] = "Blackboard"
        return "login"
    }

    @PostMapping("/login")
    @ResponseBody
    fun login(@RequestBody request: Request.Login): Any? {
        if(request.password == this.password) {
            Session.login(Permission.BLACKBOARD)
            return Response.Feedback(true)
        } else {
            Thread.sleep(2000)
            return Response.Feedback(false)
        }
    }

    @GetMapping("/logout")
    fun logout(): String {
        Session.logout()

        return "redirect:/blackboard/"
    }
}