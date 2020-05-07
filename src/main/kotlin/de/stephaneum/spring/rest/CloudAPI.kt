package de.stephaneum.spring.rest

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.ErrorCode
import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.helper.ImageService
import de.stephaneum.spring.helper.obj
import de.stephaneum.spring.security.JwtService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import javax.servlet.http.HttpServletRequest

object CloudRequest {
    data class CreateFolder(val name: String?, val parentID: Int?)
    data class UpdatePublic(val fileID: Int?, val isPublic: Boolean?)
    data class MoveFile(val fileID: Int?, val parentFolderID: Int?)
    data class MoveFolder(val folderID: Int?, val parentFolderID: Int?)
}

object CloudResponse {
    data class Feedback(val success: Boolean, val needLogin: Boolean = false, val message: String? = null)
    data class CloudInfo(val used: Int, val total: Int, val count: Int, val private: Int, val project: Int, val schoolClass: Int, val teacherChat: Int)
    data class FileKey(val key: String)
}

@RestController
@RequestMapping("/api/cloud")
class CloudAPI {

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var imageService: ImageService

    @Autowired
    private lateinit var fileRepo: FileRepo

    @Autowired
    private lateinit var folderRepo: FolderRepo

    @Autowired
    private lateinit var groupRepo: GroupRepo

    @Autowired
    private lateinit var classRepo: SchoolClassRepo

    @GetMapping("/info")
    fun getInfo(): CloudResponse.CloudInfo {
        val user = Session.get().user ?: throw ErrorCode(401, "login")

        val used = fileRepo.calcStorageUsed(user.id)
        val total = user.storage
        val count = fileRepo.countByUserId(user.id)
        val privateUsage = fileRepo.calcStorageUsedPrivate(user.id)
        val projectUsage = fileRepo.calcStorageUsedProject(user.id)
        val classUsage = fileRepo.calcStorageUsedClass(user.id)
        val teacherChatUsage = fileRepo.calcStorageUsedTeacherChat(user.id)

        return CloudResponse.CloudInfo(used, total, count, privateUsage, projectUsage, classUsage, teacherChatUsage)
    }

    @GetMapping("/view/user")
    fun getRootViewUser(): List<Any> {
        val user = Session.get().user ?: throw ErrorCode(401, "login")

        val folders = folderRepo.findPrivateFolderInRoot(user)
        val files = fileRepo.findPrivateInRoot(user)

        return digestResults(folders, files)
    }

    @GetMapping("/view/group/{id}")
    fun getRootViewGroup(@PathVariable id: Int): List<Any> {
        Session.get().user ?: throw ErrorCode(401, "login")
        val group = groupRepo.findByIdOrNull(id) ?: throw ErrorCode(404, "group not found")
        val folders = folderRepo.findGroupFolderInRoot(group)
        val files = fileRepo.findGroupInRoot(group)

        return digestResults(folders, files)
    }

    @GetMapping("/view/class/{id}")
    fun getRootViewClass(@PathVariable id: Int): List<Any> {
        Session.get().user ?: throw ErrorCode(401, "login")
        val schoolClass = classRepo.findByIdOrNull(id) ?: throw ErrorCode(404, "class not found")
        val folders = folderRepo.findClassFolderInRoot(schoolClass)
        val files = fileRepo.findClassInRoot(schoolClass)

        return digestResults(folders, files)
    }

    @GetMapping("/view/teacher")
    fun getRootViewTeacher(): List<Any> {
        Session.get().user ?: throw ErrorCode(401, "login")
        val folders = folderRepo.findTeacherFolderInRoot()
        val files = fileRepo.findTeacherInRoot()

        return digestResults(folders, files)
    }

    @GetMapping("/view/{folder}")
    fun getViewSubFolder(@PathVariable folder: Int): List<Any> {
        Session.get().user ?: throw ErrorCode(401, "login")

        val folders = folderRepo.findByParent(folder.obj())
        val files = fileRepo.findByFolderOrderByIdDesc(folder.obj())

        return digestResults(folders, files)
    }

    @PostMapping("/upload/user", "/upload/group/{id}", "/upload/class/{id}", "/upload/teacher", "/upload/category/{id}")
    fun uploadUser(@PathVariable(required = false) id: Int?,
                   @RequestParam("folder") folderID: String?,
                   @RequestParam("file") file: MultipartFile,
                   request: HttpServletRequest): Any {

        val user = Session.get().user ?: return CloudResponse.Feedback(false, needLogin = true)
        var fileName = file.originalFilename ?: return CloudResponse.Feedback(false, message = "Dateiname unbekannt")
        var contentType = file.contentType ?: return CloudResponse.Feedback(false, message = "Dateityp unbekannt")

        // ensure that the image is rotated properly
        var bytes = file.bytes
        if(contentType.startsWith("image")) {
            val rotatedBytes = imageService.digestImageRotation(file.bytes)
            if (rotatedBytes != null) {
                bytes = rotatedBytes
                fileName = fileService.getPathWithNewExtension(fileName, "jpg")
                contentType = Files.probeContentType(Paths.get(fileName))
            }
        }

        val mode = when {
            request.requestURL.endsWith("/upload/user") -> FileService.StoreMode.PRIVATE
            request.requestURL.endsWith("/upload/group/$id") -> FileService.StoreMode.GROUP
            request.requestURL.endsWith("/upload/class/$id") -> FileService.StoreMode.SCHOOL_CLASS
            request.requestURL.endsWith("/upload/teacher") -> FileService.StoreMode.TEACHER_CHAT
            request.requestURL.endsWith("/upload/category/$id") -> FileService.StoreMode.CATEGORY
            else -> throw ErrorCode(500, "Internal Error")
        }

        val digestedFolderID = if(folderID is String) {
            if(folderID == "null") {
                null
            } else {
                folderID.toInt()
            }
        } else {
            folderID?.toIntOrNull()
        }

        val result = fileService.storeFileStephaneum(user, fileName, contentType, bytes, digestedFolderID, id, mode)
        if(result is File)
            result.simplifyForPosts()

        return if(result is String) CloudResponse.Feedback(false, message = result) else result
    }

    @PostMapping("/create-folder/user", "/create-folder/group/{id}", "/create-folder/class/{id}", "/create-folder/teacher")
    fun createFolder(@RequestBody request: CloudRequest.CreateFolder,
                     @PathVariable(required = false) id: Int?,
                     httpRequest: HttpServletRequest) {

        if(request.name.isNullOrBlank())
            throw ErrorCode(400, "missing name")

        val user = Session.get().user ?: throw ErrorCode(401, "login")

        val folder = if(request.parentID != null) {
            folderRepo.findByIdOrNull(request.parentID) ?: throw ErrorCode(404, "folder not found")
        } else {
            null
        }

        val group = if(id != null && httpRequest.requestURL.endsWith("/create-folder/group/$id")) {
            groupRepo.findByIdOrNull(id) ?: throw ErrorCode(404, "group not found")
        } else {
            null
        }

        val schoolClass = if(id != null && httpRequest.requestURL.endsWith("/create-folder/class/$id")) {
            classRepo.findByIdOrNull(id) ?: throw ErrorCode(404, "class not found")
        } else {
            null
        }

        val teacherChat = httpRequest.requestURL.endsWith("/create-folder/teacher")

        folderRepo.save(Folder(0, request.name.trim(), user, group, schoolClass, teacherChat, folder))
    }

    @PostMapping("/update-public-file")
    fun updatePublic(@RequestBody request: CloudRequest.UpdatePublic): CloudResponse.Feedback {

        if(request.fileID == null || request.isPublic == null)
            return CloudResponse.Feedback(false, message = "Missing Arguments")

        val user = Session.get().user ?: return CloudResponse.Feedback(false, needLogin = true)
        val file = fileRepo.findByIdOrNull(request.fileID) ?: return CloudResponse.Feedback(false, message = "file not found")

        if(!fileService.hasAccessToFile(user, file))
            return CloudResponse.Feedback(false, message = "no access to this file")

        file.public = request.isPublic
        fileRepo.save(file)

        return CloudResponse.Feedback(true)
    }

    @PostMapping("/delete-file/{fileID}")
    fun deleteFile(@PathVariable fileID: Int): CloudResponse.Feedback {

        val user = Session.get().user ?: return CloudResponse.Feedback(false, needLogin = true)
        val file = fileRepo.findByIdOrNull(fileID) ?: return CloudResponse.Feedback(false, message = "file not found")

        if(!fileService.hasAccessToFile(user, file))
            return CloudResponse.Feedback(false, message = "no access to this file")

        fileService.deleteFileStephaneum(user, file)

        return CloudResponse.Feedback(true)
    }

    @PostMapping("/delete-folder/{folderID}")
    fun deleteFolder(@PathVariable folderID: Int): CloudResponse.Feedback {

        val user = Session.get().user ?: return CloudResponse.Feedback(false, needLogin = true)
        val folder = folderRepo.findByIdOrNull(folderID) ?: return CloudResponse.Feedback(false, message = "folder not found")

        // TODO: check permissions

        fileService.deleteFolderStephaneum(user, folder)

        return CloudResponse.Feedback(true)
    }

    @PostMapping("/move-file")
    fun moveFile(@RequestBody request: CloudRequest.MoveFile): CloudResponse.Feedback {

        if(request.fileID == null || request.parentFolderID == null)
            return CloudResponse.Feedback(false, message = "Missing Arguments")

        val user = Session.get().user ?: return CloudResponse.Feedback(false, needLogin = true)
        val file = fileRepo.findByIdOrNull(request.fileID) ?: return CloudResponse.Feedback(false, message = "file not found")
        val folder = folderRepo.findByIdOrNull(request.parentFolderID) ?: return CloudResponse.Feedback(false, message = "file not found")

        if(!fileService.hasAccessToFile(user, file))
            return CloudResponse.Feedback(false, message = "no access to this file")

        file.folder = folder
        fileRepo.save(file)

        return CloudResponse.Feedback(true)
    }

    @GetMapping("/key/{fileID}")
    fun getKey(@PathVariable fileID: Int): Any {

        val user = Session.get().user ?: return CloudResponse.Feedback(false, needLogin = true)
        val file = fileRepo.findByIdOrNull(fileID) ?: return CloudResponse.Feedback(false, message = "file not found")

        if(!fileService.hasAccessToFile(user, file))
            return CloudResponse.Feedback(false, message = "no access to this file")

        return CloudResponse.FileKey(jwtService.generateToken(mapOf("fileID" to fileID.toString())))
    }

    private fun calcSizeRecursive(folder: Folder): Int {
        val fileSize = fileRepo.findByFolderOrderByIdDesc(folder).sumBy { it.size }
        folder.size = fileSize
        folderRepo.findByParent(folder).forEach { f ->
            folder.size += calcSizeRecursive(f)
        }
        return folder.size
    }

    private fun digestResults(folders: List<Folder>, files: List<File>): List<Any> {
        for (f in folders) {
            f.simplifyForCloud()
            calcSizeRecursive(f)
        }
        files.forEach { f -> f.simplifyForCloud() }

        return List<Any>(folders.size + files.size) { i ->
            if(i < folders.size)
                folders[i]
            else
                files[i-folders.size]
        }
    }

}