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
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
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

    @Autowired
    private lateinit var backupScheduler: BackupScheduler

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
                Module(module.display, module.code, backups ?: emptyList(), module == ModuleType.MOODLE && backupService.sudoPassword == null)
            }
        }

        model["modules"] = modules
        model["backupLocation"] = configFetcher.backupLocation ?: "?"
        model["totalSize"] = fileService.convertSizeToString(totalSize)
        model["nextBackup"] = backupScheduler.getNextBackup()
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

    @PostMapping("/upload-{module}")
    fun uploadFile(@PathVariable module: String, @RequestParam("file") file: MultipartFile): String {

        if(Session.get().permission != Permission.BACKUP)
            return REDIRECT_LOGIN

        val fileName = file.originalFilename
        if(fileName == null) {
            addToast("Ein Fehler ist aufgetreten", "Dateiname unbekannt")
            return REDIRECT_ADMIN
        }

        when(module) {
            ModuleType.HOMEPAGE.code -> {
                if(!fileName.toLowerCase().endsWith(".zip")) {
                    addToast("Ein Fehler ist aufgetreten", "Nur ZIP-Dateien erlaubt")
                    return REDIRECT_ADMIN
                }
            }
            ModuleType.MOODLE.code -> {
                if(!fileName.toLowerCase().endsWith(".zip")) {
                    addToast("Ein Fehler ist aufgetreten", "Nur ZIP-Dateien erlaubt")
                    return REDIRECT_ADMIN
                }
            }
            ModuleType.AR.code -> {
                if(!fileName.toLowerCase().endsWith(".sql")) {
                    addToast("Ein Fehler ist aufgetreten", "Nur SQL-Dateien erlaubt")
                    return REDIRECT_ADMIN
                }
            }
            else -> {
                addToast("Server Error", "$module existiert nicht")
                return REDIRECT_ADMIN
            }
        }

        val path = fileService.storeFile(file.bytes, "${configFetcher.backupLocation}/$module/$fileName")
        if(path != null) {
            addToast("Datei hochgeladen")
        } else
            addToast("Ein Fehler ist aufgetreten")
        return REDIRECT_ADMIN
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

    @PostMapping("/set-password")
    fun setPassword(@RequestParam password: String): String {
        backupService.sudoPassword = password
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