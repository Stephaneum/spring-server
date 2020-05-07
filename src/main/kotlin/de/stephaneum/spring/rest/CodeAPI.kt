package de.stephaneum.spring.rest

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/codes")
class CodeAPI (
        private val codeRepo: CodeRepo
) {

    private val codePool = listOf('0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z')
    private val codeLength = 10

    @GetMapping
    fun codes(): Any {
        val user = Session.get().user ?: return de.stephaneum.spring.helper.Response.Feedback(false, needLogin = true)
        if(user.code.role != ROLE_ADMIN)
            return de.stephaneum.spring.helper.Response.Feedback(false, message = "not allowed")
        return codeRepo.findByUsed(false)
    }

    @ExperimentalStdlibApi
    @PostMapping("/add/{role}")
    fun addCode(@PathVariable role: Int): de.stephaneum.spring.helper.Response.Feedback {
        val user = Session.get().user ?: return de.stephaneum.spring.helper.Response.Feedback(false, needLogin = true)
        if(user.code.role != ROLE_ADMIN)
            return de.stephaneum.spring.helper.Response.Feedback(false, message = "not allowed")

        if(role != ROLE_STUDENT && role != ROLE_TEACHER && role != ROLE_GUEST)
            return de.stephaneum.spring.helper.Response.Feedback(false, message = "role not creatable")

        val codes = codeRepo.findAll()
        var next: String
        do {
            next = (CharArray(codeLength) { codePool.random() }).concatToString()
        } while(codes.any {it.code == next})

        codeRepo.save(Code(0, next, role, false))
        return de.stephaneum.spring.helper.Response.Feedback(true)
    }

    @PostMapping("/delete/{id}")
    fun deleteCode(@PathVariable id: Int): de.stephaneum.spring.helper.Response.Feedback {
        val user = Session.get().user ?: return de.stephaneum.spring.helper.Response.Feedback(false, needLogin = true)
        if(user.code.role != ROLE_ADMIN)
            return de.stephaneum.spring.helper.Response.Feedback(false, message = "not allowed")

        codeRepo.deleteById(id)
        return de.stephaneum.spring.helper.Response.Feedback(true)
    }
}