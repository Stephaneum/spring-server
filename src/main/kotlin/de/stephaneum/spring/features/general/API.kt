package de.stephaneum.spring.features.general

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.helper.MenuService
import de.stephaneum.spring.scheduler.Element
import de.stephaneum.spring.scheduler.ConfigFetcher
import de.stephaneum.spring.security.CryptoService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api")
class API {

    private val PEPPER = "A43w8pa0M245qga4293zt9o4mc3z98TA3nQ9mzvTa943cta43mTaoz47tz3loIhbiKh"

    @Autowired
    private lateinit var configFetcher: ConfigFetcher

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var menuService: MenuService

    @Autowired
    private lateinit var cryptoService: CryptoService

    @Autowired
    private lateinit var postRepo: PostRepo

    @Autowired
    private lateinit var userRepo: UserRepo

    @GetMapping("/info")
    fun get(): Response.Info {
        val user = Session.get().user ?: EMPTY_USER
        val copyright = configFetcher.get(Element.copyright)
        val plan = Response.Plan(configFetcher.get(Element.planLocation) != null, configFetcher.get(Element.planInfo))
        val unapproved = when {
            user.code.role == ROLE_ADMIN || user.managePosts == true    -> postRepo.countByApproved(false)
            user.code.role != ROLE_NO_LOGIN                             -> postRepo.countByApprovedAndUser(false, user)
            else                                                        -> null
        }
        return Response.Info(user, menuService.getPublic(), copyright, plan, unapproved)
    }

    @ExperimentalUnsignedTypes
    @PostMapping("/login")
    fun login(@RequestBody request: Request.Login): Response.Feedback {
        val user = userRepo.findByEmail(request.email ?: "") ?: return Response.Feedback(false)
        val salt = user.password.substring(32)
        if(user.password == cryptoService.hashMD5(request.password+salt+PEPPER)+salt) {
            Session.get().user = user;
            return Response.Feedback(true)
        } else {
            Thread.sleep(2000)
            return Response.Feedback(false)
        }
    }

    @PostMapping("/logout")
    fun logout() {
        Session.logout()
    }

    @GetMapping("/images/{fileName}")
    fun image(@PathVariable fileName: String, request: HttpServletRequest): Any {

        // get file content
        val resource = configFetcher.get(Element.fileLocation)?.let { location ->
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