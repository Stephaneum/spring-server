package de.stephaneum.backend.blackboard

import de.stephaneum.backend.Session
import de.stephaneum.backend.scheduler.BlackboardIterator
import de.stephaneum.backend.scheduler.ImageGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@Controller
@RequestMapping("/blackboard")
class Public {

    @Autowired
    private lateinit var imageGenerator: ImageGenerator

    @Autowired
    private lateinit var blackboardIterator: BlackboardIterator

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
}