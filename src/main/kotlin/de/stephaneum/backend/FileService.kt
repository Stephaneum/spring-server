package de.stephaneum.backend

import de.stephaneum.backend.scheduler.ConfigFetcher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@Service
class FileService {

    final val logger = LoggerFactory.getLogger(FileService::class.java)

    @Autowired
    private lateinit var configFetcher: ConfigFetcher

    fun storeFile(file: MultipartFile, path: String = ""): Boolean {
        // Normalize file name
        val fileName = StringUtils.cleanPath(file.originalFilename ?: "")

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                return false
            }

            // Copy file to the target location (Replacing existing file with the same name)
            val targetLocation = Path.of(configFetcher.location+path).resolve(fileName)
            Files.copy(file.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)

            return true
        } catch (ex: IOException) {
            return false
        }

    }

    fun loadFileAsResource(fileName: String, path: String = ""): Resource? {
        return try {
            val filePath = Path.of(configFetcher.location+path).resolve(fileName).normalize()
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
