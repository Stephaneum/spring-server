package de.stephaneum.backend.services

import de.stephaneum.backend.scheduler.ConfigFetcher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

@Service
class FileService {

    final val logger = LoggerFactory.getLogger(FileService::class.java)

    @Autowired
    private lateinit var configFetcher: ConfigFetcher

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

    fun loadFileAsResource(fileName: String, path: String = ""): Resource? {
        return try {
            val filePath = Paths.get(configFetcher.fileLocation+path).resolve(fileName).normalize()
            val resource = UrlResource(filePath.toUri())
            if (resource.exists()) {
                resource
            } else {
                null
            }
        } catch (ex: MalformedURLException) {
            null
        }

    }
}
