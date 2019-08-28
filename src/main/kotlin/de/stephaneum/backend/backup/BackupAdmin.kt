package de.stephaneum.backend.backup

import de.stephaneum.backend.Permission
import de.stephaneum.backend.Session
import de.stephaneum.backend.scheduler.ConfigFetcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/backup")
class BackupAdmin {

    @Autowired
    private lateinit var configFetcher: ConfigFetcher

    @GetMapping("/admin")
    fun admin(model: Model): String {
        if(Session.get().permission != Permission.BACKUP)
            return REDIRECT_LOGIN

        model["backupLocation"] = configFetcher.backupLocation ?: "?"
        model["backupHomepage"] = listOf(
                Backup("homepage_2019-08-24_04-30-34.zip", "304 MB"),
                Backup("homepage_2019-08-24_04-30-34.zip", "304 MB"),
                Backup("homepage_2019-08-24_04-30-34.zip", "304 MB"),
                Backup("homepage_2019-08-24_04-30-34.zip", "304 MB"),
                Backup("homepage_2019-08-24_04-30-34.zip", "304 MB"))
        model["backupMoodle"] = listOf(
                Backup("moodle_2019-08-24_04-30-34.zip", "304 MB"),
                Backup("moodle_2019-08-24_04-30-34.zip", "304 MB"),
                Backup("moodle_2019-08-24_04-30-34.zip", "304 MB"),
                Backup("moodle_2019-08-24_04-30-34.zip", "304 MB"),
                Backup("moodle_2019-08-24_04-30-34.zip", "304 MB"),
                Backup("moodle_2019-08-24_04-30-34.zip", "304 MB"))
        model.addAttribute("toast", Session.getAndDeleteToast())

        return "backup/admin"
    }
}