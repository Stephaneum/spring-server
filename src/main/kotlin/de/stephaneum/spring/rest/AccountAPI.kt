package de.stephaneum.spring.rest

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.FileRepo
import de.stephaneum.spring.database.UserRepo
import de.stephaneum.spring.helper.ErrorCode
import de.stephaneum.spring.helper.InputValidator
import de.stephaneum.spring.rest.dto.Request
import de.stephaneum.spring.rest.dto.Response
import de.stephaneum.spring.security.CryptoService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/account")
class AccountAPI (
        private val cryptoService: CryptoService,
        private val inputValidator: InputValidator,
        private val userRepo: UserRepo,
        private val fileRepo: FileRepo
) {

    // additional info
    @GetMapping("/info")
    fun getInfo(): Response.AccountInfo {
        val me = Session.getUser()
        val schoolClass = when (val c = me.schoolClass) {
            null -> "keine"
            else -> "${c.grade}${c.suffix}"
        }

        val used = fileRepo.calcStorageUsed(me.id)
        val total = me.storage

        return Response.AccountInfo(schoolClass, used, total)
    }

    @PostMapping("/email")
    fun updateEmail(@RequestBody request: Request.Email) {
        val me = Session.getUser()

        if(request.email.isBlank())
            throw ErrorCode(400, "empty email")

        if(userRepo.existsByEmail(request.email))
            throw ErrorCode(409, "email in use")

        if(!inputValidator.validateEmail(request.email))
            throw ErrorCode(410, "email not valid")

        me.email = request.email
        userRepo.save(me)
    }

    @ExperimentalStdlibApi
    @ExperimentalUnsignedTypes
    @PostMapping("/password")
    fun updatePassword(@RequestBody request: Request.ChangePassword) {
        val me = Session.getUser()

        if(!cryptoService.checkPassword(request.oldPassword, me.password))
            throw ErrorCode(403, "wrong password")

        me.password = cryptoService.hashPassword(request.newPassword)
        userRepo.save(me)
    }
}