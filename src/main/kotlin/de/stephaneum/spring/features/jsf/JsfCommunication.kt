package de.stephaneum.spring.features.jsf

import de.stephaneum.spring.helper.RestClient
import de.stephaneum.spring.security.JwtService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.lang.Exception

enum class JsfEvent {
    SYNC_ALL,
    SYNC_MENU,
    SYNC_SPECIAL_TEXT,
    SYNC_VARIABLES,
    SYNC_PLAN
}

@Service
class JsfCommunication {

    val logger = LoggerFactory.getLogger(JsfCommunication::class.java)

    @Value("\${jsf.url}")
    private lateinit var jsfURL: String

    @Autowired
    private lateinit var jwtService: JwtService

    fun send(event: JsfEvent) {
        val token = jwtService.generateToken(mapOf("event" to event.toString()))
        try {
            RestClient.post("$jsfURL?event=$token", "")
        } catch (e: Exception) {
            logger.error(e.toString())
        }

    }
}