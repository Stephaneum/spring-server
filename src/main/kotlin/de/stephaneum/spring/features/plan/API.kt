package de.stephaneum.spring.features.plan

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.jsf.JsfCommunication
import de.stephaneum.spring.jsf.JsfEvent
import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.helper.LogService
import de.stephaneum.spring.helper.PlanService
import de.stephaneum.spring.helper.Response
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.scheduler.Element
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@RestController
@RequestMapping("/api/plan")
class PlanAPI {

    private val lastModifiedFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm (EEEE)", Locale.GERMANY).withZone(ZoneId.systemDefault());
    private val finalFileName = "vertretungsplan.pdf"

    @Autowired
    private lateinit var jsfCommunication: JsfCommunication

    @Autowired
    private lateinit var planService: PlanService

    @Autowired
    private lateinit var configScheduler: ConfigScheduler

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var logService: LogService

    @GetMapping("/last-modified")
    fun lastModified(): Response.Feedback {
        val path = configScheduler.get(Element.planLocation)
        if(path != null) {
            val file = File(path)
            if(file.exists()) {
                return Response.Feedback(true, message = lastModifiedFormat.format(Instant.ofEpochMilli(file.lastModified())))
            }
        }
        return Response.Feedback(false)
    }

    @PostMapping("/text")
    fun updateText(@RequestParam(required = false) text: String?): Response.Feedback {

        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)
        if(user.code.role != ROLE_ADMIN && user.managePlans != true)
            return Response.Feedback(false, message = "not allowed")

        configScheduler.save(Element.planInfo, text)
        jsfCommunication.send(JsfEvent.SYNC_PLAN)

        return Response.Feedback(true)
    }

    @PostMapping("/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile): Response.Feedback {

        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)
        if(user.code.role != ROLE_ADMIN && user.managePlans != true)
            return Response.Feedback(false, message = "not allowed")

        val fileName = file.originalFilename ?: return Response.Feedback(false, message = "Dateiname unbekannt")

        if(!fileName.toLowerCase().endsWith(".pdf"))
            return Response.Feedback(false, message = "Nur PDF-Dateien erlaubt")

        val path = fileService.storeFile(file.bytes, "${configScheduler.get(Element.fileLocation)}/$finalFileName")
        if(path != null) {
            val info = planService.resolveDate(File(path))
            if(info != null)
                configScheduler.save(Element.planInfo, info)
            configScheduler.save(Element.planLocation, path)
            jsfCommunication.send(JsfEvent.SYNC_PLAN)
            logService.log(EventType.UPLOAD, "Vertretungsplan", user)
            return Response.Feedback(true, message = if(info != null) "Änderungen gespeichert.<br>Datum wurde erkannt und aktualisiert." else "Änderungen gespeichert.")
        } else {
            return Response.Feedback(false, message = "Ein Fehler ist aufgetreten")
        }
    }

    @PostMapping("/delete")
    fun delete(): Response.Feedback {

        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)
        if(user.code.role != ROLE_ADMIN && user.managePlans != true)
            return Response.Feedback(false, message = "not allowed")

        val path = configScheduler.get(Element.planLocation)
        if(path != null) {
            val success = fileService.deleteFile(path)
            if(success) {
                configScheduler.save(Element.planLocation, null)
                jsfCommunication.send(JsfEvent.SYNC_PLAN)
                return Response.Feedback(true)
            }
        }
        return Response.Feedback(false)
    }
}