package de.stephaneum.spring.features.cms

import de.stephaneum.spring.database.File
import de.stephaneum.spring.database.FileRepo
import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.helper.ImageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO

@Service
class PostService {

    @Autowired
    private lateinit var imageService: ImageService

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var fileRepo: FileRepo

    fun compress(file: File) {
        val image = ImageIO.read(java.io.File(file.path))
        val imageCompressed = imageService.reduceSize(image, 1000, 1000)

        // update in hard drive
        Files.delete(Paths.get(file.path))
        val newPath = fileService.getPathWithNewExtension(file.path, "jpg")
        ImageIO.write(imageCompressed, "jpg", java.io.File(newPath))

        // update path and size in database
        file.path = newPath
        file.size = java.io.File(newPath).length().toInt()
        fileRepo.save(file)
    }
}