package de.stephaneum.spring.backup

import de.stephaneum.spring.Permission
import de.stephaneum.spring.Session
import de.stephaneum.spring.helper.ErrorCode
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
    fun data(): Response.AdminData {

        Session.mustHaveAccess(Permission.BACKUP)

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
    fun backupFull() {

        Session.mustHaveAccess(Permission.BACKUP)
        mustNotRunning()

        backupService.backupFull()
    }

    @PostMapping("/backup-{module}")
    fun backup(@PathVariable module: String) {

        Session.mustHaveAccess(Permission.BACKUP)
        mustNotRunning()

        when(module) {
            ModuleType.HOMEPAGE.code -> backupService.backup(ModuleType.HOMEPAGE)
            ModuleType.MOODLE.code -> backupService.backup(ModuleType.MOODLE)
            ModuleType.AR.code -> backupService.backup(ModuleType.AR)
            else -> throw ErrorCode(400, "unknown backup module")
        }
    }

    @PostMapping("/restore/{module}/{file}")
    fun restore(@PathVariable module: String, @PathVariable file: String) {

        Session.mustHaveAccess(Permission.BACKUP)
        mustNotRunning()

        when(module) {
            ModuleType.HOMEPAGE.code -> backupService.restore(ModuleType.HOMEPAGE, file)
            ModuleType.MOODLE.code -> backupService.restore(ModuleType.MOODLE, file)
            ModuleType.AR.code -> backupService.restore(ModuleType.AR, file)
            else -> throw ErrorCode(400, "unknown backup module")
        }
    }

    @PostMapping("/upload-{module}")
    fun uploadFile(@PathVariable module: String, @RequestParam("file") file: MultipartFile) {

        Session.mustHaveAccess(Permission.BACKUP)

        val fileName = file.originalFilename ?: throw ErrorCode(400, "unknown file name")

        when(module) {
            ModuleType.HOMEPAGE.code -> {
                if(!fileName.toLowerCase().endsWith(".zip")) {
                    throw ErrorCode(409, "only zip files")
                }
            }
            ModuleType.MOODLE.code -> {
                if(!fileName.toLowerCase().endsWith(".zip")) {
                    throw ErrorCode(409, "only .zip files")
                }
            }
            ModuleType.AR.code -> {
                if(!fileName.toLowerCase().endsWith(".sql")) {
                    throw ErrorCode(409, "only .sql files")
                }
            }
            else -> throw ErrorCode(400, "unknown module '$module'")
        }

        fileService.storeFile(file.bytes, "${configScheduler.get(Element.backupLocation)}/$module/$fileName") ?: throw ErrorCode(500, "could not save file")
    }

    @GetMapping("/download/{folder}/{file}")
    fun download(@PathVariable folder: String, @PathVariable file: String, response: HttpServletResponse): ResponseEntity<*> {

        Session.mustHaveAccess(Permission.BACKUP)

        val backupLocation = configScheduler.get(Element.backupLocation) ?: throw ErrorCode(500, "unknown backup location")
        val resource = fileService.loadFileAsResource("$backupLocation/$folder/$file") ?: throw ErrorCode(404, "file not found")

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"$file\"")
                .contentLength(resource.contentLength())
                .contentType(MediaType.parseMediaType(if(file.endsWith(".zip")) "application/zip" else "application/sql"))
                .body(resource)
    }

    @PostMapping("/delete/{folder}/{file}")
    fun delete(@PathVariable folder: String, @PathVariable file: String) {

        Session.mustHaveAccess(Permission.BACKUP)

        val backupLocation = configScheduler.get(Element.backupLocation) ?: throw ErrorCode(500, "unknown backup location")
        val success = fileService.deleteFile("$backupLocation/$folder/$file")

        if(!success)
            throw ErrorCode(500, "deletion failed")
    }

    @PostMapping("/set-password")
    fun setPassword(@RequestBody request: Request.Password) {

        Session.mustHaveAccess(Permission.BACKUP)

        backupService.sudoPassword = request.password

        // now test if it was successful
        backupService.sudoPassword ?: throw ErrorCode(403, "sudo test failed")
    }

    @GetMapping("/log-data")
    fun logData(): LogData {

        Session.mustHaveAccess(Permission.BACKUP)

        return LogData(BackupLogger.getLogsHTML(), backupService.running, backupService.error)
    }

    private fun mustNotRunning() {
        if(backupService.running)
            throw ErrorCode(409, "backup is already running")
    }
}