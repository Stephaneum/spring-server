package de.stephaneum.spring.blackboard

import de.stephaneum.spring.Permission
import de.stephaneum.spring.Session
import de.stephaneum.spring.helper.resolveIP
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest


@RestController
@RequestMapping("/blackboard/api")
class BlackboardPublicAPI {

    @Value("\${blackboard.password}")
    private lateinit var password: String

    @Autowired
    private lateinit var activeClientsScheduler: ActiveClientsScheduler

    @Autowired
    private lateinit var blackboardScheduler: BlackboardScheduler

    @PostMapping("/login")
    fun login(@RequestBody request: Request.Login): Response.Feedback {
        if(request.password == this.password) {
            Session.login(Permission.BLACKBOARD)
            return Response.Feedback(true)
        } else {
            Thread.sleep(2000)
            return Response.Feedback(false)
        }
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