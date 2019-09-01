package de.stephaneum.spring.helper

import de.stephaneum.spring.scheduler.ConfigFetcher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import java.io.*
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

    @Autowired
    private lateinit var configFetcher: ConfigFetcher

    /**
     * @param content byte array which should be saved
     * @param path the path to the folder relative to the main location
     * @param fileName name of file which will be appended to the path
     * @return path to file if saving was successful
     */
    fun storeFile(content: ByteArray, path: String = "", fileName: String): String? {

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                return null
            }

            // Copy file to the target location (Replacing existing file with the same name)
            val targetLocation = Paths.get(configFetcher.fileLocation+path).resolve(fileName)
            Files.copy(ByteArrayInputStream(content), targetLocation, StandardCopyOption.REPLACE_EXISTING)

            return targetLocation.toString().replace("\\", "/")
        } catch (ex: IOException) {
            logger.error("Storing file failed",ex)
            return null
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
    fun listFiles(path: String): List<File>? {
        val folder = File(path)
        return folder.listFiles()?.toList()?.filter { it.isFile }
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
            println("Enter folder \"$fileName\"")
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
}
