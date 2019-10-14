package de.stephaneum.spring.features.backup

import de.stephaneum.spring.Permission
import de.stephaneum.spring.Session
import de.stephaneum.spring.helper.checkIE
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import javax.servlet.http.HttpServletRequest

@Controller
@RequestMapping("/backup")
class BackupController {

    @Autowired
    private lateinit var backupService: BackupService

    @GetMapping
    fun home(): String {
        if(Session.get().permission == Permission.BACKUP)
            return REDIRECT_ADMIN
        else
            return REDIRECT_LOGIN
    }

    @GetMapping("/login")
    fun login(model: Model, @RequestParam error: Boolean = false, request: HttpServletRequest): String {

        if(checkIE(request))
            return "forward:/static/no-support-ie.html"

        if(Session.get().permission == Permission.BACKUP)
            return REDIRECT_ADMIN

        model["title"] = "Backup"
        return "login-generic"
    }

    @GetMapping("/admin")
    fun admin(model: Model): String {
        if(Session.get().permission != Permission.BACKUP)
            return REDIRECT_LOGIN

        if(backupService.running)
            return "redirect:/backup/logs"

        return "backup/admin"
    }

    @GetMapping("/logs")
    fun logs(model: Model): String {
        if(Session.get().permission != Permission.BACKUP)
            return REDIRECT_LOGIN

        return "backup/logs"
    }

}