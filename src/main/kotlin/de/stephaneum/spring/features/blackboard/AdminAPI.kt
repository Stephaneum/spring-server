package de.stephaneum.spring.features.blackboard

import de.stephaneum.spring.Permission
import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.helper.ImageService
import de.stephaneum.spring.Session
import de.stephaneum.spring.database.Blackboard
import de.stephaneum.spring.database.BlackboardRepo
import de.stephaneum.spring.database.Type
import de.stephaneum.spring.database.now
import de.stephaneum.spring.scheduler.ConfigFetcher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.HtmlUtils
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.PostMapping
import java.io.File


@RestController
@RequestMapping("/blackboard")
class BlackboardAdminAPI {

    final val logger = LoggerFactory.getLogger(BlackboardAdminAPI::class.java)
    final val STANDARD_VALUE = "Hier klicken, um Text einzugeben"

    @Autowired
    private lateinit var blackboardRepo: BlackboardRepo

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var imageService: ImageService

    @Autowired
    private lateinit var blackboardScheduler: BlackboardScheduler

    @Autowired
    private lateinit var configFetcher: ConfigFetcher

    @Autowired
    private lateinit var activeClientsScheduler: ActiveClientsScheduler

    // live updates

    @GetMapping("/data")
    fun data(): Any {

        if(Session.get().permission != Permission.BLACKBOARD)
            return Response.Feedback(false, needLogin = true)

        return Response.AdminData(Type.values().toList(), blackboardRepo.findByOrderByOrder())
    }

    @GetMapping("/info")
    fun info(): Any {

        if(Session.get().permission != Permission.BLACKBOARD)
            return Response.Feedback(false, needLogin = true)

        val activeSec = (System.currentTimeMillis()-blackboardScheduler.activeSince) / 1000 + 1
        return Response.AdminInfo(blackboardScheduler.active.id, activeSec.toInt(), activeClientsScheduler.activeClients.size, blackboardScheduler.timeToNextRefreshSec().toInt())
    }

    // Mutations

    @PostMapping("/add")
    fun add(): Response.Feedback {

        if(Session.get().permission != Permission.BLACKBOARD)
            return Response.Feedback(false, needLogin = true)

        val max = if(blackboardRepo.count() == 0L) 0 else blackboardRepo.findMaxOrder()
        blackboardRepo.save(Blackboard(0, Type.TEXT, STANDARD_VALUE, 10, max + 1, true))
        return Response.Feedback(true)
    }

    @PostMapping("/upload/{id}")
    fun uploadFile(@PathVariable id: Int, @RequestParam("file") file: MultipartFile): Response.Feedback {

        if(Session.get().permission != Permission.BLACKBOARD)
            return Response.Feedback(false, needLogin = true)

        val board = blackboardRepo.findByIdOrNull(id) ?: return Response.Feedback(false, message = "Element nicht gefunden")

        val fileName = file.originalFilename ?: return Response.Feedback(false, message = "Dateiname unbekannt")

        if(board.type == Type.PDF && !fileName.toLowerCase().endsWith(".pdf")) {
            return Response.Feedback(false, message = "Nur PDF-Dateien erlaubt")
        } else if(board.type == Type.IMG && !fileName.toLowerCase().endsWith(".png") && !fileName.toLowerCase().endsWith("jpg") && !fileName.toLowerCase().endsWith("jpeg")) {
            return Response.Feedback(false, message = "Nur PNG oder JPG Dateien erlaubt")
        }

        if(blackboardRepo.findAll().any { blackboard -> blackboard.id != id && blackboard.getFileName() == fileName }) {
            return Response.Feedback(false, message = "Dateiname existiert bereits")
        }

        var bytes = file.bytes
        var finalFileName = fileName
        if(board.type == Type.IMG && !finalFileName.toLowerCase().endsWith(".jpg") && !finalFileName.toLowerCase().endsWith(".jpeg")) {
            // to jpeg
            val image = imageService.convertToBufferedImage(file.bytes)
            bytes = imageService.convertToJPG(image)
            finalFileName = fileService.getPathWithNewExtension(finalFileName, "jpg")
            logger.info("converted to jpeg: $finalFileName")
        }

        // create missing blackboard folder
        val folder = File("${configFetcher.fileLocation}/blackboard")
        if (!folder.exists())
            folder.mkdirs()

        val path = fileService.storeFile(bytes, "${configFetcher.fileLocation}/blackboard/$finalFileName")
        if(path != null) {
            board.value = path
            board.lastUpdate = now()
            blackboardRepo.save(board)
            return Response.Feedback(true)
        } else {
            return Response.Feedback(false)
        }
    }

    @PostMapping("/rename/{id}")
    fun rename(@PathVariable id: Int, @RequestBody request: Request.Value): Response.Feedback {
        if(Session.get().permission != Permission.BLACKBOARD)
            return Response.Feedback(false, needLogin = true)

        val board = blackboardRepo.findByIdOrNull(id) ?: return Response.Feedback(false, message = "Element nicht gefunden")
        board.value = HtmlUtils.htmlEscape(request.value ?: "").replace(Regex("\r\n|\n|\r"), "<br>")
        board.lastUpdate = now()
        blackboardRepo.save(board)
        return Response.Feedback(true)
    }

    @PostMapping("/duration/{id}")
    fun duration(@PathVariable id: Int, @RequestBody request: Request.Duration): Response.Feedback {
        if(Session.get().permission != Permission.BLACKBOARD)
            return Response.Feedback(false, needLogin = true)

        if(request.duration == null || request.duration <= 0 || request.duration >= 3600)
            return Response.Feedback(false, message = "Die Dauer muss zwischen 0 und 3600 liegen")

        val board = blackboardRepo.findByIdOrNull(id) ?: return Response.Feedback(false, message = "Element nicht gefunden")
        board.duration = request.duration
        board.lastUpdate = now()
        blackboardRepo.save(board)
        return Response.Feedback(true)
    }

    @PostMapping("/type/{id}")
    fun type(@PathVariable id: Int, @RequestBody request: Request.Type): Response.Feedback {
        if(Session.get().permission != Permission.BLACKBOARD)
            return Response.Feedback(false, needLogin = true)

        val board = blackboardRepo.findByIdOrNull(id) ?: return Response.Feedback(false, message = "Element nicht gefunden")
        board.type = request.type
        board.value = STANDARD_VALUE
        board.lastUpdate = now()
        blackboardRepo.save(board)

        return Response.Feedback(true)
    }

    @PostMapping("/toggle-visibility/{id}")
    fun toggleVisibility(@PathVariable id: Int): Response.Feedback {
        if(Session.get().permission != Permission.BLACKBOARD)
            return Response.Feedback(false, needLogin = true)

        val board = blackboardRepo.findByIdOrNull(id) ?: return Response.Feedback(false, message = "Element nicht gefunden")
        board.visible = !board.visible
        board.lastUpdate = now()
        blackboardRepo.save(board)

        return Response.Feedback(true)
    }

    @PostMapping("/move-up/{id}")
    fun moveUp(@PathVariable id: Int): Response.Feedback {
        if(Session.get().permission != Permission.BLACKBOARD)
            return Response.Feedback(false, needLogin = true)

        val boards = blackboardRepo.findByOrderByOrder()
        val board = boards.find { it.id == id } ?: return Response.Feedback(false, message = "Element nicht gefunden")
        val boardPrev = boards.find { it.order == board.order-1 } ?: return Response.Feedback(false)

        val temp = board.order
        board.order = boardPrev.order
        boardPrev.order = temp

        blackboardRepo.saveAll(listOf(board, boardPrev))

        return Response.Feedback(true)
    }

    @PostMapping("/move-down/{id}")
    fun moveDown(@PathVariable id: Int): Response.Feedback {
        if(Session.get().permission != Permission.BLACKBOARD)
            return Response.Feedback(false, needLogin = true)

        val boards = blackboardRepo.findByOrderByOrder()
        val board = boards.find { it.id == id } ?: return Response.Feedback(false, message = "Element nicht gefunden")
        val boardNext = boards.find { it.order == board.order+1 } ?: return Response.Feedback(false)

        val temp = board.order
        board.order = boardNext.order
        boardNext.order = temp

        blackboardRepo.saveAll(listOf(board, boardNext))

        return Response.Feedback(true)
    }

    @PostMapping("/delete/{id}")
    fun delete(@PathVariable id: Int): Response.Feedback {
        if(Session.get().permission != Permission.BLACKBOARD)
            return Response.Feedback(false, needLogin = true)

        blackboardRepo.deleteById(id)
        repairOrder()
        return Response.Feedback(true)
    }

    private fun repairOrder() {
        val boards = blackboardRepo.findAll()
        val sorted = boards.sortedBy { it.order }
        sorted.forEachIndexed { index, blackboard -> blackboard.order = index }
        blackboardRepo.saveAll(sorted)
    }
}