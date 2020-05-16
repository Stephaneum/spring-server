package de.stephaneum.spring.controller

import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.scheduler.Element
import org.springframework.http.*
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class PlanController (
        private val fileService: FileService,
        private val configScheduler: ConfigScheduler
) {

    @GetMapping("/vertretungsplan.pdf")
    fun download(): Any {

        val path = configScheduler.get(Element.planLocation) ?: return "404"
        val resource = fileService.loadFileAsResource(path) ?: return "404"

        return ResponseEntity.ok()
                .contentLength(resource.contentLength())
                .contentType(MediaType.parseMediaType(fileService.getMimeFromPath(path)))
                .header("Content-Disposition", "inline; filename=\"vertretungsplan.pdf\"")
                .body(resource)
    }
}