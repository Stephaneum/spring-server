package de.stephaneum.spring.helper

import de.stephaneum.spring.database.*
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.scheduler.Element
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.data.repository.findByIdOrNull
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

    final val logger = LoggerFactory.getLogger(FileService::class.java)

    val formatter = DecimalFormat("#.#")
    enum class StoreMode(val description: String) {
        PRIVATE("Privatspeicher"),
        SCHOOL_CLASS("Klassenspeicher"),
        GROUP("Projektspeicher"),
        CATEGORY("Rubrik"),
        TEACHER_CHAT("Lehrerchat")
    }

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
    private lateinit var statsCloudRepo: StatsCloudRepo

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
     * @param classProjectCategoryID depends on the mode
     * @param mode will be used to determine where to store and what ID to be used
     * @return the file if success, else a string representing the error
     */
    fun storeFileStephaneum(user: User, filename: String, mime: String, content: ByteArray, folder: Any?, classProjectCategoryID: Int?, mode: StoreMode): Any {

        // check if enough space
        if(user.storage - fileRepo.calcStorageUsed(user.id) < content.size)
            return "Nicht genügend Speicher"

        // resolve folder
        val mainPath = configScheduler.get(Element.fileLocation) ?: return "Interner Fehler"
        var savingFolder: Folder?
        if(mode == StoreMode.PRIVATE && folder is String) {
            savingFolder = folderRepo.findPrivateFolderInRoot(user, folder).firstOrNull()
            if(savingFolder == null) {
                // create new one
                savingFolder = folderRepo.save(Folder(0, folder, user, null, null, false, null))
            }
        } else if(folder is Int){
            savingFolder = Folder(folder)
        } else {
            savingFolder = null
        }

        // resolve the additional id
        var group: Group? = null
        var schoolClass: SchoolClass? = null
        if(mode == StoreMode.SCHOOL_CLASS || mode == StoreMode.GROUP || mode == StoreMode.CATEGORY) {
            if(classProjectCategoryID == null)
                return "unknown class/project/category ID"

            if(mode == StoreMode.SCHOOL_CLASS)
                schoolClass = SchoolClass(classProjectCategoryID)
            else if(mode == StoreMode.GROUP)
                group = Group(classProjectCategoryID)
        }

        val file = fileRepo.save(de.stephaneum.spring.database.File(
                id = 0,
                user = if(mode == StoreMode.CATEGORY) null else user,
                path = "", // will be set in the next step
                group = group,
                schoolClass = schoolClass,
                timestamp = now(),
                size = content.size,
                mime = mime,
                public = false,
                teacherChat = mode == StoreMode.TEACHER_CHAT,
                folder = savingFolder))

        // now set the filename with the id
        file.path = "$mainPath/${file.id}_$filename"
        fileRepo.save(file)

        // set category picture
        if(mode == StoreMode.CATEGORY) {
            val menu = menuRepo.findByIdOrNull(classProjectCategoryID!!) ?: return "Datei gespeichert, aber Verknüpfung mit Gruppe gescheitert"
            menu.image = file
            menuRepo.save(menu)
        }

        // save file
        val targetLocation = Paths.get(file.path)
        Files.copy(ByteArrayInputStream(content), targetLocation, StandardCopyOption.REPLACE_EXISTING)

        // update stats
        statsCloudRepo.save(StatsCloud(0, now(), file.size))

        // log
        logService.log(EventType.UPLOAD, mode.description, user, filename)

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

        folderRepo.deleteById(folder.id)
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

        val mode = when {
            file.group != null -> StoreMode.GROUP
            file.schoolClass != null -> StoreMode.SCHOOL_CLASS
            file.teacherChat -> StoreMode.TEACHER_CHAT
            else -> StoreMode.PRIVATE
        }

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
        logService.log(EventType.DELETE_FILE, mode.description, user, file.generateFileName())
    }

    fun deleteFileStephaneumFinal(file: de.stephaneum.spring.database.File) {
        // delete file (db and hard drive)
        fileRepo.delete(file)
        deleteFile(file.path)

        // update stats
        statsCloudRepo.save(StatsCloud(0, now(), -file.size))

        logger.info("File deleted: '${file.generateFileName()}'")
    }

    /**
     * @return true if this file is used for a post, menu (section),
     */
    fun hasConnections(file: de.stephaneum.spring.database.File): Boolean {
        if(filePostRepo.countByFile(file) != 0)
            return true

        if(menuRepo.countByImage(file) != 0)
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
            file.schoolClass != null && user.schoolClass?.id == file.schoolClass?.id -> true // user is inside school class
            file.teacherChat && user.code.role == ROLE_TEACHER -> true // teacher chat
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
     * @param bytes the amount of bytes
     * @return human readable string
     */
    fun convertSizeToString(bytes: Long): String {
        val s = when {
            bytes < 1024                -> "$bytes Bytes"
            bytes < 1024 * 1024         -> formatter.format(bytes.toDouble() / 1024) + " KB"
            bytes < 1024 * 1024 * 1024  -> formatter.format(bytes.toDouble() / (1024 * 1024)) + " MB"
            else                        -> formatter.format(bytes.toDouble() / (1024 * 1024 * 1024)) + " GB"
        }
        return s.replace('.', ',')
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
}
