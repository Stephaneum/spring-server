package de.stephaneum.spring.rest

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.EventType
import de.stephaneum.spring.database.UserRepo
import de.stephaneum.spring.helper.ErrorCode
import de.stephaneum.spring.helper.LogService
import de.stephaneum.spring.helper.UserAgentDetector
import de.stephaneum.spring.helper.resolveIP
import de.stephaneum.spring.rest.objects.Request
import de.stephaneum.spring.security.CryptoService
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api")
class AuthAPI (
        private val logService: LogService,
        private val userAgentDetector: UserAgentDetector,
        private val cryptoService: CryptoService,
        private val userRepo: UserRepo
) {

    @ExperimentalUnsignedTypes
    @PostMapping("/login")
    fun login(@RequestBody request: Request.Login,
              @RequestHeader(value="User-Agent", required = false) userAgent: String?,
              @RequestHeader(value="X-Forwarded-For", required = false) forwardedIP: String?,
              httpServletRequest: HttpServletRequest) {
        val user = userRepo.findByEmail(request.email ?: "") ?: throw ErrorCode(403, "Login failed")
        if(!cryptoService.checkPassword(request.password ?: "", user.password)) {
            Thread.sleep(2000)
            throw ErrorCode(403, "Login failed")
        }

        Session.get().user = user

        val ua = userAgent?.toLowerCase() ?: ""
        logService.log(EventType.LOGIN, user, "IP: ${resolveIP(forwardedIP, httpServletRequest)}, ${userAgentDetector.getBrowser(ua).repr}, ${userAgentDetector.getOS(ua).repr}")
    }

    @PostMapping("/logout")
    fun logout() {
        Session.logout()
    }
}