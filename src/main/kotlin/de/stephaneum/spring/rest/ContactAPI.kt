package de.stephaneum.spring.rest

import com.fasterxml.jackson.annotation.JsonInclude
import de.stephaneum.spring.helper.InputValidator
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

data class ContactMessage(val name: String?, val email: String?, val subject: String?, val message: String?, val privacy: Boolean?, val website: String?)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ContactResult(val success: Boolean, val error: String? = null)

private const val RATE_WINDOW = 60 * 60 * 1000L // 1 hour
private const val RATE_LIMIT_IP = 5             // per ip and window
private const val RATE_LIMIT_GLOBAL = 50        // all ips together per window, protects the gmail account

@RestController
@RequestMapping("/api/contact")
class ContactAPI (
        private val mailSender: JavaMailSender,
        private val inputValidator: InputValidator
) {

    private val logger = LoggerFactory.getLogger(ContactAPI::class.java)

    @Value("\${spring.mail.username}")
    private lateinit var mailFrom: String

    @Value("\${contact.mail.to}")
    private lateinit var mailTo: String

    // ip -> timestamps of sent messages
    private val history = mutableMapOf<String, MutableList<Long>>()

    @PostMapping("/message")
    fun send(@RequestBody request: ContactMessage, httpRequest: HttpServletRequest): ResponseEntity<ContactResult> {

        // honeypot: pretend success so that bots do not retry
        if (!request.website.isNullOrBlank())
            return ResponseEntity.ok(ContactResult(true))

        // remove line breaks to prevent header injection
        val name = request.name?.trim()?.replace(Regex("[\r\n]+"), " ") ?: ""
        val email = request.email?.trim() ?: ""
        val subject = request.subject?.trim()?.replace(Regex("[\r\n]+"), " ") ?: ""
        val message = request.message?.trim() ?: ""

        val error = when {
            name.isEmpty() || name.length > 100                                         -> "Bitte gib deinen Namen ein."
            email.length > 150 || !inputValidator.validateEmail(email.lowercase())      -> "Bitte gib eine gültige E-Mail-Adresse ein."
            subject.isEmpty() || subject.length > 150                                   -> "Bitte gib einen Betreff ein."
            message.isEmpty() || message.length > 5000                                  -> "Bitte schreibe eine Nachricht."
            request.privacy != true                                                     -> "Bitte bestätige die Datenschutzerklärung."
            else                                                                        -> null
        }
        if (error != null)
            return ResponseEntity.badRequest().body(ContactResult(false, error))

        if (!tryAcquire(httpRequest.remoteAddr))
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ContactResult(false, "Zu viele Nachrichten. Bitte versuche es später noch einmal."))

        val mail = SimpleMailMessage().apply {
            setFrom(mailFrom)
            setTo(mailTo)
            setReplyTo(email)
            setSubject("Begabungenleben.de: $subject")
            setText("Neue Nachricht über Begabungenleben.de\n\n" +
                    "Name: $name\n" +
                    "E-Mail: $email\n" +
                    "Betreff: $subject\n\n" +
                    "Nachricht:\n" +
                    "----------------------------------------\n" +
                    "$message\n" +
                    "----------------------------------------\n\n" +
                    "Diese Nachricht wurde über das Kontaktformular von Begabungenleben.de gesendet.")
        }

        return try {
            mailSender.send(mail)
            ResponseEntity.ok(ContactResult(true))
        } catch (e: MailException) {
            logger.error("could not send contact message", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ContactResult(false, "Die Nachricht konnte leider nicht versendet werden. Bitte versuche es später noch einmal."))
        }
    }

    /**
     * sliding window rate limit per ip and globally
     * @return true if the message may be sent
     */
    @Synchronized
    private fun tryAcquire(ip: String): Boolean {
        val now = System.currentTimeMillis()
        history.values.forEach { list -> list.removeIf { it < now - RATE_WINDOW } }
        history.values.removeIf { it.isEmpty() }

        val ipHistory = history.getOrPut(ip) { mutableListOf() }
        if (ipHistory.size >= RATE_LIMIT_IP || history.values.sumOf { it.size } >= RATE_LIMIT_GLOBAL)
            return false

        ipHistory.add(now)
        return true
    }
}
