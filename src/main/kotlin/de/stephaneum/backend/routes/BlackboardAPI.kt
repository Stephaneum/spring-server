package de.stephaneum.backend.routes

import de.stephaneum.backend.Session
import de.stephaneum.backend.Toast
import de.stephaneum.backend.database.Blackboard
import de.stephaneum.backend.database.BlackboardRepo
import de.stephaneum.backend.database.Type
import de.stephaneum.backend.database.now
import de.stephaneum.backend.scheduler.BlackboardIterator
import de.stephaneum.backend.scheduler.ImageGenerator
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.HtmlUtils
import javax.servlet.http.HttpServletResponse

data class TimestampJSON(val timestamp: Long)

@Controller
@RequestMapping("/blackboard")
class BlackboardAPI {

    val logger = LoggerFactory.getLogger(BlackboardAPI::class.java)
    val REDIRECT_LOGIN = "redirect:/blackboard/login"
    val REDIRECT_ADMIN = "redirect:/blackboard/admin"

    @Value("\${security.blackboard.password}")
    private lateinit var password: String

    @Autowired
    private lateinit var imageGenerator: ImageGenerator

    @Autowired
    private lateinit var blackboardIterator: BlackboardIterator

    @Autowired
    private lateinit var blackboardRepo: BlackboardRepo

    @GetMapping
    fun index(model: Model): String {

        model["active"] = blackboardIterator.active
        model["planIndexes"] = (0 until imageGenerator.images.size).toList()
        model["planDate"] = imageGenerator.date ?: "Stephaneum"
        model.addAttribute("toast", Session.getAndDeleteToast())

        return "blackboard/index"
    }

    @GetMapping("/img/{index}")
    fun img(@PathVariable index: Int, response: HttpServletResponse) {

        if(index >= imageGenerator.images.size)
            return

        response.contentType = "image/jpeg"
        response.outputStream.write(imageGenerator.images[index])
    }

    @GetMapping("/timestamp")
    @ResponseBody
    fun timestamp(): TimestampJSON {
        return TimestampJSON(blackboardIterator.active.lastUpdate.time)
    }

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
        blackboardRepo.save(Blackboard(0, Type.TEXT, "Hier klicken, um Text einzugeben", 10, max + 1, true))
        Session.addToast("Element hinzugefügt")
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
        board.lastUpdate = now()
        blackboardRepo.save(board)
        Session.addToast("Typ geändert")
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

    // Auth

    @GetMapping("/login")
    fun login(model: Model, @RequestParam error: Boolean = false): String {
        if(Session.get().loggedIn)
            return REDIRECT_ADMIN

        model["loginFailed"] = error
        if(error) {
            model["toast"] = Toast("Login gescheitert")
            println("here")
        }

        return "blackboard/login"
    }

    @PostMapping("/login")
    fun login(password: String): Any? {
        if(password == this.password) {
            Session.login()
            return REDIRECT_ADMIN
        } else {
            return "redirect:/blackboard/login?error=true"
        }
    }

    @GetMapping("/logout")
    fun logout(): String {
        Session.logout()

        return "redirect:/blackboard"
    }
}