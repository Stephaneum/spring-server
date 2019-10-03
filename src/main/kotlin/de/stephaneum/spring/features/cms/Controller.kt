package de.stephaneum.spring.features.cms

import de.stephaneum.spring.Session
import de.stephaneum.spring.security.JwtService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class CMSController {

    @Autowired
    private lateinit var jwtService: JwtService

    @GetMapping("/beitrag-manager")
    fun get(@RequestParam(required = false) key: String?): String {

        // login
        if(key != null) {
            Session.get().user = jwtService.getUser(key)
            if(Session.get().user != null) {
                return "redirect:beitrag-manager"
            }
        }

        return "post-manager"
    }

}