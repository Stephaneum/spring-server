package de.stephaneum.spring.features.admin

import de.stephaneum.spring.Session
import de.stephaneum.spring.helper.checkIE
import de.stephaneum.spring.security.JwtService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import javax.servlet.http.HttpServletRequest

@Controller
class AdminController {

    @Autowired
    private lateinit var jwtService: JwtService

    @GetMapping("/admin-logs")
    fun logs(@RequestParam(required = false) key: String?, request: HttpServletRequest): String {

        if(checkIE(request))
            return "forward:/static/no-support-ie.html"

        // login
        if(key != null) {
            Session.get().user = jwtService.getUser(key)
            if(Session.get().user != null) {
                return "redirect:${request.requestURL}"
            }
        }

        return "admin-logs"
    }

    @GetMapping("/admin-codes")
    fun codes(@RequestParam(required = false) key: String?, request: HttpServletRequest): String {

        if(checkIE(request))
            return "forward:/static/no-support-ie.html"

        // login
        if(key != null) {
            Session.get().user = jwtService.getUser(key)
            if(Session.get().user != null) {
                return "redirect:${request.requestURL}"
            }
        }

        return "admin-codes"
    }

}