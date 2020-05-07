package de.stephaneum.spring.rest

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.EventType
import de.stephaneum.spring.database.LogRepo
import de.stephaneum.spring.database.ROLE_ADMIN
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

object Response {
    data class LogInfo(val amount: Long)
    data class Log(val id: Int, val date: String, val type: String, val className: String, val typeID: Int, val info: String)
}

@RestController
@RequestMapping("/api/logs")
class LogAPI (
        private val logRepo: LogRepo
) {

    private val dateFormat = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy, HH:mm:ss", Locale.GERMANY).withZone(ZoneId.systemDefault())
    private val updateInterval = 30*1000 // 30 sec
    private var logs: List<Response.Log> = emptyList()
    private var lastUpdate = 0L

    @GetMapping("/info")
    fun logInfo(): Any {
        val user = Session.get().user ?: return de.stephaneum.spring.helper.Response.Feedback(false, needLogin = true)
        if(user.code.role != ROLE_ADMIN)
            return de.stephaneum.spring.helper.Response.Feedback(false, message = "not allowed")
        return Response.LogInfo(logRepo.count())
    }

    @GetMapping("/{amount}")
    fun logs(@PathVariable amount: Int): Any {
        val user = Session.get().user ?: return de.stephaneum.spring.helper.Response.Feedback(false, needLogin = true)
        if(user.code.role != ROLE_ADMIN)
            return de.stephaneum.spring.helper.Response.Feedback(false, message = "not allowed")

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