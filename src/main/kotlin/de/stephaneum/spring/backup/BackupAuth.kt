package de.stephaneum.spring.backup

import de.stephaneum.spring.Permission
import de.stephaneum.spring.Session
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.*


@Controller
@RequestMapping("/backup")
class BackupAuth {

    @Value("\${backup.password}")
    private lateinit var password: String

    @GetMapping
    fun home(): String {
        if(Session.get().permission == Permission.BACKUP)
            return REDIRECT_ADMIN
        else
            return REDIRECT_LOGIN
    }

    @GetMapping("/login")
    fun login(model: Model, @RequestParam error: Boolean = false): String {
        if(Session.get().permission == Permission.BACKUP)
            return REDIRECT_ADMIN

        model["title"] = "Backup"
        return "login"
    }

    @PostMapping("/login")
    @ResponseBody
    fun login(@RequestBody request: Request.Login): Any? {
        if(request.password == this.password) {
            Session.login(Permission.BACKUP)
            return Response.Feedback(true)
        } else {
            Thread.sleep(2000)
            return Response.Feedback(false)
        }
    }

    @GetMapping("/logout")
    fun logout(): String {
        Session.logout()

        return "redirect:/backup/login"
    }
}