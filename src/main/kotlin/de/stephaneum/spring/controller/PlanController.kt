package de.stephaneum.spring.controller

import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.scheduler.Element
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.encryption.AccessPermission
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import org.springframework.core.io.Resource
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import java.io.ByteArrayOutputStream

@Controller
class PlanController(
    private val fileService: FileService,
    private val configScheduler: ConfigScheduler,
) {

    @GetMapping("/vertretungsplan.pdf")
    fun download(): Any {

        val path = configScheduler.get(Element.planLocation) ?: return "404"
        val resource = fileService.loadFileAsResource(path) ?: return "404"
        val password = configScheduler.get(Element.planPassword)

        return if (password == null) {
            // return the pdf as is
            ResponseEntity.ok()
                .contentLength(resource.contentLength())
                .contentType(MediaType.parseMediaType(fileService.getMimeFromPath(path)))
                .header("Content-Disposition", "inline; filename=\"vertretungsplan.pdf\"")
                .body(resource)
        } else {
            val bytes = encryptPdf(resource, password)
            ResponseEntity.ok()
                .contentLength(bytes.size.toLong())
                .contentType(MediaType.parseMediaType(fileService.getMimeFromPath(path)))
                .header("Content-Disposition", "inline; filename=\"vertretungsplan.pdf\"")
                .body(bytes)
        }
    }

    private fun encryptPdf(pdfResource: Resource, password: String): ByteArray {
        val bytes = pdfResource.inputStream.readAllBytes()

        val doc: PDDocument = PDDocument.load(bytes)
        val keyLength = 256

        // The owner and the user are the same person, we don't differentiate it here
        val spp = StandardProtectionPolicy(password, password, AccessPermission())
        spp.encryptionKeyLength = keyLength
        doc.protect(spp)

        val outStream = ByteArrayOutputStream()
        doc.save(outStream)
        doc.close()

        return outStream.toByteArray()
    }
}
