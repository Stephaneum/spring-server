package de.stephaneum.spring.backup

import de.stephaneum.spring.Permission
import de.stephaneum.spring.Session
import de.stephaneum.spring.Session.addToast
import de.stephaneum.spring.scheduler.ConfigFetcher
import de.stephaneum.spring.helper.FileService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
@RequestMapping("/backup")
class BackupAdmin {

    @Autowired
    private lateinit var configFetcher: ConfigFetcher

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var backupService: BackupService

    @GetMapping("/admin")
    fun admin(model: Model): String {
        if(Session.get().permission != Permission.BACKUP)
            return REDIRECT_LOGIN

        if(backupService.running)
            return REDIRECT_LOGS

        var modules = listOf<Module>()
        var totalSize = 0L
        configFetcher.backupLocation?.let { backupLocation ->
            modules = MODULES.map { module ->
                val backups = fileService.listFiles("$backupLocation/${module.code}")?.map { file ->
                    totalSize += file.length()
                    Backup(file.name, fileService.convertSizeToString(file.length()))
                }?.sortedBy { it.name }
                Module(module.display, backups ?: emptyList(), "backup-${module.code}", "upload-${module.code}")
            }
        }

        model["modules"] = modules
        model["backupLocation"] = configFetcher.backupLocation ?: "?"
        model["totalSize"] = fileService.convertSizeToString(totalSize)
        model.addAttribute("toast", Session.getAndDeleteToast())

        return "backup/admin"
    }

    @GetMapping("/backup-{module}")
    fun backup(@PathVariable module: String): String {

        if(backupService.running)
            return REDIRECT_LOGS

        when(module) {
            ModuleType.HOMEPAGE.code -> backupService.backup(ModuleType.HOMEPAGE)
            ModuleType.MOODLE.code -> backupService.backup(ModuleType.MOODLE)
            ModuleType.AR.code -> backupService.backup(ModuleType.AR)
            else -> {
                addToast("Server Error", "$module existiert nicht")
                return REDIRECT_ADMIN
            }
        }
        return REDIRECT_LOGS
    }

    @GetMapping("/logs")
    fun logs(model: Model): String {
        if(Session.get().permission != Permission.BACKUP)
            return REDIRECT_LOGIN

        if(!backupService.running)
            return REDIRECT_ADMIN

        model["logs"] = BackupLogs.getLogsHTML()
        return "backup/logs"
    }

    @GetMapping("/log-data")
    @ResponseBody
    fun logData(): Log? {
        if(Session.get().permission != Permission.BACKUP)
            return null
        return Log(BackupLogs.getLogsHTML(), backupService.running)
    }
}