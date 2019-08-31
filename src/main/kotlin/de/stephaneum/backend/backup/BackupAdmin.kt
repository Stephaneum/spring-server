package de.stephaneum.backend.backup

import de.stephaneum.backend.Permission
import de.stephaneum.backend.Session
import de.stephaneum.backend.scheduler.ConfigFetcher
import de.stephaneum.backend.services.FileService
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

    @Autowired
    private lateinit var fileService: FileService

    @GetMapping("/admin")
    fun admin(model: Model): String {
        if(Session.get().permission != Permission.BACKUP)
            return REDIRECT_LOGIN

        with(configFetcher.backupLocation) {
            if(this != null) {
                model["modules"] = MODULES.map { module ->
                    val moduleLowerCase = module.toLowerCase()
                    val files = fileService.listFiles("$this/$moduleLowerCase")
                    val backups = mutableListOf<Backup>()
                    files?.forEach { file ->
                        if (file.isFile) {
                            backups.add(Backup(file.name, fileService.convertSizeToString(file.length())))
                        }
                    }
                    Module(module, backups.sortedBy { it.name }, "backup-$moduleLowerCase", "upload-$moduleLowerCase")
                }
            } else {
                model["modules"] = listOf<Module>()
            }
        }

        model["backupLocation"] = configFetcher.backupLocation ?: "?"
        model.addAttribute("toast", Session.getAndDeleteToast())

        return "backup/admin"
    }
}