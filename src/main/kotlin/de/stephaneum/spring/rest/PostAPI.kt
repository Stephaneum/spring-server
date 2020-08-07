package de.stephaneum.spring.rest

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.*
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.scheduler.Element
import de.stephaneum.spring.security.CryptoService
import org.jsoup.Jsoup
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths

object PostRequest {
    data class UpdatePost(val id: Int?,
                          val title: String?,
                          val text: String?,
                          val images: List<File>,
                          val layoutPost: Int,
                          val layoutPreview: Int,
                          val preview: Int,
                          val menuID: Int?)

    data class UpdatePostPassword(val postID: Int,
                                  val password: String?)

    data class UpdateSpecial(val type: String, val text: String?)
}

object PostResponse {
    data class Feedback(val success: Boolean, val needLogin: Boolean = false, val message: String? = null)

    data class PostManager(val maxPictureSize: Int, val menuAdmin: Boolean, val writableMenu: List<Menu>)
    data class Text(val text: String?)
}

@RestController
@RequestMapping("/api/post")
class PostAPI (
        private val postService: PostService,
        private val cryptoService: CryptoService,
        private val imageService: ImageService,
        private val fileService: FileService,
        private val menuService: MenuService,
        private val configScheduler: ConfigScheduler,
        private val logService: LogService,
        private val menuRepo: MenuRepo,
        private val fileRepo: FileRepo,
        private val postRepo: PostRepo,
        private val filePostRepo: FilePostRepo
) {

    @GetMapping
    fun get(@RequestParam(required = false) unapproved: Boolean?,
            @RequestParam(required = false) postID: Int?,
            @RequestParam(required = false) menuID: Int?,
            @RequestParam(required = false) noContent: Boolean?): Any {
        when {
            postID != null -> {
                // single post
                val (session, _) = Session.createIfNotExists()
                val me = session.user
                val post = postRepo.findByIdOrNull(postID) ?: throw ErrorCode(404, "post not found")

                post.simplify()
                post.menu?.simplify()
                post.images = filePostRepo.findByPostId(post.id).map { it.file.apply { simplifyForPosts() } }
                if(noContent == true)
                    post.content = null

                if(post.password != null && !((me != null && hasAccessToPost(me, post)) || Session.hasAccess(post)))
                    throw ErrorCode(403, "no access")

                post.password = null
                return post
            }
            unapproved == true || menuID != null -> {
                // multiple posts
                val user = Session.get().user ?: User()
                return when {
                    menuID != null -> postService.getPosts(menuID, noContent == true) // get posts from a menu
                    user.code.role == ROLE_ADMIN || menuService.isMenuAdmin(user) -> postService.getAllUnapprovedPosts(noContent == true) // get all unapproved posts
                    else -> postService.getUnapprovedPosts(user, noContent = true) // get only unapproved posts from the current user
                }
            }
            else -> return PostResponse.Feedback(false, message = "Empty request body")
        }
    }

    @PostMapping
    fun update(@RequestBody request: PostRequest.UpdatePost): Post {

        val user = Session.getUser()
        val oldPost: Post?
        if(request.id != null) {
            // update post -> check
            // we also create a copy because Repository.save() does modify the old reference
            oldPost = postRepo.findByIdOrNull(request.id)?.copy() ?: throw ErrorCode(404, "post not found")
            if(!hasAccessToPost(user, oldPost))
                throw ErrorCode(403, "You are not allowed to modify this post.")
        } else {
            oldPost = null
        }

        val menu = if(request.menuID != null) menuRepo.findByIdOrNull(request.menuID) else null

        // validate menu
        if(menu != null) {
            val allowed = user.code.role == ROLE_ADMIN || menuService.canWrite(user, menu)
            if(!allowed)
                throw ErrorCode(403, "You are not allowed to specify a menu")
        }

        // validate title
        if(request.title.isNullOrBlank())
            throw ErrorCode(400, "Empty title")

        // validate assignment
        if((user.code.role == ROLE_ADMIN || menuService.isMenuAdmin(user)) && request.menuID == null)
            throw ErrorCode(400, "missing assignment")

        // save the post
        val text = if(request.text != null && (request.text.isBlank() || Jsoup.parse(request.text).text().isBlank())) null else request.text
        val savedPost = postRepo.save(Post(request.id ?: 0, oldPost?.user ?: user, menu, request.title, text, now(), if(oldPost != null) user else null, null, request.menuID != null, request.preview, request.layoutPost, request.layoutPreview))

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
            oldPost == null -> EventType.CREATE_POST
            oldPost.menu == null && menu != null -> EventType.APPROVE_POST
            else -> EventType.EDIT_POST
        }
        logService.log(eventType, user, request.title)
        return savedPost
    }

    @ExperimentalUnsignedTypes
    @PostMapping("/update-password")
    fun updatePassword(@RequestBody request: PostRequest.UpdatePostPassword): PostResponse.Feedback {
        val user = Session.get().user ?: return PostResponse.Feedback(false, needLogin = true)
        val post = postRepo.findByIdOrNull(request.postID) ?: return PostResponse.Feedback(false, message = "post not found")

        if(hasAccessToPost(user, post)) {
            val password = if(request.password.isNullOrBlank()) null else cryptoService.hashMD5(request.password)
            post.password = password
            postRepo.save(post)
            return PostResponse.Feedback(true)
        } else {
            return PostResponse.Feedback(false, message = "not allowed")
        }
    }

    @PostMapping("/delete/{postID}")
    fun delete(@PathVariable postID: Int): PostResponse.Feedback {
        val user = Session.get().user ?: return PostResponse.Feedback(false, needLogin = true)
        val post = postRepo.findByIdOrNull(postID) ?: return PostResponse.Feedback(false, message = "post not found")

        if(hasAccessToPost(user, post)) {
            postRepo.deleteById(postID)
            logService.log(EventType.DELETE_POST, user, post.title)
            return PostResponse.Feedback(true)
        } else {
            return PostResponse.Feedback(false, message = "not allowed")
        }
    }

    @GetMapping("/info-post-manager")
    fun infoPostManager(): Any {
        val user = Session.get().user ?: return PostResponse.Feedback(false, needLogin = true)

        return PostResponse.PostManager(configScheduler.get(Element.maxPictureSize)?.toInt()
                ?: 0, menuService.isMenuAdmin(user), menuService.getWritable(user))
    }

    @GetMapping("/images-available")
    fun getImages(): Any {

        val user = Session.get().user ?: return PostResponse.Feedback(false, needLogin = true)
        val files = fileRepo.findMyImages(user.id, "image")
        files.forEach { f -> f.simplifyForPosts() }

        return files
    }

    @PostMapping("/upload-image")
    fun uploadImage(@RequestParam("file") file: MultipartFile): Any {

        val user = Session.get().user ?: return PostResponse.Feedback(false, needLogin = true)
        var fileName = file.originalFilename ?: return PostResponse.Feedback(false, message = "Dateiname unbekannt")
        var contentType = file.contentType ?: return PostResponse.Feedback(false, message = "Dateityp unbekannt")

        if(!contentType.startsWith("image"))
            return PostResponse.Feedback(false, message = "Nur Bilder erlaubt")

        // ensure that the image is rotated properly
        val bytes = imageService.digestImageRotation(file.bytes)
        if(bytes != null) {
            fileName = fileService.getPathWithNewExtension(fileName, "jpg")
            contentType = Files.probeContentType(Paths.get(fileName))
        }

        val result = fileService.storeFileStephaneum(user, fileName, contentType, bytes ?: file.bytes, "Beiträge", null)
        result.simplifyForPosts()

        return result
    }

    private fun hasAccessToPost(user: User, post: Post): Boolean {
        val menu = post.menu
        if(menu != null) {
            // assigned
            return user.code.role == ROLE_ADMIN || menuService.canWrite(user, menu)
        } else {
            // unapproved
            return user.code.role == ROLE_ADMIN || menuService.isMenuAdmin(user) || user.id == (post.user?.id ?: 0)
        }
    }

    // special

    @GetMapping("/special")
    fun getSpecial(@RequestParam type: String): Any {

        val user = Session.get().user ?: return PostResponse.Feedback(false, needLogin = true)
        if(user.code.role == ROLE_ADMIN || menuService.isMenuAdmin(user)) {
            return PostResponse.Text(configScheduler.get(Element.valueOf(type)))
        } else {
            return PostResponse.Feedback(false, message = "only admin or post manager")
        }
    }

    @PostMapping("/special")
    fun updateSpecial(@RequestBody request: PostRequest.UpdateSpecial): Any {

        val user = Session.get().user ?: return PostResponse.Feedback(false, needLogin = true)
        if(user.code.role == ROLE_ADMIN || menuService.isMenuAdmin(user)) {
            val type = Element.valueOf(request.type)
            val plainText = Jsoup.parse(request.text ?: "").text()
            val finalText = when(type) {
                Element.events, Element.coop, Element.coopURL, Element.liveticker, Element.contact, Element.imprint, Element.copyright, Element.dev -> request.text
                Element.history, Element.euSa -> {
                    if(plainText.startsWith("http")) {
                        plainText.replace("\u00A0", "").trim()
                    } else {
                        request.text
                    }
                }
                else -> return PostResponse.Feedback(false, message = "invalid type")
            }
            configScheduler.save(type, finalText)
            logService.log(EventType.EDIT_POST, user, "${type.info} (spezieller Text)")
            return PostResponse.Feedback(true)
        } else {
            return PostResponse.Feedback(false, message = "only admin or post manager")
        }
    }
}