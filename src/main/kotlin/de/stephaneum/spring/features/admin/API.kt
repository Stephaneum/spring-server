package de.stephaneum.spring.features.admin

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.Response.Feedback
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@RestController
@RequestMapping("/api/admin")
class AdminAPI {

    @Autowired
    private lateinit var logRepo: LogRepo

    private val dateFormat = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy, HH:mm:ss", Locale.GERMANY).withZone(ZoneId.systemDefault())
    private val updateInterval = 30*1000 // 30 sec
    private var logs: List<Response.Log> = emptyList()
    private var lastUpdate = 0L

    @GetMapping("/log/info")
    fun logInfo(): Any {
        val user = Session.get().user ?: return Feedback(false, needLogin = true)
        if(user.code.role != ROLE_ADMIN)
            return Feedback(false, message = "not allowed")
        return Response.LogInfo(logRepo.count())
    }

    @GetMapping("/log/{amount}")
    fun logs(@PathVariable amount: Int): Any {
        val user = Session.get().user ?: return Feedback(false, needLogin = true)
        if(user.code.role != ROLE_ADMIN)
            return Feedback(false, message = "not allowed")

        if(System.currentTimeMillis() - lastUpdate >= updateInterval) {
            logs = logRepo.findByOrderByTimestampDesc().map { log ->
                val type = EventType.valueOf(log.type)
                Response.Log(
                        id = log.id,
                        date = dateFormat.format(log.timestamp.toLocalDateTime()),
                        type = type.description,
                        className = type.className,
                        typeID = type.id,
                        info = log.info)
            }
            lastUpdate = System.currentTimeMillis()
        }

        if(amount >= logs.size)
            return logs
        else
            return logs.subList(0, amount)
    }
}