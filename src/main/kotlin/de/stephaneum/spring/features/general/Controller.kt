package de.stephaneum.spring.features.general

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class GeneralController {

    @GetMapping("/login")
    fun get(): String {
        return "login"
    }

    @GetMapping("/")
    fun getRoot(): String {
        return "redirect:login"
    }

}