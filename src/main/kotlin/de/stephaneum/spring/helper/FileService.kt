package de.stephaneum.spring.helper

import de.stephaneum.spring.database.*
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.scheduler.Element
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import java.io.*
import java.io.File
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.text.DecimalFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@Service
class FileService {

    private val logger = LoggerFactory.getLogger(FileService::class.java)

    @Autowired
    private lateinit var configScheduler: ConfigScheduler

    @Autowired
    private lateinit var fileRepo: FileRepo

    @Autowired
    private lateinit var filePostRepo: FilePostRepo

    @Autowired
    private lateinit var userGroupRepo: UserGroupRepo

    @Autowired
    private lateinit var folderRepo: FolderRepo

    @Autowired
    private lateinit var menuRepo: MenuRepo

    @Autowired
    private lateinit var cloudStatsService: CloudStatsService

    @Autowired
    private lateinit var logService: LogService

    /**
     * @param content byte array which should be saved
     * @param path the path where to store the file
     * @return path to file if saving was successful
     */
    fun storeFile(content: ByteArray, path: String): String? {

        try {
            // Copy file to the target location (Replacing existing file with the same name)
            val targetLocation = Paths.get(path)
            Files.copy(ByteArrayInputStream(content), targetLocation, StandardCopyOption.REPLACE_EXISTING)

            return targetLocation.toString().replace("\\", "/")
        } catch (ex: IOException) {
            logger.error("Storing file failed",ex)
            return null
        }
    }

    /**
     * saves file to database and hard drive
     * logs the actions
     * updates the statistics
     * @param user the user saving the file (the owner)
     * @param filename filename
     * @param mime mime type
     * @param content byte array which should be saved
     * @param folder the folder (if Integer, then used as ID, else used as root folder name)
     * @param group if specified, then file is saved in group
     * @param lockedFolder if true, then folder is not removable
     * @return the saved file
     */
    fun storeFileStephaneum(user: User, filename: String, mime: String, content: ByteArray, folder: Any?, group: Group?, lockedFolder: Boolean = false): de.stephaneum.spring.database.File {

        // check if enough space
        if(user.storage - fileRepo.calcStorageUsed(user.id) < content.size)
            throw ErrorCode(409, "Not enough storage")

        // resolve folder
        val mainPath = configScheduler.get(Element.fileLocation) ?: throw ErrorCode(500, "unknown file location")
        val savingFolder = when (folder) {
            is Int -> Folder(folder)
            is String -> when (group) {
                null -> folderRepo.findPrivateFolderInRoot(user, folder).firstOrNull() ?: folderRepo.save(Folder(0, folder, user, null, null, false, null, lockedFolder))
                else -> folderRepo.findGroupFolderInRoot(group, folder).firstOrNull() ?: folderRepo.save(Folder(0, folder, user, group, null, false, null, lockedFolder))
            }
            else -> null
        }

        val file = fileRepo.save(de.stephaneum.spring.database.File(
                id = 0,
                user = user,
                path = "", // will be set in the next step
                group = group,
                schoolClass = null,
                timestamp = now(),
                size = content.size,
                mime = mime,
                public = false,
                teacherChat = false,
                folder = savingFolder))

        // now set the filename with the id
        file.path = "$mainPath/${file.id}_$filename"
        fileRepo.save(file)

        // save file
        val targetLocation = Paths.get(file.path)
        Files.copy(ByteArrayInputStream(content), targetLocation, StandardCopyOption.REPLACE_EXISTING)

        // update stats
        cloudStatsService.add(file.size)

        // log
        if(group != null)
            logService.log(EventType.UPLOAD, "Gruppenspeicher", user, "${group.name}, $filename")
        else
            logService.log(EventType.UPLOAD, "Privatspeicher", user, filename)

        return file
    }

    /**
     * deletes the folder and its children (e.g. child folders, files)
     * regarding the files: those are deleted according to [FileService.deleteFileStephaneum]
     * @param user the user who is deleting the folder
     * @param folder the folder to be deleted
     */
    fun deleteFolderStephaneum(user: User, folder: Folder) {

        val children = folderRepo.findByParent(folder)
        for(f in children) {
            deleteFolderStephaneum(user, f) // delete child folders
        }

        val files = fileRepo.findByFolder(folder)
        for(f in files) {
            deleteFileStephaneum(user, f) // delete files of the current folder
        }

        folderRepo.delete(folder)
    }

    /**
     * deletes all files which are uploaded by this user
     */
    fun clearStorage(user: User) {
        val files = fileRepo.findByUser(user)
        files.forEach { file -> deleteFileStephaneum(user, file) }
    }

    /**
     * deletes the file (in the perspective of the user)
     *
     * but in reality:
     * deletes the file if it has no connections, otherwise just removes the other connections (user, project, class, teacherChat)
     * @param user the user who is deleting this file
     * @param file the file to be deleted
     */
    fun deleteFileStephaneum(user: User, file: de.stephaneum.spring.database.File) {
        val group = file.group

        if(hasConnections(file)) {
            // has connections just remove the other connections
            file.user = null
            file.schoolClass = null
            file.group = null
            file.teacherChat = false
            fileRepo.save(file)
            logger.info("Connections to the file '${file.generateFileName()}' has been deleted but still exists due to connections to posts / menus")
        } else {
            deleteFileStephaneumFinal(file)
        }

        // log
        if(group != null)
            logService.log(EventType.DELETE_FILE, "Gruppenspeicher", user, "${group.name}, ${file.generateFileName()}")
        else
            logService.log(EventType.DELETE_FILE, "Privatspeicher", user, file.generateFileName())
    }

    /**
     * deletes a file from hdd and from database permanently
     * please ensure that there are no connections to posts etc.
     * called by [de.stephaneum.spring.scheduler.GarbageCollector] and [deleteFileStephaneum]
     */
    fun deleteFileStephaneumFinal(file: de.stephaneum.spring.database.File) {
        // delete file (db and hard drive)
        fileRepo.delete(file)
        deleteFile(file.path)

        // update stats
        cloudStatsService.add(-file.size)

        logger.info("File deleted: '${file.generateFileName()}'")
    }

    /**
     * @return true if this file is used for a post, menu (section),
     */
    fun hasConnections(file: de.stephaneum.spring.database.File): Boolean {
        if(filePostRepo.countByFile(file) != 0)
            return true

        return false
    }

    /**
     * @return true if the user has access to the file
     */
    fun hasAccessToFile(user: User, file: de.stephaneum.spring.database.File): Boolean {
        return when {
            file.user?.id == user.id -> true // user owns this file
            user.code.role == ROLE_ADMIN -> true // user is admin
            file.group?.let { group -> userGroupRepo.existsByUserAndGroup(user, group) } ?: false -> true // user is inside group
            else -> false
        }
    }

    /**
     * @return true if the user has access to the folder
     */
    fun hasAccessToFolder(user: User, folder: Folder): Boolean {
        return when {
            folder.user?.id == user.id -> true // user owns this folder
            user.code.role == ROLE_ADMIN -> true // user is admin
            folder.group?.let { group -> userGroupRepo.existsByUserAndGroup(user, group) } ?: false -> true // user is inside group
            else -> false
        }
    }

    fun loadFileAsResource(path: String): Resource? {
        return try {
            val resource = UrlResource(Paths.get(path).toUri())
            if (resource.exists()) {
                resource
            } else {
                null
            }
        } catch (ex: MalformedURLException) {
            null
        }
    }

    fun loadFileAsString(path: String): String? {
        return try {
            String(Files.readAllBytes(Paths.get(path)))
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * @param original the original path to file
     * @param extension the new extension
     * @return path with new extension
     */
    fun getPathWithNewExtension(original: String, extension: String): String {
        val index = original.lastIndexOf('.')+1
        if(index == 0)
            return "$original.$extension"

        val withoutExtension = original.substring(0, index)
        return withoutExtension+extension
    }

    /**
     * @param path the path to the folder containing the listed files
     * @return list of all files
     */
    fun listFiles(path: String): List<File> {
        val folder = File(path)
        return folder.listFiles()?.toList()?.filter { it.isFile } ?: emptyList()
    }

    /**
     * @param path the path to the folder containing the listed files
     * @return list of all files recursively
     */
    fun listFilesRecursive(path: String): List<File> {
        val folder = File(path)
        val files: MutableList<File> = mutableListOf()
        val entities = folder.listFiles()?.toList() ?: emptyList()
        for(entity in entities) {
            if(entity.isFile)
                files.add(entity)
            else if(entity.isDirectory)
                files.addAll(listFilesRecursive(entity.absolutePath))
        }
        return files
    }

    /**
     * @param folder the folder to delete
     * @param deleteParent if true, then the folder itself will be also deleted
     */
    fun deleteFolder(folder: File, deleteParent: Boolean) {
        val files = folder.listFiles()
        if (files != null) { //some JVMs return null for empty dirs
            for (f in files) {
                if (f.isDirectory) {
                    deleteFolder(f, true)
                } else {
                    f.delete()
                }
            }
        }

        if (deleteParent)
            folder.delete()
    }


    /**
     * deletes the file
     * @return true if it was successful
     */
    fun deleteFile(path: String): Boolean {
        val file = File(path)
        if(file.exists()) {
            return file.delete()
        } else {
            return false
        }
    }

    @Throws(IOException::class)
    fun zip(fileToZip: String, destination: String) {

        val fos = FileOutputStream(destination)
        val zipOut = ZipOutputStream(fos)
        val file = File(fileToZip)

        _zip(file, null, zipOut)
        zipOut.close()
        fos.close()
    }

    // recursive function
    @Throws(IOException::class)
    private fun _zip(fileToZip: File, fileName: String?, zipOut: ZipOutputStream) {

        if (fileToZip.isHidden) {
            return
        }
        if (fileToZip.isDirectory) {
            val children = fileToZip.listFiles()
            for (i in children!!.indices) {

                val childFile = children[i]

                val current: String
                if (fileName != null)
                    current = fileName + "/" + childFile.name
                else
                    current = childFile.name

                _zip(childFile, current, zipOut)
            }
            return
        }

        if (fileName != null) {
            val fis = FileInputStream(fileToZip)
            val zipEntry = ZipEntry(fileName)
            zipOut.putNextEntry(zipEntry)
            val bytes = ByteArray(1024)
            while (true) {
                val length = fis.read(bytes)
                if(length < 0)
                    break
                zipOut.write(bytes, 0, length)
            }
            fis.close()
        }
    }

    @Throws(IOException::class)
    fun unzip(fileToUnzip: String, destination: String) {

        val buffer = ByteArray(1024)
        val zis = ZipInputStream(FileInputStream(fileToUnzip))
        var zipEntry: ZipEntry? = zis.nextEntry

        while (zipEntry != null) {

            val fileName = zipEntry.name                       // e.g. dateien/23_Bild.png
            val filePath = "$destination/$fileName"            // e.g. /home/projekt/dateien/23_Bild.png
            val newFile = File(filePath)

            newFile.parentFile.mkdirs() // create missing parent folders

            val fos = FileOutputStream(newFile)
            while (true) {
                val length = zis.read(buffer)
                if(length <= 0)
                    break

                fos.write(buffer, 0, length)
            }
            fos.close()
            zipEntry = zis.nextEntry
        }
        zis.closeEntry()
        zis.close()
    }

    fun getFileName(path: String): String {
        val index = path.lastIndexOf('/')+1
        return if(index != -1)
            path.substring(index)
        else
            ""
    }

    fun getExtension(path: String): String {
        val index = path.lastIndexOf('.')+1
        return if(index != -1)
            path.substring(index)
        else
            ""
    }

    fun getMimeFromPath(path: String): String {
        return getMime(getExtension(path))
    }

    /**
     * get the mime from the file extension
     * @param extension the extension of the given file
     * @return the resulting mime type or 'application/octet-stream' if it cannot be solved
     */
    fun getMime(extension: String): String {
        return when (extension.toLowerCase()) {
            "pdf" -> "application/pdf"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "pptx" -> "application/mspowerpoint"
            "xml" -> "application/xml"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            "mp4" -> "video/mp4"
            "zip" -> "application/zip"
            "wav" -> "audio/x-wav"
            "htm", "html" -> "text/html"
            "js" -> "text/javascript"
            "css" -> "text/css"
            else -> "application/octet-stream"
        }
    }

    /**
     * @return true if the mime type indicates that it is an image
     */
    fun isImage(mime: String) = mime.startsWith("image")

    fun isPDF(mime: String) = mime == "application/pdf"
}
