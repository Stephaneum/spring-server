package de.stephaneum.spring.features.general

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.EMPTY_USER
import de.stephaneum.spring.database.PostRepo
import de.stephaneum.spring.database.ROLE_ADMIN
import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.scheduler.ConfigFetcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api")
class API {

    @Autowired
    private lateinit var configFetcher: ConfigFetcher

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var postRepo: PostRepo

    @GetMapping("/info")
    fun get(): Info {
        val user = Session.get().user ?: EMPTY_USER
        val copyright = configFetcher.copyright
        val plan = Plan(configFetcher.planLocation != null, configFetcher.planInfo)
        val unapproved = if(user.code.role == ROLE_ADMIN || user.managePosts == true) postRepo.countByApproved(false) else null
        return Info(user, copyright, plan, unapproved)
    }

    @GetMapping("/images/{fileName}")
    fun image(@PathVariable fileName: String, request: HttpServletRequest): Any {

        // get file content
        val resource = configFetcher.fileLocation?.let { location ->
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