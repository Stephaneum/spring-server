package de.stephaneum.backend.blackboard

import de.stephaneum.backend.Session
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
    private lateinit var pdfToImageScheduler: PdfToImageScheduler

    @Autowired
    private lateinit var blackboardIterator: BlackboardScheduler

    @GetMapping
    fun index(model: Model): String {

        model["active"] = blackboardIterator.active

        val pdf = pdfToImageScheduler.instances.find { it.boardId == blackboardIterator.active.id } ?: PdfImages(0)
        model["pdfIndexes"] = (0 until pdf.images.size).toList()
        model["pdfTitle"] = pdf.title ?: "Stephaneum"
        model.addAttribute("toast", Session.getAndDeleteToast())

        return "blackboard/index"
    }

    @GetMapping("/img/{boardId}/{index}")
    fun img(@PathVariable boardId: Int, @PathVariable index: Int, response: HttpServletResponse) {

        val pdf = pdfToImageScheduler.instances.find { it.boardId == boardId } ?: PdfImages(0)

        if(index >= pdf.images.size)
            return

        response.contentType = "image/jpeg"
        response.outputStream.write(pdf.images[index])
    }

    @GetMapping("/timestamp")
    @ResponseBody
    fun timestamp(): TimestampJSON {
        return TimestampJSON(blackboardIterator.active.lastUpdate.time)
    }
}