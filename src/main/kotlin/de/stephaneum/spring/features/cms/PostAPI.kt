package de.stephaneum.spring.features.cms

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.EMPTY_USER
import de.stephaneum.spring.database.File
import de.stephaneum.spring.database.FileRepo
import de.stephaneum.spring.helper.FileService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/post")
class PostAPI {

    @Autowired
    private lateinit var fileRepo: FileRepo

    @Autowired
    private lateinit var fileService: FileService

    @GetMapping("/images-available")
    fun get(): Any {

        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)
        val files = fileRepo.findMyImages(user.id, "image")
        digestFiles(files)

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
            digestFiles(listOf(result))

        return if(result is String) Response.Feedback(false, message = result) else result
    }

    fun digestFiles(files: List<File>) {
        files.forEach { file ->
            file.fileNameWithID = file.path.substring(file.path.lastIndexOf('/') + 1)
            file.path = ""
            file.user = EMPTY_USER
            file.folder = null
        }
    }
}