package de.stephaneum.spring.rest

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.EMPTY_USER
import de.stephaneum.spring.database.PostRepo
import de.stephaneum.spring.database.ROLE_ADMIN
import de.stephaneum.spring.database.ROLE_NO_LOGIN
import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.helper.MenuService
import de.stephaneum.spring.rest.objects.Response
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.scheduler.Element
import org.springframework.http.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api")
class PublicAPI (
        private val configScheduler: ConfigScheduler,
        private val menuService: MenuService,
        private val fileService: FileService,
        private val postRepo: PostRepo
) {

    @GetMapping("/info")
    fun get(): Response.Info {
        val user = Session.get().user ?: EMPTY_USER
        val copyright = configScheduler.get(Element.copyright)
        val plan = Response.Plan(configScheduler.get(Element.planLocation) != null, configScheduler.get(Element.planInfo))
        val history = configScheduler.get(Element.history)
        val euSa = configScheduler.get(Element.euSa)
        val unapproved = when {
            user.code.role == ROLE_ADMIN || user.managePosts == true    -> postRepo.countByApproved(false)
            user.code.role != ROLE_NO_LOGIN                             -> postRepo.countByApprovedAndUser(false, user)
            else                                                        -> null
        }
        return Response.Info(user, menuService.getPublic(), copyright, plan, history, euSa, unapproved)
    }

    @GetMapping("/images/{fileName}")
    fun image(@PathVariable fileName: String, request: HttpServletRequest): Any {

        // get file content
        val resource = configScheduler.get(Element.fileLocation)?.let { location ->
            fileService.loadFileAsResource("$location/$fileName")
        } ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).build<Void>()

        // check 304 not modified
        val lastModified = (resource.lastModified() / 1000) * 1000 // ignore millis
        if(lastModified <= request.getDateHeader("If-Modified-Since")) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(HttpHeaders().apply {
                setDate("Last-Modified", lastModified)
            }).build<Void>()
        }

        // mime
        val mime = fileService.getMime(fileName.substring(fileName.lastIndexOf('.')+1).toLowerCase())
        if(!fileService.isImage(mime)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build<Void>()

        // success request
        return ResponseEntity.ok()
                .headers(HttpHeaders().apply {
                    setDate("Last-Modified", lastModified)
                })
                .cacheControl(CacheControl.empty().cachePublic())
                .contentLength(resource.contentLength())
                .contentType(MediaType.parseMediaType(mime))
                .body(resource)
    }
}