package de.stephaneum.spring.rest

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.DBHelper
import de.stephaneum.spring.helper.ErrorCode
import de.stephaneum.spring.helper.JsfEvent
import de.stephaneum.spring.helper.JsfService
import de.stephaneum.spring.rest.objects.Response
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.scheduler.Element
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/variable")
class VariableAPI (
        private val jsfService: JsfService,
        private val dbService: DBHelper,
        private val configScheduler: ConfigScheduler
) {

    @GetMapping
    fun getVariables(): Response.Variables {
        return Response.Variables(
                configScheduler.get(Element.storageTeacher)?.toIntOrNull() ?: 0,
                configScheduler.get(Element.storageStudent)?.toIntOrNull() ?: 0,
                configScheduler.get(Element.fileLocation) ?: "",
                configScheduler.get(Element.backupLocation) ?: "",
                configScheduler.get(Element.maxPictureSize)?.toIntOrNull() ?: 0,
                configScheduler.get(Element.passwordResetTimeout)?.toIntOrNull() ?: 0
        )
    }

    @PostMapping("/update")
    fun updateDirection(@RequestBody request: Config) {
        val user = Session.get().user ?: throw ErrorCode(401, "login")
        if(user.code.role != ROLE_ADMIN)
            throw ErrorCode(403, "admin only")

        val element = Element.valueOf(request.key)

        if(element == Element.fileLocation)
            dbService.updateFilePath(
                    oldPath = configScheduler.get(element) ?: throw ErrorCode(500, "no previous path"),
                    newPath = request.value ?: throw ErrorCode(400, "empty path"))

        configScheduler.save(element, request.value)
        jsfService.send(JsfEvent.SYNC_VARIABLES)
    }
}