package de.stephaneum.spring.features.cms

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/cms")
class CMSController {

    @GetMapping("/post-manager")
    fun get(): String {
        return "post-manager"
    }

}