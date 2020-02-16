package de.stephaneum.spring.features.cms

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.features.jsf.JsfCommunication
import de.stephaneum.spring.features.jsf.JsfEvent
import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.helper.ImageService
import de.stephaneum.spring.helper.MenuService
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.scheduler.Element
import de.stephaneum.spring.security.CryptoService
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths


@RestController
@RequestMapping("/api/post")
class PostAPI {

    @Autowired
    private lateinit var jsfCommunication: JsfCommunication

    @Autowired
    private lateinit var cryptoService: CryptoService

    @Autowired
    private lateinit var fileRepo: FileRepo

    @Autowired
    private lateinit var postRepo: PostRepo

    @Autowired
    private lateinit var imageService: ImageService

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var configScheduler: ConfigScheduler

    @Autowired
    private lateinit var menuService: MenuService

    @Autowired
    private lateinit var filePostRepo: FilePostRepo

    @Autowired
    private lateinit var logRepo: LogRepo

    @GetMapping
    fun get(@RequestParam(required = false) unapproved: Boolean?,
            @RequestParam(required = false) postID: Int?,
            @RequestParam(required = false) menuID: Int?,
            @RequestParam(required = false) noContent: Boolean?): Any {
        when {
            postID != null -> {
                // single post
                val post = postRepo.findByIdOrNull(postID) ?: return Response.Feedback(false, message = "post not found")
                post.simplify()
                post.menu?.simplify()
                post.images = filePostRepo.findByPostId(post.id).map { it.file.apply { simplifyForPosts() } }
                if(noContent == true)
                    post.content = null
                return post
            }
            unapproved == true || menuID != null -> {
                // multiple posts
                val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)
                val posts = when {
                    menuID != null -> postRepo.findByMenuIdOrderByTimestampDesc(menuID) // get posts from a menu
                    user.code.role == ROLE_ADMIN || user.managePosts == true -> postRepo.findUnapproved() // get all unapproved posts
                    else -> postRepo.findUnapproved(user.id) // get only unapproved posts from the current user
                }

                if(posts.isEmpty())
                    return emptyList<Post>()

                val images = filePostRepo.findImagesByPostIdIn(posts.map { it.id })
                return posts.apply {
                    forEach { p ->
                        p.simplify()
                        p.menu?.simplify()
                        p.images = images.filter { it.postID == p.id }.map { it.file.apply { simplifyForPosts() } }
                        if(noContent == true)
                            p.content = null
                    }
                }
            }
            else -> return Response.Feedback(false, message = "Empty request body")
        }
    }

    @PostMapping
    fun update(@RequestBody request: Request.UpdatePost): Any {

        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)
        val oldPost: Post?
        if(request.id != null) {
            // update post -> check
            // we also create a copy because Repository.save() does modify the old reference
            oldPost = postRepo.findByIdOrNull(request.id)?.copy() ?: return Response.Feedback(false, message = "post not found")
            if(!hasAccessToPost(user, oldPost))
                return Response.Feedback(false, message = "You are not allowed to modify this post.")
        } else {
            oldPost = null
        }

        // validate menuID
        if(request.menuID != null) {
            val allowed = user.code.role == ROLE_ADMIN || user.managePosts == true || (user.createCategories == true && menuService.ownsCategory(user.id, request.menuID))
            if(!allowed)
                return Response.Feedback(false, message = "You are not allowed to specify a menu")
        }

        // validate title
        if(request.title.isNullOrBlank())
            return Response.Feedback(false, message = "Empty title.")

        // validate assignment
        if((user.code.role == ROLE_ADMIN || user.managePosts == true) && request.menuID == null)
            return Response.Feedback(false, message = "Missing assignment.")

        // save the post
        val menu = if(request.menuID != null) Menu(request.menuID) else null
        val text = if(request.text != null && (request.text.isBlank() || Jsoup.parse(request.text).text().isBlank())) null else request.text
        val savedPost = postRepo.save(Post(request.id ?: 0, oldPost?.user ?: user, menu, request.title, text, now(), if(oldPost != null) user else null, null, request.menuID != null, request.preview, false, request.layoutPost, request.layoutPreview))

        // compress images
        val maxPictureSize = configScheduler.get(Element.maxPictureSize)?.toInt() ?: 0
        val imagesFull = fileRepo.findByIdIn(request.images.map { it.id }) // get the full data because it was simplified for security reasons
        imagesFull.forEach { i ->
            if(i.size >= maxPictureSize) {
                imageService.reduceSizeOfFile(i)
            }
        }
        val images = imagesFull.map { i -> FilePost(0, i, savedPost) }
        if(request.id != null)
            filePostRepo.deleteByPostId(request.id) // delete old images
        filePostRepo.saveAll(images)

        // log
        val eventType = when {
            oldPost == null -> EventType.CREATE_POST.id
            oldPost.menu == null && menu != null -> EventType.APPROVE_POST.id
            else -> EventType.EDIT_POST.id
        }
        logRepo.save(Log(0, now(), eventType, "${user.firstName} ${user.lastName} (${user.code.getRoleString()}), ${request.title}"))
        return savedPost
    }

    @ExperimentalUnsignedTypes
    @PostMapping("/update-password")
    fun updatePassword(@RequestBody request: Request.UpdatePostPassword): Response.Feedback {
        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)
        val post = postRepo.findByIdOrNull(request.postID) ?: return Response.Feedback(false, message = "post not found")

        if(hasAccessToPost(user, post)) {
            val password = if(request.password.isNullOrBlank()) null else cryptoService.hashMD5(request.password)
            post.password = password
            postRepo.save(post)
            return Response.Feedback(true)
        } else {
            return Response.Feedback(false, message = "not allowed")
        }
    }

    @PostMapping("/delete/{postID}")
    fun delete(@PathVariable postID: Int): Response.Feedback {
        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)
        val post = postRepo.findByIdOrNull(postID) ?: return Response.Feedback(false, message = "post not found")

        if(hasAccessToPost(user, post)) {
            postRepo.deleteById(postID)
            logRepo.save(Log(0, now(), EventType.DELETE_POST.id, "${user.firstName} ${user.lastName} (${user.code.getRoleString()}), ${post.title}"))
            return Response.Feedback(true)
        } else {
            return Response.Feedback(false, message = "not allowed")
        }
    }

    @GetMapping("/info-post-manager")
    fun infoPostManager(): Any {
        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)

        return Response.PostManager(configScheduler.get(Element.maxPictureSize)?.toInt() ?: 0, menuService.getCategory(user.id))
    }

    @GetMapping("/images-available")
    fun getImages(): Any {

        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)
        val files = fileRepo.findMyImages(user.id, "image")
        files.forEach { f -> f.simplifyForPosts() }

        return files
    }

    @PostMapping("/upload-image")
    fun uploadImage(@RequestParam("file") file: MultipartFile): Any {

        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)
        var fileName = file.originalFilename ?: return Response.Feedback(false, message = "Dateiname unbekannt")
        var contentType = file.contentType ?: return Response.Feedback(false, message = "Dateityp unbekannt")

        if(!contentType.startsWith("image"))
            return Response.Feedback(false, message = "Nur Bilder erlaubt")

        // ensure that the image is rotated properly
        val bytes = imageService.digestImageRotation(file.bytes)
        if(bytes != null) {
            fileName = fileService.getPathWithNewExtension(fileName, "jpg")
            contentType = Files.probeContentType(Paths.get(fileName))
        }

        val result = fileService.storeFileStephaneum(user, fileName, contentType, bytes ?: file.bytes, "Beiträge", -1, FileService.StoreMode.PRIVATE)
        if(result is File)
            result.simplifyForPosts()

        return if(result is String) Response.Feedback(false, message = result) else result
    }

    private fun hasAccessToPost(user: User, post: Post): Boolean {
        if(post.menu != null) {
            // assigned
            val menuID = post.menu?.id ?: return false
            return user.code.role == ROLE_ADMIN || user.managePosts == true || (user.createCategories == true && menuService.ownsCategory(user.id, menuID))
        } else {
            // unapproved
            return user.code.role == ROLE_ADMIN || user.managePosts == true || user.id == (post.user?.id ?: 0)
        }
    }

    // special

    @GetMapping("/special")
    fun getSpecial(@RequestParam type: String): Any {

        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)
        if(user.code.role == ROLE_ADMIN || user.managePosts == true) {
            return Response.Text(configScheduler.get(Element.valueOf(type)))
        } else {
            return Response.Feedback(false, message = "only admin or post manager")
        }
    }

    @PostMapping("/special")
    fun updateSpecial(@RequestBody request: Request.UpdateSpecial): Any {

        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)
        if(user.code.role == ROLE_ADMIN || user.managePosts == true) {
            val type = Element.valueOf(request.type)
            val plainText = Jsoup.parse(request.text ?: "").text()
            val finalText = when(type) {
                Element.liveticker, Element.coop, Element.coopURL -> plainText
                Element.contact, Element.imprint, Element.copyright, Element.dev -> request.text
                Element.history, Element.euSa -> {
                    if(plainText.startsWith("http")) {
                        plainText.replace("\u00A0", "").trim();
                    } else {
                        request.text
                    }
                }
                Element.events -> request.text?.replace("&nbsp;", " ")?.replace("&quot;", "\"")?.replace("&amp;", "&")
                else -> return Response.Feedback(false, message = "invalid type")
            }
            configScheduler.save(type, finalText)
            logRepo.save(Log(0, now(), EventType.EDIT_POST.id, "${user.firstName} ${user.lastName} (${user.code.getRoleString()}), ${type.info} (spezieller Text)"))
            jsfCommunication.send(JsfEvent.SYNC_SPECIAL_TEXT)
            return Response.Feedback(true)
        } else {
            return Response.Feedback(false, message = "only admin or post manager")
        }
    }
}