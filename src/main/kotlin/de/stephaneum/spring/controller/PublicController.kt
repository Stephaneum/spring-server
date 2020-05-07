package de.stephaneum.spring.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class PublicController {

    @GetMapping("/")
    fun getRoot(): String {
        return "redirect:login"
    }

    @GetMapping("/login")
    fun getLogin(): String {
        return "login"
    }

}