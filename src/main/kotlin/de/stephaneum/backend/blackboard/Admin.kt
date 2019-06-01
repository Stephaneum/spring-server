package de.stephaneum.backend.blackboard

import de.stephaneum.backend.FileService
import de.stephaneum.backend.ImageService
import de.stephaneum.backend.Session
import de.stephaneum.backend.database.Blackboard
import de.stephaneum.backend.database.BlackboardRepo
import de.stephaneum.backend.database.Type
import de.stephaneum.backend.database.now
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.HtmlUtils
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.PostMapping


@Controller
@RequestMapping("/blackboard")
class Admin {

    final val logger = LoggerFactory.getLogger(Admin::class.java)
    final val STANDARD_VALUE = "Hier klicken, um Text einzugeben"

    @Autowired
    private lateinit var blackboardRepo: BlackboardRepo

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var imageService: ImageService

    @GetMapping("/admin")
    fun admin(model: Model): String {
        if(!Session.get().loggedIn)
            return REDIRECT_LOGIN

        model["types"] = Type.values()
        model["boards"]  = blackboardRepo.findByOrderByOrder()
        model.addAttribute("toast", Session.getAndDeleteToast())

        return "blackboard/admin"
    }

    // Mutations

    @GetMapping("/add")
    fun add(): String? {
        if(!Session.get().loggedIn)
            return REDIRECT_LOGIN

        val max = if(blackboardRepo.count() == 0L) 0 else blackboardRepo.findMaxOrder()
        blackboardRepo.save(Blackboard(0, Type.TEXT, STANDARD_VALUE, 10, max + 1, true))
        Session.addToast("Element hinzugefügt")
        return REDIRECT_ADMIN
    }

    @PostMapping("/upload/{id}")
    fun uploadFile(@PathVariable id: Int, @RequestParam("file") file: MultipartFile): String {

        if(!Session.get().loggedIn)
            return REDIRECT_LOGIN

        val board = blackboardRepo.findByIdOrNull(id) ?: return REDIRECT_ADMIN

        var fileName = file.originalFilename
        if(fileName == null) {
            Session.addToast("Ein Fehler ist aufgetreten", "Dateiname unbekannt")
            return REDIRECT_ADMIN
        }
        if(board.type == Type.PDF && !fileName.toLowerCase().endsWith(".pdf")) {
            Session.addToast("Ein Fehler ist aufgetreten", "Nur PDF-Dateien erlaubt")
            return REDIRECT_ADMIN
        } else if(board.type == Type.IMG && !fileName.toLowerCase().endsWith(".png") && !fileName.toLowerCase().endsWith("jpg") && !fileName.toLowerCase().endsWith("jpeg")) {
            Session.addToast("Ein Fehler ist aufgetreten", "Nur PNG oder JPG Dateien erlaubt")
            return REDIRECT_ADMIN
        }

        if(blackboardRepo.findAll().any { blackboard -> blackboard.id != id && blackboard.getFileName() == fileName }) {
            Session.addToast("Ein Fehler ist aufgetreten", "Dateiname existiert bereits")
            return REDIRECT_ADMIN
        }

        var bytes = file.bytes
        if(board.type == Type.IMG && !fileName.toLowerCase().endsWith(".jpg") && !fileName.toLowerCase().endsWith(".jpeg")) {
            // to jpeg
            val image = imageService.convertToBufferedImage(file.bytes)
            bytes = imageService.convertToJPG(image)
            fileName = fileService.changeExtension(fileName, "jpg")
            logger.info("converted to jpeg: $fileName")
        }

        val path = fileService.storeFile(bytes, "/blackboard", fileName)
        if(path != null) {
            board.value = path
            board.lastUpdate = now()
            blackboardRepo.save(board)
            Session.addToast("Datei hochgeladen")
        } else
            Session.addToast("Ein Fehler ist aufgetreten")
        return REDIRECT_ADMIN
    }

    @GetMapping("/rename/{id}")
    fun rename(@PathVariable id: Int, value: String): String? {
        if(!Session.get().loggedIn)
            return REDIRECT_LOGIN

        val board = blackboardRepo.findByIdOrNull(id) ?: return REDIRECT_ADMIN
        board.value = HtmlUtils.htmlEscape(value).replace(Regex("\r\n|\n|\r"), "<br>")
        board.lastUpdate = now()
        blackboardRepo.save(board)
        Session.addToast("Text geändert")
        return REDIRECT_ADMIN
    }

    @GetMapping("/duration/{id}")
    fun duration(@PathVariable id: Int, duration: Int): String? {
        if(!Session.get().loggedIn)
            return REDIRECT_LOGIN

        val board = blackboardRepo.findByIdOrNull(id) ?: return REDIRECT_ADMIN
        board.duration = duration
        board.lastUpdate = now()
        blackboardRepo.save(board)
        Session.addToast("Dauer geändert")
        return REDIRECT_ADMIN
    }

    @GetMapping("/type/{id}")
    fun type(@PathVariable id: Int, type: Type): String? {
        if(!Session.get().loggedIn)
            return REDIRECT_LOGIN

        val board = blackboardRepo.findByIdOrNull(id) ?: return REDIRECT_ADMIN
        board.type = type
        board.value = STANDARD_VALUE
        board.lastUpdate = now()
        blackboardRepo.save(board)

        Session.addToast("Typ geändert -> ${type.getString()}")
        return REDIRECT_ADMIN
    }

    @GetMapping("/toggle-visibility/{id}")
    fun toggleVisibility(@PathVariable id: Int): String? {
        if(!Session.get().loggedIn)
            return REDIRECT_LOGIN

        val board = blackboardRepo.findByIdOrNull(id) ?: return REDIRECT_ADMIN
        board.visible = !board.visible
        board.lastUpdate = now()
        blackboardRepo.save(board)

        Session.addToast(if(board.visible) "Element ist wieder sichtbar" else "Element wird ausgeblendet")
        return REDIRECT_ADMIN
    }

    @GetMapping("/move-up/{id}")
    fun moveUp(@PathVariable id: Int): String? {
        if(!Session.get().loggedIn)
            return REDIRECT_LOGIN

        val boards = blackboardRepo.findByOrderByOrder()
        val board = boards.find { it.id == id } ?: return REDIRECT_ADMIN
        val boardPrev = boards.find { it.order == board.order-1 } ?: return REDIRECT_ADMIN

        val temp = board.order
        board.order = boardPrev.order
        boardPrev.order = temp

        blackboardRepo.saveAll(listOf(board, boardPrev))

        return REDIRECT_ADMIN
    }

    @GetMapping("/move-down/{id}")
    fun moveDown(@PathVariable id: Int): String? {
        if(!Session.get().loggedIn)
            return REDIRECT_LOGIN

        val boards = blackboardRepo.findByOrderByOrder()
        val board = boards.find { it.id == id } ?: return REDIRECT_ADMIN
        val boardNext = boards.find { it.order == board.order+1 } ?: return REDIRECT_ADMIN

        val temp = board.order
        board.order = boardNext.order
        boardNext.order = temp

        blackboardRepo.saveAll(listOf(board, boardNext))

        return REDIRECT_ADMIN
    }

    @GetMapping("/delete/{id}")
    fun delete(@PathVariable id: Int): String? {
        if(!Session.get().loggedIn)
            return REDIRECT_ADMIN

        blackboardRepo.deleteById(id)
        repairOrder()
        Session.addToast("Element gelöscht")
        return REDIRECT_ADMIN
    }

    private fun repairOrder() {
        val boards = blackboardRepo.findAll()
        val sorted = boards.sortedBy { it.order }
        sorted.forEachIndexed { index, blackboard -> blackboard.order = index }
        blackboardRepo.saveAll(sorted)
    }
}