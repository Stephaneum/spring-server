package de.stephaneum.spring.backup

import de.stephaneum.spring.Permission
import de.stephaneum.spring.Session
import de.stephaneum.spring.helper.ErrorCode
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/backup/api")
class BackupAuthAPI (
        @Value("\${backup.password}")
        private val password: String
) {

    @PostMapping("/login")
    fun login(@RequestBody request: Request.Password) {

        if(request.password != this.password) {
            Thread.sleep(2000)
            throw ErrorCode(403, "login failed")
        }

        Session.login(Permission.BACKUP)
    }

    @PostMapping("/logout")
    fun logout() {
        Session.logout()
    }
}