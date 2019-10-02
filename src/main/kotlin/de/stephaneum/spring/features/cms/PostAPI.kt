package de.stephaneum.spring.features.cms

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.helper.MenuService
import de.stephaneum.spring.scheduler.ConfigFetcher
import de.stephaneum.spring.security.CryptoService
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/post")
class PostAPI {

    @Autowired
    private lateinit var cryptoService: CryptoService

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

    @GetMapping
    fun get(@RequestParam(required = false) unapproved: Boolean?,
            @RequestParam(required = false) postID: Int?,
            @RequestParam(required = false) menuID: Int?,
            @RequestParam(required = false) noContent: Boolean?): Any {
        when {
            unapproved == true || menuID != null -> {
                Session.get().user ?: return Response.Feedback(false, needLogin = true)
                val posts = if(menuID != null) postRepo.findByMenuIdOrderByTimestampDesc(menuID) else postRepo.findUnapproved()

                return posts.apply {
                    forEach { p ->
                        p.simplify()
                        p.menu?.simplify()
                        p.images = filePostRepo.findByPostId(p.id).map { it.file.apply { simplifyForPosts() } }
                        if(noContent == true)
                            p.content = null
                    }
                }
            }
            postID != null -> {
                val post = postRepo.findByIdOrNull(postID) ?: return Response.Feedback(false, message = "post not found")
                post.simplify()
                post.menu?.simplify()
                post.images = filePostRepo.findByPostId(post.id).map { it.file.apply { simplifyForPosts() } }
                if(noContent == true)
                    post.content = null
                return post
            }
            else -> return Response.Feedback(false, message = "Empty request body")
        }
    }

    @PostMapping("/update")
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
        val maxPictureSize = configFetcher.maxPictureSize ?: 0
        val imagesFull = fileRepo.findByIdIn(request.images.map { it.id }) // get the full data because it was simplified for security reasons
        imagesFull.forEach { i ->
            if(i.size >= maxPictureSize) {
                postService.compress(i)
            }
        }
        val images = imagesFull.map { i -> FilePost(0, i, savedPost) }
        if(request.id != null)
            filePostRepo.deleteByPostId(request.id) // delete old images
        filePostRepo.saveAll(images)

        // log
        val eventType = when {
            oldPost == null -> EventType.CREATE_POST.id
            oldPost.menu != null -> EventType.EDIT_POST.id
            else -> EventType.APPROVE_POST.id
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
            return Response.Feedback(true)
        } else {
            return Response.Feedback(false, message = "not allowed")
        }
    }

    @GetMapping("/info-post-manager")
    fun infoPostManager(): Any {
        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)

        return Response.PostManager(configFetcher.maxPictureSize ?: 0, menuService.getCategory(user.id))
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
        val fileName = file.originalFilename ?: return Response.Feedback(false, message = "Dateiname unbekannt")
        if(file.contentType == null || !file.contentType!!.startsWith("image"))
            return Response.Feedback(false, message = "Nur Bilder erlaubt")

        val result = fileService.storeFileStephaneum(user, fileName, file.contentType!!, file.bytes, "Beiträge", -1, FileService.StoreMode.PRIVATE)
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
}