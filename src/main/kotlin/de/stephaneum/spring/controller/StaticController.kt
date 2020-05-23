package de.stephaneum.spring.controller

import de.stephaneum.spring.database.Static
import de.stephaneum.spring.database.StaticMode
import de.stephaneum.spring.database.StaticRepo
import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.scheduler.Element
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
class StaticController (
        private val fileService: FileService,
        private val configScheduler: ConfigScheduler,
        private val staticRepo: StaticRepo
) {

    @GetMapping("/s/**")
    fun getPublic(@RequestParam(required = false) download: Boolean?, request: HttpServletRequest, response: HttpServletResponse, model: Model): Any? {

        val path = request.requestURI.replace("${request.contextPath}/s/","")
        val mainPath = configScheduler.get(Element.fileLocation) ?: return "500"
        if(path.endsWith(".html") || path.endsWith(".htm")) {

            // html request

            val page = staticRepo.findByPath(path) ?: return "404"

            if(download == true || page.mode == StaticMode.FULL_SCREEN) {
                val content = fileService.loadFileAsString("$mainPath/${Static.FOLDER_NAME}/$path")
                val name = fileService.getFileName(path)
                val disposition = if (download == true) "attachment" else "inline"
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("text/html; charset=utf-8"))
                        .header("Content-Disposition", "$disposition; filename=\"$name\"")
                        .body(content)
            }

            return "forward:/static/index.html" // return vue
        } else {
            // other file types, e.g. images
            val resource = fileService.loadFileAsResource("$mainPath/${Static.FOLDER_NAME}/$path") ?: return "404"
            return ResponseEntity.ok()
                    .contentLength(resource.contentLength())
                    .contentType(MediaType.parseMediaType(fileService.getMimeFromPath(path)))
                    .body(resource)
        }
    }
}