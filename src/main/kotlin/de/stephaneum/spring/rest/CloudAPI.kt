package de.stephaneum.spring.rest

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.ErrorCode
import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.helper.ImageService
import de.stephaneum.spring.helper.obj
import de.stephaneum.spring.security.JwtService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Paths
import javax.servlet.http.HttpServletRequest

object CloudRequest {
    data class CreateFolder(val name: String?, val parentID: Int?)
    data class UpdatePublic(val fileID: Int?, val isPublic: Boolean?)
    data class Move(val fileId: Int?, val folderId: Int?, val targetFolderId: Int?)
}

object CloudResponse {
    data class CloudInfo(val used: Int, val total: Int, val count: Int, val private: Int, val project: Int)
    data class FileKey(val key: String)
}

@RestController
@RequestMapping("/api/cloud")
class CloudAPI (
        private val fileService: FileService,
        private val jwtService: JwtService,
        private val imageService: ImageService,
        private val fileRepo: FileRepo,
        private val folderRepo: FolderRepo,
        private val groupRepo: GroupRepo
) {

    @GetMapping("/info")
    fun getInfo(): CloudResponse.CloudInfo {
        val user = Session.getUser()

        val used = fileRepo.calcStorageUsed(user.id)
        val total = user.storage
        val count = fileRepo.countByUserId(user.id)
        val privateUsage = fileRepo.calcStorageUsedPrivate(user.id)
        val projectUsage = fileRepo.calcStorageUsedProject(user.id)

        return CloudResponse.CloudInfo(used, total, count, privateUsage, projectUsage)
    }

    @GetMapping("/view/user")
    fun getRootViewUser(): List<Any> {
        val user = Session.getUser()

        val folders = folderRepo.findPrivateFolderInRoot(user)
        val files = fileRepo.findPrivateInRoot(user)

        return digestResults(folders, files)
    }

    @GetMapping("/view/group/{id}")
    fun getRootViewGroup(@PathVariable id: Int): List<Any> {
        Session.getUser()
        val group = groupRepo.findByIdOrNull(id) ?: throw ErrorCode(404, "group not found")
        val folders = folderRepo.findGroupFolderInRoot(group)
        val files = fileRepo.findGroupInRoot(group)

        return digestResults(folders, files)
    }

    @GetMapping("/view/{folder}")
    fun getViewSubFolder(@PathVariable folder: Int): List<Any> {
        Session.getUser()

        val folders = folderRepo.findByParent(folder.obj())
        val files = fileRepo.findByFolderOrderByIdDesc(folder.obj())

        return digestResults(folders, files)
    }

    @PostMapping("/upload/user", "/upload/group/{groupId}")
    fun uploadUser(@PathVariable(required = false) groupId: Int?,
                   @RequestParam("folder") folderID: String?,
                   @RequestParam("file") file: MultipartFile,
                   request: HttpServletRequest): File {

        val user = Session.getUser()
        var fileName = file.originalFilename ?: throw ErrorCode(400, "missing file name")
        var contentType = file.contentType ?: throw ErrorCode(400, "missing content type")

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

        val group = when (groupId) {
            null -> null
            else -> groupRepo.findByIdOrNull(groupId) ?: throw ErrorCode(404, "group not found")
        }

        val folder = when (folderID) {
            "null" -> null
            else -> folderID?.toIntOrNull()
        }

        val result = fileService.storeFileStephaneum(user, fileName, contentType, bytes, folder, group)
        result.simplifyForPosts()

        return result
    }

    @PostMapping("/create-folder/user", "/create-folder/group/{groupId}")
    fun createFolder(@RequestBody request: CloudRequest.CreateFolder,
                     @PathVariable(required = false) groupId: Int?,
                     httpRequest: HttpServletRequest) {

        if(request.name.isNullOrBlank())
            throw ErrorCode(400, "missing name")

        val user = Session.getUser()

        val folder = when (request.parentID) {
            null -> null
            else -> folderRepo.findByIdOrNull(request.parentID) ?: throw ErrorCode(404, "folder not found")
        }

        val group = when (groupId) {
            null -> null
            else -> groupRepo.findByIdOrNull(groupId) ?: throw ErrorCode(404, "group not found")
        }

        folderRepo.save(Folder(0, request.name.trim(), user, group, folder))
    }

    @PostMapping("/update-public-file")
    fun updatePublic(@RequestBody request: CloudRequest.UpdatePublic) {

        if(request.fileID == null || request.isPublic == null)
            throw ErrorCode(400, "Missing Arguments")

        val user = Session.getUser()
        val file = fileRepo.findByIdOrNull(request.fileID) ?: throw ErrorCode(404, "file not found")

        if(!fileService.hasAccessToFile(user, file))
            throw ErrorCode(403, "no access to this file")

        file.public = request.isPublic
        fileRepo.save(file)
    }

    @PostMapping("/delete-file/{fileID}")
    fun deleteFile(@PathVariable fileID: Int) {

        val user = Session.getUser()
        val file = fileRepo.findByIdOrNull(fileID) ?: throw ErrorCode(404, "file not found")

        if(!fileService.hasAccessToFile(user, file))
            throw ErrorCode(403, "no access to this file")

        fileService.deleteFileStephaneum(user, file)
    }

    @PostMapping("/delete-folder/{folderID}")
    fun deleteFolder(@PathVariable folderID: Int) {

        val user = Session.getUser()
        val folder = folderRepo.findByIdOrNull(folderID) ?: throw ErrorCode(404, "folder not found")

        // TODO: check permissions

        fileService.deleteFolderStephaneum(user, folder)
    }

    @PostMapping("/move")
    fun move(@RequestBody request: CloudRequest.Move) {

        val me = Session.getUser()

        val target = if (request.targetFolderId != null)
            folderRepo.findByIdOrNull(request.targetFolderId) ?: throw ErrorCode(404, "target folder not found")
        else
            null

        when {
            request.fileId != null -> {
                val file = fileRepo.findByIdOrNull(request.fileId) ?: throw ErrorCode(404, "file not found")

                if(!fileService.hasAccessToFile(me, file))
                    throw ErrorCode(403, "no access to this file")

                file.folder = target
                fileRepo.save(file)
            }
            request.folderId != null -> {
                val folder = folderRepo.findByIdOrNull(request.folderId) ?: throw ErrorCode(404, "source folder not found")

                if(target != null && isChild(target, folder))
                    throw ErrorCode(409, "no recursion allowed")

                if(!fileService.hasAccessToFolder(me, folder))
                    throw ErrorCode(403, "no access to this folder")

                folder.parent = target
                folderRepo.save(folder)
            }
        }
    }

    @GetMapping("/key/{fileID}")
    fun getKey(@PathVariable fileID: Int): CloudResponse.FileKey {

        val user = Session.getUser()
        val file = fileRepo.findByIdOrNull(fileID) ?: throw ErrorCode(404, "file not found")

        if(!fileService.hasAccessToFile(user, file))
            throw ErrorCode(403, "no access to this file")

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

        return List(folders.size + files.size) { i ->
            if(i < folders.size)
                folders[i]
            else
                files[i-folders.size]
        }
    }

    private fun isChild(folder: Folder, possibleParent: Folder): Boolean {
        if (folder.id == possibleParent.id)
            return true

        val parent = folder.parent
        if (parent != null)
            return isChild(parent, possibleParent)

        return false
    }

}