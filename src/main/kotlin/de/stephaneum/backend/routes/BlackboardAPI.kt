package de.stephaneum.backend.routes

import de.stephaneum.backend.scheduler.ImageGenerator
import de.stephaneum.backend.scheduler.PDFLocation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
@RequestMapping("/blackboard")
class BlackboardAPI {

    val logger = LoggerFactory.getLogger(BlackboardAPI::class.java)

    @Autowired
    private lateinit var pdfLocation: PDFLocation

    @Autowired
    private lateinit var imageGenerator: ImageGenerator

    @GetMapping("/pdf")
    fun pdf(request: HttpServletRequest, response: HttpServletResponse) {

        val path = pdfLocation.location

        if (path == null) {
            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            val writer = response.writer
            writer.println("<html>")
            writer.println("<head><title>Ein Fehler ist aufgetreten</title></title>")
            writer.println("<body>")
            writer.println("<br><br>")
            writer.println("<h1 align=\"center\">Bis jetzt wurde kein Vertretungsplan hochgeladen.</h1>")
            writer.println("<br><br>")
            writer.println("<h2 align=\"center\">Error 500</h2>")
            writer.println("</body></html>")
            writer.flush()
            writer.close()
            return
        }

        try {
            // last-modified
            val file = File(path)
            val lastModified = (file.lastModified() / 1000) * 1000 //last-modified speichert keine Millisekunden
            val lastModifiedBrowser = request.getDateHeader("If-Modified-Since")
            if (lastModified <= lastModifiedBrowser) {
                response.addDateHeader("Last-Modified", lastModified)
                response.status = HttpServletResponse.SC_NOT_MODIFIED
                return
            }

            // content
            val input = BufferedInputStream(FileInputStream(path))
            val bytes = ByteArray(input.available())
            input.read(bytes)
            input.close()

            // write content to response
            response.addHeader("Cache-Control", "no-cache")
            response.addDateHeader("Last-Modified", lastModified)
            response.contentType = "application/pdf"
            response.outputStream.write(bytes)
        } catch (e: IOException) {
            logger.error(e.message)
        }

    }

    @GetMapping
    fun html(model: Model): String {
        model["indexes"] = (0 until imageGenerator.images.size).toList()
        model["date"] = imageGenerator.date ?: "Stephaneum"
        return "blackboard"
    }

    @GetMapping("/img/{index}")
    fun jpg(@PathVariable index: Int, response: HttpServletResponse) {

        if(index >= imageGenerator.images.size)
            return

        response.contentType = "image/jpeg"
        response.outputStream.write(imageGenerator.images[index])
    }
}