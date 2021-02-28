package de.stephaneum.spring.rest

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.CodeService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/codes")
class CodeAPI (
        private val codeService: CodeService,
        private val codeRepo: CodeRepo
) {

    @GetMapping
    fun codes(): Any {
        val user = Session.get().user ?: return de.stephaneum.spring.helper.Response.Feedback(false, needLogin = true)
        if(user.code.role != ROLE_ADMIN)
            return de.stephaneum.spring.helper.Response.Feedback(false, message = "not allowed")
        return codeRepo.findByUsed(false)
    }

    @PostMapping("/add/{role}")
    fun addCode(@PathVariable role: Int): de.stephaneum.spring.helper.Response.Feedback {
        val user = Session.get().user ?: return de.stephaneum.spring.helper.Response.Feedback(false, needLogin = true)
        if(user.code.role != ROLE_ADMIN)
            return de.stephaneum.spring.helper.Response.Feedback(false, message = "not allowed")

        if(role != ROLE_STUDENT && role != ROLE_TEACHER && role != ROLE_GUEST)
            return de.stephaneum.spring.helper.Response.Feedback(false, message = "role not creatable")

        codeService.generateCode(role)
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