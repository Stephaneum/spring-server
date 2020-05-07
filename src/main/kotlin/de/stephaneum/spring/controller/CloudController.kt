package de.stephaneum.spring.controller

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.FileRepo
import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.security.JwtService
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.util.FileCopyUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import java.io.IOException
import java.io.InputStreamReader
import java.io.UncheckedIOException

@Controller
class CloudController (
        private val fileService: FileService,
        private val jwtService: JwtService,
        private val fileRepo: FileRepo
) {

    @GetMapping("/api/cloud/download/file/{fileID}")
    fun download(@PathVariable fileID: Int, @RequestParam(required = false) download: Boolean?, @RequestParam(required = false) key: String?, @RequestParam(required = false) txt: Boolean?): Any {

        val file = fileRepo.findByIdOrNull(fileID) ?: return "404"

        if(key != null) {
            // for office documents
            val data = jwtService.getData(key)
            if(data?.get("fileID") != fileID.toString()) {
                return "403"
            }
        } else {
            val user = Session.get().user ?: return "403"
            if(!fileService.hasAccessToFile(user, file))
                return "403"
        }

        val resource = fileService.loadFileAsResource(file.path) ?: return "404"

        if(txt == true) {
            val content = try {
                InputStreamReader(resource.inputStream).use { reader -> FileCopyUtils.copyToString(reader) }
            } catch (e: IOException) {
                throw UncheckedIOException(e)
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("text/plain"))
                    .body(content)
        } else {
            return ResponseEntity.ok()
                    .contentLength(resource.contentLength())
                    .contentType(MediaType.parseMediaType(fileService.getMimeFromPath(file.path)))
                    .apply {
                        if(download == true)
                            header("Content-Disposition", "attachment; filename=\"" + file.generateFileName() + "\"")
                    }
                    .body(resource)
        }
    }
}