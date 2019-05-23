package de.stephaneum.backend.routes

import de.stephaneum.backend.Session
import de.stephaneum.backend.database.BlackboardRepo
import de.stephaneum.backend.scheduler.ImageGenerator
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletResponse

@Controller
@RequestMapping("/blackboard")
class BlackboardAPI {

    val logger = LoggerFactory.getLogger(BlackboardAPI::class.java)

    @Value("\${security.blackboard.password}")
    private lateinit var password: String

    @Autowired
    private lateinit var imageGenerator: ImageGenerator

    @Autowired
    private lateinit var blackboardRepo: BlackboardRepo

    @GetMapping
    fun html(model: Model): String {
        model["indexes"] = (0 until imageGenerator.images.size).toList()
        model["date"] = imageGenerator.date ?: "Stephaneum"
        return "blackboard/index"
    }

    @GetMapping("/img/{index}")
    fun jpg(@PathVariable index: Int, response: HttpServletResponse) {

        if(index >= imageGenerator.images.size)
            return

        response.contentType = "image/jpeg"
        response.outputStream.write(imageGenerator.images[index])
    }

    @GetMapping("/admin")
    fun admin(model: Model): String {
        if(!Session.get().loggedIn)
            return "redirect:/blackboard/login"

        model["blackboards"]  = blackboardRepo.findByOrderByOrder()

        return "blackboard/admin"
    }

    @GetMapping("/login")
    fun login(model: Model, error: Boolean = false): String {
        if(Session.get().loggedIn)
            return "redirect:/blackboard/admin"

        model["login"] = Models.Login()
        model["error"] = error

        return "blackboard/login"
    }

    @PostMapping("/login")
    fun login(@ModelAttribute login: Models.Login): Any? {
        if(login.password == password) {
            Session.login()
            return "redirect:/blackboard/admin"
        } else {
            return ModelAndView("blackboard/login", mapOf(Pair("error", "true")))
        }
    }

    @GetMapping("/logout")
    fun logout(): String {
        Session.logout()

        return "redirect:/blackboard"
    }
}