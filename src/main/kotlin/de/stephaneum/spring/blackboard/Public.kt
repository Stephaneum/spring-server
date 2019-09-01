package de.stephaneum.spring.blackboard

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.BlackboardRepo
import de.stephaneum.spring.database.Type
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.*
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
@RequestMapping("/blackboard")
class Public {

    @Autowired
    private lateinit var pdfToImageScheduler: PdfToImageScheduler

    @Autowired
    private lateinit var blackboardIterator: BlackboardScheduler

    @Autowired
    private lateinit var blackboardRepo: BlackboardRepo

    var activeClients = mutableMapOf<String, Long>()

    @GetMapping("/")
    fun index(model: Model): String {

        model["active"] = blackboardIterator.active

        val pdf = pdfToImageScheduler.instances.find { it.boardId == blackboardIterator.active.id } ?: PdfImages(0)
        model["pdfIndexes"] = (0 until pdf.images.size).toList()
        model["pdfTitle"] = pdf.title ?: "Stephaneum"
        model.addAttribute("toast", Session.getAndDeleteToast())

        return "blackboard/index"
    }

    @GetMapping("/img/{boardId}/{index}")
    fun pdfImg(@PathVariable boardId: Int, @PathVariable index: Int, request: HttpServletRequest, response: HttpServletResponse) {

        val pdf = pdfToImageScheduler.instances.find { it.boardId == boardId } ?: PdfImages(0)

        if(index >= pdf.images.size)
            return

        // last-modified
        val lastModified = (pdf.lastModified / 1000) * 1000 // last-modified does not save millis
        val lastModifiedBrowser = request.getDateHeader("If-Modified-Since")
        if (lastModified <= lastModifiedBrowser) {
            response.addDateHeader("Last-Modified", lastModified)
            response.status = HttpServletResponse.SC_NOT_MODIFIED
            return
        }

        response.addHeader("Cache-Control", "public")
        response.addDateHeader("Last-Modified", lastModified)
        response.contentType = "image/jpeg"
        response.outputStream.write(pdf.images[index])
    }

    @GetMapping("/img/{boardId}")
    fun img(@PathVariable boardId: Int, request: HttpServletRequest, response: HttpServletResponse) {

        val board = blackboardRepo.findByIdOrNull(boardId) ?: return

        if(board.type != Type.IMG || !board.isUploaded())
            return

        // last-modified
        val file = File(board.value)
        val lastModified = (file.lastModified() / 1000) * 1000 // last-modified does not save millis
        val lastModifiedBrowser = request.getDateHeader("If-Modified-Since")
        if (lastModified <= lastModifiedBrowser) {
            response.addDateHeader("Last-Modified", lastModified)
            response.status = HttpServletResponse.SC_NOT_MODIFIED
            return
        }

        // content
        val input = BufferedInputStream(FileInputStream(board.value))
        val bytes = ByteArray(input.available())
        input.read(bytes)
        input.close()

        // write content to response
        response.addHeader("Cache-Control", "public")
        response.addDateHeader("Last-Modified", lastModified)
        response.contentType = "image/jpeg"
        response.outputStream.write(bytes)
    }


    @GetMapping("/timestamp")
    @ResponseBody
    fun timestamp(@RequestHeader(value="X-Forwarded-For", required = false) forwardedIP: String?,
                  request: HttpServletRequest): TimestampJSON {

        // track active clients
        val ip = resolveIP(forwardedIP, request)
        activeClients[ip] = System.currentTimeMillis()

        return TimestampJSON(blackboardIterator.active.lastUpdate.time)
    }

    @GetMapping("/robots.txt", produces = ["text/plain; charset=utf-8"])
    @ResponseBody
    fun robots(): String {
        return "User-agent: *\nDisallow: /"
    }

    private fun resolveIP(forwardedIP: String?, request: HttpServletRequest): String {
        return forwardedIP?.split(",")?.first()?.trim() ?: request.remoteAddr
    }
}