package de.stephaneum.spring.rest

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.*
import de.stephaneum.spring.rest.dto.Request
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.scheduler.Element
import de.stephaneum.spring.security.CryptoService
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

data class RegisterRequest(val code: String, val email: String, val password: String, val firstName: String, val lastName: String, val sex: Int, val schoolClass: String?)

@RestController
@RequestMapping("/api")
class AuthAPI (
        private val logService: LogService,
        private val inputValidator: InputValidator,
        private val userAgentDetector: UserAgentDetector,
        private val cryptoService: CryptoService,
        private val classService: ClassService,
        private val configScheduler: ConfigScheduler,
        private val userRepo: UserRepo,
        private val codeRepo: CodeRepo,
        private val classRepo: SchoolClassRepo
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
        logAuth(EventType.LOGIN, user, userAgent, forwardedIP, httpServletRequest)
    }

    @PostMapping("/check-code")
    fun checkCode(@RequestParam code: String): Code {
        return codeRepo.findByCodeAndUsed(code, false) ?: throw ErrorCode(400, "invalid code")
    }

    @ExperimentalUnsignedTypes
    @ExperimentalStdlibApi
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest,
                 @RequestHeader(value="User-Agent", required = false) userAgent: String?,
                 @RequestHeader(value="X-Forwarded-For", required = false) forwardedIP: String?,
                 httpServletRequest: HttpServletRequest) {

        val code = codeRepo.findByCodeAndUsed(request.code, false) ?: throw ErrorCode(400, "invalid code")

        if(request.firstName.isBlank() || request.lastName.isBlank() || request.email.isBlank() || request.password.isBlank())
            throw ErrorCode(400, "missing fields")

        if(!inputValidator.validateEmail(request.email))
            throw ErrorCode(423, "invalid email")

        if(userRepo.existsByEmail(request.email))
            throw ErrorCode(409, "email already exists")

        val schoolClass = when {
            request.schoolClass != null && request.schoolClass.isNotBlank() -> {
                val parsed = classService.parse(request.schoolClass) // throw 412 or 418
                classRepo.findByGradeAndSuffix(parsed.grade, parsed.suffix) ?: classRepo.save(SchoolClass(0, parsed.grade, parsed.suffix))
            }
            code.role == ROLE_STUDENT -> throw ErrorCode(417, "students must have school class")
            else -> null
        }

        val storage = if(code.role == ROLE_STUDENT)
            configScheduler.get(Element.storageStudent)?.toIntOrNull() ?: 0
        else
            configScheduler.get(Element.storageTeacher)?.toIntOrNull() ?: 0

        val user = userRepo.save(User(
                code = code,
                email = request.email,
                password = cryptoService.hashPassword(request.password),
                firstName = request.firstName,
                lastName = request.lastName,
                gender = request.sex,
                schoolClass = schoolClass,
                storage = storage
        ))

        code.used = true
        codeRepo.save(code)

        Session.get().user = user
        logAuth(EventType.REGISTER, user, userAgent, forwardedIP, httpServletRequest)
    }

    @PostMapping("/logout")
    fun logout() {
        Session.logout()
    }

    private fun logAuth(type: EventType, user: User, userAgent: String?, forwardedIP: String?, request: HttpServletRequest) {
        val ua = userAgent?.lowercase() ?: ""
        logService.log(type, user, "IP: ${resolveIP(forwardedIP, request)}, ${userAgentDetector.getBrowser(ua).repr}, ${userAgentDetector.getOS(ua).repr}")
    }
}