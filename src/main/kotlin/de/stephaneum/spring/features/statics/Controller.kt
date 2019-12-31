package de.stephaneum.spring.features.statics

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.Static
import de.stephaneum.spring.database.StaticMode
import de.stephaneum.spring.database.StaticRepo
import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.helper.checkIE
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.scheduler.Element
import de.stephaneum.spring.security.JwtService
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
class StaticController {

    @Autowired
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var configScheduler: ConfigScheduler

    @Autowired
    private lateinit var staticRepo: StaticRepo

    @GetMapping("/s/**")
    fun getPublic(@RequestParam(required = false) key: String?, request: HttpServletRequest, response: HttpServletResponse, model: Model): Any? {

        if(checkIE(request))
            return "forward:/static/no-support-ie.html"

        val path = request.requestURI.replace("/s/","")
        val mainPath = configScheduler.get(Element.fileLocation) ?: return "500"
        if(path.endsWith(".html") || path.endsWith(".htm")) {

            // html request

            val page = staticRepo.findByPath(path)
            val content = fileService.loadFileAsString("$mainPath/${Static.FOLDER_NAME}/$path")

            if(page != null && content != null) {
                val doc = Jsoup.parse(content)
                model["head"] = doc.select("head").first().html()
                model["body"] = doc.select("body").first().html()
                model["title"] = doc.select("title").first()?.html() ?: "Beitrag"

                return when(page.mode) {
                    StaticMode.MIDDLE -> "static-middle"
                    StaticMode.FULL_WIDTH -> "static-full-width"
                    StaticMode.FULL_SCREEN -> {
                        // just return the html file as is
                        response.contentType = "text/html"
                        response.characterEncoding = "UTF-8"
                        response.writer.println(content)
                        null
                    }
                }
            } else {
                return "404"
            }
        } else {
            // other file types, e.g. images
            val resource = fileService.loadFileAsResource("$mainPath/${Static.FOLDER_NAME}/$path") ?: return "404"
            return ResponseEntity.ok()
                    .contentLength(resource.contentLength())
                    .contentType(MediaType.parseMediaType(fileService.getMimeFromPath(path)))
                    .body(resource)
        }
    }

    @GetMapping("admin-static")
    fun getAdmin(@RequestParam(required = false) key: String?, request: HttpServletRequest): String {

        if(checkIE(request))
            return "forward:/static/no-support-ie.html"

        // login
        if(key != null) {
            Session.get().user = jwtService.getUser(key)
            if(Session.get().user != null) {
                return "redirect:static-manager"
            }
        }

        return "admin-static"
    }
}