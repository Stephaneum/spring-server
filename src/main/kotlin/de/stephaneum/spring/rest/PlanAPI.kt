package de.stephaneum.spring.rest

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.*
import de.stephaneum.spring.rest.objects.Response
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.scheduler.Element
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

data class LastModified(val lastModified: String)

@RestController
@RequestMapping("/api/plan")
class PlanAPI (
        private val planService: PlanService,
        private val configScheduler: ConfigScheduler,
        private val fileService: FileService,
        private val logService: LogService
) {

    private val lastModifiedFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm (EEEE)", Locale.GERMANY).withZone(ZoneId.systemDefault())
    private val finalFileName = "vertretungsplan.pdf"

    @GetMapping("/last-modified")
    fun lastModified(): LastModified {
        val path = configScheduler.get(Element.planLocation) ?: throw ErrorCode(409, "no plan")
        val file = File(path)
        if(!file.exists())
            throw ErrorCode(500, "file does not exists")

        return LastModified(lastModifiedFormat.format(Instant.ofEpochMilli(file.lastModified())))
    }

    @PostMapping("/text")
    fun updateText(@RequestParam(required = false) text: String?) {
        val me = Session.getUser()
        if(me.code.role != ROLE_ADMIN && !me.managePlans)
            throw ErrorCode(403, "not allowed")

        configScheduler.save(Element.planInfo, text)
    }

    @PostMapping("/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile): Response.Feedback {
        val me = Session.getUser()
        if(me.code.role != ROLE_ADMIN && !me.managePlans)
            throw ErrorCode(403, "not allowed")

        val fileName = file.originalFilename ?: throw ErrorCode(400, "unknown filename")

        if(!fileName.toLowerCase().endsWith(".pdf"))
            throw ErrorCode(409, "only pdf allowed")

        val path = fileService.storeFile(file.bytes, "${configScheduler.get(Element.fileLocation)}/$finalFileName") ?: throw ErrorCode(500, "file could not be saved")
        val info = planService.resolveDate(File(path))
        if(info != null)
            configScheduler.save(Element.planInfo, info)
        configScheduler.save(Element.planLocation, path)
        logService.log(EventType.UPLOAD, "Vertretungsplan", me)
        return Response.Feedback(true, message = if(info != null) "Änderungen gespeichert.<br>Datum wurde erkannt und aktualisiert." else "Änderungen gespeichert.")
    }

    @PostMapping("/delete")
    fun delete() {
        val me = Session.getUser()
        if(me.code.role != ROLE_ADMIN && !me.managePlans)
            throw ErrorCode(403, "not allowed")

        val path = configScheduler.get(Element.planLocation) ?: throw ErrorCode(404, "no plan")
        val success = fileService.deleteFile(path)
        if(!success)
            throw ErrorCode(500, "file could not be deleted")

        configScheduler.save(Element.planLocation, null)
    }
}