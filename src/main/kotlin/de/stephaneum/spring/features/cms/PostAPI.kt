package de.stephaneum.spring.features.cms

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.helper.MenuService
import de.stephaneum.spring.scheduler.ConfigFetcher
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/post")
class PostAPI {

    @Autowired
    private lateinit var fileRepo: FileRepo

    @Autowired
    private lateinit var postRepo: PostRepo

    @Autowired
    private lateinit var postService: PostService

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var configFetcher: ConfigFetcher

    @Autowired
    private lateinit var menuService: MenuService

    @Autowired
    private lateinit var filePostRepo: FilePostRepo

    @Autowired
    private lateinit var logRepo: LogRepo

    @Autowired
    private lateinit var menuRepo: MenuRepo

    @PostMapping("/create")
    fun create(@RequestBody request: Request.CreatePost): Any {

        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)

        println(request)
        // validate menuID
        if(request.menuID != null) {
            val allowed = user.code.role == ROLE_ADMIN || user.createPosts == true || (user.createCategories == true && menuService.ownsCategory(user.id, request.menuID))
            if(!allowed)
                return Response.Feedback(false, message = "You are not allowed to specify a menu")
        }

        // validate title
        if(request.title.isNullOrBlank())
            return Response.Feedback(false, message = "Empty title.")

        // validate assignment
        if((user.code.role == ROLE_ADMIN || user.createPosts == true) && request.menuID == null)
            return Response.Feedback(false, message = "Missing assignment.")

        // save the post
        val menu = if(request.menuID != null) Menu(request.menuID) else null
        val text = if(request.text != null && (request.text.isBlank() || Jsoup.parse(request.text).text().isBlank())) null else request.text
        val savedPost = postRepo.save(Post(0, user, menu, request.title, text, now(), null, null, request.menuID != null, request.preview, false, request.layoutPost, request.layoutPreview))

        // compress images
        val maxPictureSize = configFetcher.maxPictureSize ?: 0
        val imagesFull = fileRepo.findByIdIn(request.images.map { it.id }) // get the full data because it was simplified for security reasons
        imagesFull.forEach { i ->
            if(i.size >= maxPictureSize) {
                postService.compress(i)
            }
        }
        val images = imagesFull.map { i -> FilePost(0, i, savedPost) }
        filePostRepo.saveAll(images)

        // log
        logRepo.save(Log(0, now(), EventType.CREATE_POST.id, "${user.firstName} ${user.lastName} (${user.code.getRoleString()}), ${request.title}"))
        return savedPost
    }

    @GetMapping("/info-post-manager")
    fun infoPostManager(): Any {
        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)
        val hasCategory = user.createCategories == true && menuRepo.findCategory(user.id) != null

        return Response.PostManager(configFetcher.maxPictureSize ?: 0, hasCategory)
    }

    @GetMapping("/images-available")
    fun get(): Any {

        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)
        val files = fileRepo.findMyImages(user.id, "image")
        digestFiles(files)

        return files
    }

    @PostMapping("/upload-image")
    fun uploadImage(@RequestParam("file") file: MultipartFile): Any {

        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)
        val fileName = file.originalFilename ?: return Response.Feedback(false, message = "Dateiname unbekannt")
        if(file.contentType == null || !file.contentType!!.startsWith("image"))
            return Response.Feedback(false, message = "Nur Bilder erlaubt")

        val result = fileService.storeFileStephaneum(user, fileName, file.contentType!!, file.bytes, "Beiträge", -1, FileService.StoreMode.PRIVATE)
        if(result is File)
            digestFiles(listOf(result))

        return if(result is String) Response.Feedback(false, message = result) else result
    }

    fun digestFiles(files: List<File>) {
        files.forEach { file ->
            file.fileNameWithID = file.path.substring(file.path.lastIndexOf('/') + 1)
            file.path = ""
            file.user = EMPTY_USER
            file.folder = null
        }
    }
}