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

    @Autowired
    private lateinit var codeRepo: CodeRepo

    private val dateFormat = DateTimeFormatter.ofPattern("EEEE, dd.MM.yyyy, HH:mm:ss", Locale.GERMANY).withZone(ZoneId.systemDefault())
    private val updateInterval = 30*1000 // 30 sec
    private val codePool = listOf('0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z')
    private val codeLength = 10
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

    @GetMapping("/codes")
    fun codes(): Any {
        val user = Session.get().user ?: return Feedback(false, needLogin = true)
        if(user.code.role != ROLE_ADMIN)
            return Feedback(false, message = "not allowed")
        return codeRepo.findByUsed(false)
    }

    @ExperimentalStdlibApi
    @PostMapping("/codes/add/{role}")
    fun addCode(@PathVariable role: Int): Feedback {
        val user = Session.get().user ?: return Feedback(false, needLogin = true)
        if(user.code.role != ROLE_ADMIN)
            return Feedback(false, message = "not allowed")

        if(role != ROLE_STUDENT && role != ROLE_TEACHER && role != ROLE_GUEST)
            return Feedback(false, message = "role not creatable")

        val codes = codeRepo.findAll()
        var next: String
        do {
            next = (CharArray(codeLength) { codePool.random() }).concatToString()
        } while(codes.any {it.code == next})

        codeRepo.save(Code(0, next, role, false))
        return Feedback(true)
    }

    @PostMapping("/codes/delete/{id}")
    fun deleteCode(@PathVariable id: Int): Feedback {
        val user = Session.get().user ?: return Feedback(false, needLogin = true)
        if(user.code.role != ROLE_ADMIN)
            return Feedback(false, message = "not allowed")

        codeRepo.deleteById(id)
        return Feedback(true)
    }
}