package de.stephaneum.spring.features.backup

import de.stephaneum.spring.Permission
import de.stephaneum.spring.Session
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/backup/api")
class BackupAuthAPI {

    @Value("\${backup.password}")
    private lateinit var password: String

    @PostMapping("/login")
    fun login(@RequestBody request: Request.Password): Response.Feedback {
        if(request.password == this.password) {
            Session.login(Permission.BACKUP)
            return Response.Feedback(true)
        } else {
            Thread.sleep(2000)
            return Response.Feedback(false)
        }
    }

    @PostMapping("/logout")
    fun logout(): Response.Feedback {
        Session.logout()

        return Response.Feedback(true)
    }
}