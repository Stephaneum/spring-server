package de.stephaneum.spring.features.cloud

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.FileRepo
import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.helper.checkIE
import de.stephaneum.spring.security.JwtService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import javax.servlet.http.HttpServletRequest

@Controller
class UserCloudController {

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var fileRepo: FileRepo

    @GetMapping("/cloud")
    fun get(@RequestParam(required = false) key: String?, request: HttpServletRequest): String {

        if(checkIE(request))
            return "forward:/static/no-support-ie.html"

        // login
        if(key != null) {
            Session.get().user = jwtService.getUser(key)
            if(Session.get().user != null) {
                return "redirect:cloud"
            }
        }

        return "user-cloud"
    }

    @GetMapping("/api/cloud/download/file/{fileID}")
    fun download(@PathVariable fileID: Int): Any {
        // TODO: check if this file is accessible from the user
        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)
        val file = fileRepo.findByIdOrNull(fileID) ?: return "404"
        val resource = fileService.loadFileAsResource(file.path) ?: return "404"
        return ResponseEntity.ok()
                .contentLength(resource.contentLength())
                .contentType(MediaType.parseMediaType(fileService.getMimeFromPath(file.path)))
                .header("Content-Disposition", "attachment; filename=\"" + file.generateFileName() + "\"")
                .body(resource)
    }

}