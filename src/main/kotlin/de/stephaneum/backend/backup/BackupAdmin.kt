package de.stephaneum.backend.backup

import de.stephaneum.backend.Permission
import de.stephaneum.backend.Session
import de.stephaneum.backend.scheduler.ConfigFetcher
import de.stephaneum.backend.helper.FileService
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

        var modules = listOf<Module>()
        var totalSize = 0L
        with(configFetcher.backupLocation) {
            if(this != null) {
                modules = MODULES.map { module ->
                    val moduleLowerCase = module.toLowerCase()
                    val backups = fileService.listFiles("$this/$moduleLowerCase")?.map { file ->
                        totalSize += file.length()
                        Backup(file.name, fileService.convertSizeToString(file.length()))
                    }?.sortedBy { it.name }
                    Module(module, backups ?: emptyList(), "backup-$moduleLowerCase", "upload-$moduleLowerCase")
                }
            }
        }

        model["modules"] = modules
        model["backupLocation"] = configFetcher.backupLocation ?: "?"
        model["totalSize"] = fileService.convertSizeToString(totalSize)
        model.addAttribute("toast", Session.getAndDeleteToast())

        return "backup/admin"
    }
}