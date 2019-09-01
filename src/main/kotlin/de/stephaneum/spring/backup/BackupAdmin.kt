package de.stephaneum.spring.backup

import de.stephaneum.spring.Permission
import de.stephaneum.spring.Session
import de.stephaneum.spring.Session.addToast
import de.stephaneum.spring.scheduler.ConfigFetcher
import de.stephaneum.spring.helper.FileService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import javax.servlet.http.HttpServletResponse

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
                Module(module.display, module.code, backups ?: emptyList())
            }
        }

        model["modules"] = modules
        model["backupLocation"] = configFetcher.backupLocation ?: "?"
        model["totalSize"] = fileService.convertSizeToString(totalSize)
        model.addAttribute("toast", Session.getAndDeleteToast())

        return "backup/admin"
    }

    @GetMapping("/backup")
    fun backupFull(): String {

        if(backupService.running)
            return REDIRECT_LOGS

        backupService.backupFull()
        return REDIRECT_LOGS
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

    @GetMapping("/restore/{module}/{file}")
    fun restore(@PathVariable module: String, @PathVariable file: String): String {

        if(backupService.running)
            return REDIRECT_LOGS

        when(module) {
            ModuleType.HOMEPAGE.code -> backupService.restore(ModuleType.HOMEPAGE, file)
            ModuleType.MOODLE.code -> backupService.restore(ModuleType.MOODLE, file)
            ModuleType.AR.code -> backupService.restore(ModuleType.AR, file)
            else -> {
                addToast("Server Error", "$module existiert nicht")
                return REDIRECT_ADMIN
            }
        }
        return REDIRECT_LOGS
    }

    @GetMapping("/download/{folder}/{file}")
    fun download(@PathVariable folder: String, @PathVariable file: String, response: HttpServletResponse): Any? {

        if(Session.get().permission != Permission.BACKUP)
            return REDIRECT_LOGIN

        val resource = configFetcher.backupLocation?.let { location ->
            fileService.loadFileAsResource("$location/$folder/$file")
        }

        if(resource != null)
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"$file\"")
                    .contentLength(resource.contentLength())
                    .contentType(MediaType.parseMediaType(if(file.endsWith(".zip")) "application/zip" else "application/sql"))
                    .body(resource)
        else
            return null
    }

    @GetMapping("/delete/{folder}/{file}")
    fun delete(@PathVariable folder: String, @PathVariable file: String): String {

        configFetcher.backupLocation?.let { location ->
            if(fileService.deleteFile("$location/$folder/$file")) {
                addToast("Backup gelöscht", file)
            } else {
                addToast("Löschen fehlgeschlagen", file)
            }
        }

        return REDIRECT_ADMIN
    }

    @GetMapping("/logs")
    fun logs(model: Model): String {
        if(Session.get().permission != Permission.BACKUP)
            return REDIRECT_LOGIN

        if(!backupService.running && !backupService.error)
            return REDIRECT_ADMIN

        model["logs"] = BackupLogs.getLogsHTML()
        return "backup/logs"
    }

    @GetMapping("/log-data")
    @ResponseBody
    fun logData(): Log? {
        if(Session.get().permission != Permission.BACKUP)
            return null
        return Log(BackupLogs.getLogsHTML(), backupService.running, backupService.error)
    }
}