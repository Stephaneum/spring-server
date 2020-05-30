package de.stephaneum.spring.backup

import de.stephaneum.spring.Permission
import de.stephaneum.spring.Session
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.scheduler.Element
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import javax.servlet.http.HttpServletResponse


@RestController
@RequestMapping("/backup/api")
class BackupAdminAPI (
        private val configScheduler: ConfigScheduler,
        private val fileService: FileService,
        private val backupService: BackupService,
        private val backupScheduler: BackupScheduler
) {

    @GetMapping("/data")
    fun data(): Any {

        if(Session.get().permission != Permission.BACKUP)
            return Response.Feedback(false, needLogin = true)

        var modules = listOf<Module>()
        var totalSize = 0L
        configScheduler.get(Element.backupLocation)?.let { backupLocation ->
            modules = MODULES.map { module ->
                val backups = fileService.listFiles("$backupLocation/${module.code}").map { file ->
                    totalSize += file.length()
                    Backup(file.name, fileService.convertSizeToString(file.length()))
                }.sortedBy { it.name }
                Module(module.display, module.code, backups, module == ModuleType.MOODLE && backupService.sudoPassword == null)
            }
        }

        return Response.AdminData(
                modules = modules,
                backupLocation = configScheduler.get(Element.backupLocation) ?: "?",
                totalSize = fileService.convertSizeToString(totalSize),
                nextBackup = backupScheduler.getNextBackup())
    }

    @PostMapping("/backup")
    fun backupFull(): Response.Feedback {

        if(Session.get().permission != Permission.BACKUP)
            return Response.Feedback(false, needLogin = true)

        if(backupService.running)
            return Response.Feedback(false)

        backupService.backupFull()
        return Response.Feedback(true)
    }

    @PostMapping("/backup-{module}")
    fun backup(@PathVariable module: String): Response.Feedback {

        if(Session.get().permission != Permission.BACKUP)
            return Response.Feedback(false, needLogin = true)

        if(backupService.running)
            return Response.Feedback(false)

        when(module) {
            ModuleType.HOMEPAGE.code -> backupService.backup(ModuleType.HOMEPAGE)
            ModuleType.MOODLE.code -> backupService.backup(ModuleType.MOODLE)
            ModuleType.AR.code -> backupService.backup(ModuleType.AR)
            else -> return Response.Feedback(false)
        }
        return Response.Feedback(true)
    }

    @PostMapping("/restore/{module}/{file}")
    fun restore(@PathVariable module: String, @PathVariable file: String): Response.Feedback {

        if(Session.get().permission != Permission.BACKUP)
            return Response.Feedback(false, needLogin = true)

        if(backupService.running)
            return Response.Feedback(false)

        when(module) {
            ModuleType.HOMEPAGE.code -> backupService.restore(ModuleType.HOMEPAGE, file)
            ModuleType.MOODLE.code -> backupService.restore(ModuleType.MOODLE, file)
            ModuleType.AR.code -> backupService.restore(ModuleType.AR, file)
            else -> return Response.Feedback(false)
        }
        return Response.Feedback(true)
    }

    @PostMapping("/upload-{module}")
    fun uploadFile(@PathVariable module: String, @RequestParam("file") file: MultipartFile): Response.Feedback {

        if(Session.get().permission != Permission.BACKUP)
            return Response.Feedback(false, needLogin = true)

        val fileName = file.originalFilename ?: return Response.Feedback(false, message = "Dateiname unbekannt")

        when(module) {
            ModuleType.HOMEPAGE.code -> {
                if(!fileName.toLowerCase().endsWith(".zip")) {
                    return Response.Feedback(false, message = "Nur ZIP-Dateien erlaubt")
                }
            }
            ModuleType.MOODLE.code -> {
                if(!fileName.toLowerCase().endsWith(".zip")) {
                    return Response.Feedback(false, message = "Nur ZIP-Dateien erlaubt")
                }
            }
            ModuleType.AR.code -> {
                if(!fileName.toLowerCase().endsWith(".sql")) {
                    return Response.Feedback(false, message = "Nur SQL-Dateien erlaubt")
                }
            }
            else -> return Response.Feedback(false, message = "$module existiert nicht")
        }

        val path = fileService.storeFile(file.bytes, "${configScheduler.get(Element.backupLocation)}/$module/$fileName")
        if(path != null) {
            return Response.Feedback(true)
        } else {
            return Response.Feedback(false, message = "Ein Fehler ist aufgetreten")
        }
    }

    @GetMapping("/download/{folder}/{file}")
    fun download(@PathVariable folder: String, @PathVariable file: String, response: HttpServletResponse): Any? {

        if(Session.get().permission != Permission.BACKUP)
            return REDIRECT_LOGIN

        val resource = configScheduler.get(Element.backupLocation)?.let { location ->
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

    @PostMapping("/delete/{folder}/{file}")
    fun delete(@PathVariable folder: String, @PathVariable file: String): Response.Feedback {

        if(Session.get().permission != Permission.BACKUP)
            return Response.Feedback(false, needLogin = true)

        var success = true
        configScheduler.get(Element.backupLocation)?.let { location ->
            success = fileService.deleteFile("$location/$folder/$file")
        }

        return Response.Feedback(success)
    }

    @PostMapping("/set-password")
    fun setPassword(@RequestBody request: Request.Password): Response.Feedback {

        if(Session.get().permission != Permission.BACKUP)
            return Response.Feedback(false, needLogin = true)

        backupService.sudoPassword = request.password
        return Response.Feedback(backupService.sudoPassword != null)
    }

    @GetMapping("/log-data")
    fun logData(): Any {
        if(Session.get().permission != Permission.BACKUP)
            return Response.Feedback(false, needLogin = true)
        else
            return Log(BackupLogger.getLogsHTML(), backupService.running, backupService.error)
    }
}