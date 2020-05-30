package de.stephaneum.spring.blackboard

import de.stephaneum.spring.Permission
import de.stephaneum.spring.Session
import de.stephaneum.spring.helper.ErrorCode
import de.stephaneum.spring.helper.resolveIP
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest


@RestController
@RequestMapping("/blackboard/api")
class BlackboardPublicAPI (
        @Value("\${blackboard.password}")
        private val password: String,
        private val blackboardScheduler: BlackboardScheduler,
        private val activeClientsScheduler: ActiveClientsScheduler
) {

    @PostMapping("/login")
    fun login(@RequestBody request: Request.Login) {

        if(request.password != this.password) {
            Thread.sleep(2000)
            throw ErrorCode(403, "wrong password")
        }

        Session.login(Permission.BLACKBOARD)
    }

    @PostMapping("/logout")
    fun logout(): Response.Feedback {
        Session.logout()

        return Response.Feedback(true)
    }

    @GetMapping("/timestamp")
    fun timestamp(@RequestHeader(value="X-Forwarded-For", required = false) forwardedIP: String?,
                  request: HttpServletRequest): Response.Timestamp {

        // track active clients
        val ip = resolveIP(forwardedIP, request)
        activeClientsScheduler.activeClients[ip] = System.currentTimeMillis()

        if(blackboardScheduler.paused)
            return Response.Timestamp(null)
        else
            return Response.Timestamp(blackboardScheduler.active.lastUpdate.time)
    }
}