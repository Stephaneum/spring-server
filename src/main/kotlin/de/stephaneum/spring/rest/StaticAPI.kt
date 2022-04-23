package de.stephaneum.spring.rest

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.ErrorCode
import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.scheduler.Element
import org.jsoup.Jsoup
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

data class AdminStaticInfo(val staticPath: String, val pages: List<Static>)
data class StaticContent(val mode: StaticMode, val title: String, val body: String)

@RestController
@RequestMapping("/api/static")
class StaticAPI (
        private val fileService: FileService,
        private val configScheduler: ConfigScheduler,
        private val staticRepo: StaticRepo
) {

    @GetMapping
    fun get(@RequestParam path: String): StaticContent {
        val mainPath = configScheduler.get(Element.fileLocation) ?: throw ErrorCode(500, "unknown file location")
        val page = staticRepo.findByPath(path)
        val content = fileService.loadFileAsString("$mainPath/${Static.FOLDER_NAME}/$path")

        if (page == null || content == null)
            throw ErrorCode(404, "page not found")

        val doc = Jsoup.parse(content)
        val body = doc.select("body").first()?.html() ?: ""
        val title = doc.select("title").first()?.html() ?: "Beitrag"

        return StaticContent(page.mode, title, body)
    }

    @GetMapping("/status")
    fun getFiles(): AdminStaticInfo {
        Session.getUser(adminOnly = true)

        val location = configScheduler.get(Element.fileLocation) ?: throw ErrorCode(500, "unknown file location")
        return AdminStaticInfo(staticPath = "$location/${Static.FOLDER_NAME}", pages = staticRepo.findAll().toList())
    }

    // storing a file in root static-level
    @PostMapping("/upload")
    fun upload(@RequestParam("file") file: MultipartFile) {
        Session.getUser(adminOnly = true)

        val fileName = file.originalFilename?.lowercase() ?: throw ErrorCode(400, message = "unknown filename")
        if(!fileName.endsWith(".htm") && !fileName.endsWith("html"))
            throw ErrorCode(409, "only html")

        val finalFileName = fileService.getPathWithNewExtension(fileName, "html")

        if(staticRepo.existsByPath(finalFileName))
            throw ErrorCode(410, "already exists")

        // save to hard drive
        fileService.storeFile(file.inputStream, "${configScheduler.get(Element.fileLocation)}/${Static.FOLDER_NAME}/$finalFileName") ?: throw ErrorCode(500, "storing failed")

        // save to database
        staticRepo.save(Static(path = finalFileName))
    }

    @PostMapping("/toggle-mode/{id}")
    fun toggleMode(@PathVariable id: Int) {
        Session.getUser(adminOnly = true)

        val page = staticRepo.findByIdOrNull(id) ?: throw ErrorCode(404, "not found")
        page.mode = when(page.mode) {
            StaticMode.MIDDLE -> StaticMode.FULL_WIDTH
            StaticMode.FULL_WIDTH -> StaticMode.FULL_SCREEN
            StaticMode.FULL_SCREEN -> StaticMode.MIDDLE
        }
        staticRepo.save(page)
    }

    @PostMapping("/delete/{id}")
    fun delete(@PathVariable id: Int) {
        Session.getUser(adminOnly = true)

        val page = staticRepo.findByIdOrNull(id) ?: throw ErrorCode(404, "not found")
        val location = configScheduler.get(Element.fileLocation) ?: throw ErrorCode(500, "unknown file location")
        val success = fileService.deleteFile("$location/${Static.FOLDER_NAME}/${page.path}")
        staticRepo.delete(page)

        if(!success)
            throw ErrorCode(500, "file could not be deleted")
    }
}