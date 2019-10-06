package de.stephaneum.spring.features.plan

import de.stephaneum.spring.Session
import de.stephaneum.spring.security.JwtService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class PlanController {

    @Autowired
    private lateinit var jwtService: JwtService

    @GetMapping("/vertretungsplan-manager")
    fun get(@RequestParam(required = false) key: String?): String {

        // login
        if(key != null) {
            Session.get().user = jwtService.getUser(key)
            if(Session.get().user != null) {
                return "redirect:vertretungsplan-manager"
            }
        }

        return "plan-manager"
    }

}