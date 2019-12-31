package de.stephaneum.spring.features.statics

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.ROLE_ADMIN
import de.stephaneum.spring.database.Static
import de.stephaneum.spring.database.StaticMode
import de.stephaneum.spring.database.StaticRepo
import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.helper.Response
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.scheduler.Element
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.*

data class AdminStaticInfo(val staticPath: String, val pages: List<Static>)

@RestController
@RequestMapping("/api/admin/static")
class StaticAPI {

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var staticRepo: StaticRepo

    @Autowired
    private lateinit var configScheduler: ConfigScheduler

    @GetMapping
    fun getFiles(): Any {
        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)
        if(user.code.role != ROLE_ADMIN) return Response.Feedback(false, message = "no admin")
        val location = configScheduler.get(Element.fileLocation) ?: return Response.Feedback(false, message = "internal error")

        return AdminStaticInfo(staticPath = "$location/${Static.FOLDER_NAME}", pages = staticRepo.findAll().toList())
    }

    @PostMapping("/toggle-mode/{id}")
    fun toggleMode(@PathVariable id: Int): Response.Feedback {
        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)
        if(user.code.role == ROLE_ADMIN) {
            val page = staticRepo.findByIdOrNull(id) ?: return Response.Feedback(false, message = "not found")
            page.mode = when(page.mode) {
                StaticMode.MIDDLE -> StaticMode.FULL_WIDTH
                StaticMode.FULL_WIDTH -> StaticMode.FULL_SCREEN
                StaticMode.FULL_SCREEN -> StaticMode.MIDDLE
            }
            staticRepo.save(page)
            return Response.Feedback(true)
        } else {
            return Response.Feedback(false, message = "only admin")
        }
    }

    @PostMapping("/delete/{id}")
    fun delete(@PathVariable id: Int): Response.Feedback {
        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)
        if(user.code.role == ROLE_ADMIN) {
            val page = staticRepo.findByIdOrNull(id) ?: return Response.Feedback(false, message = "not found")
            configScheduler.get(Element.fileLocation)?.let { location ->
                val success = fileService.deleteFile("$location/${Static.FOLDER_NAME}/${page.path}")

                if(success) {
                    staticRepo.deleteById(id)
                    return Response.Feedback(true)
                } else {
                    return Response.Feedback(false, message = "file could not be deleted")
                }
            }
            return Response.Feedback(false, message = "main file location unknown")
        } else {
            return Response.Feedback(false, message = "only admin")
        }
    }
}