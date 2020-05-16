package de.stephaneum.spring.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class LegacyController {

    @GetMapping("/home.xhtml")
    fun home(@RequestParam(required = false) id: Int?): String {
        if(id != null) {
            return "redirect:/menu/$id"
        } else {
            return "redirect:/"
        }
    }

    @GetMapping("/beitrag.xhtml")
    fun post(@RequestParam id: Int): String {
        return "redirect:/beitrag/$id"
    }

    @GetMapping("/public/")
    fun file(@RequestParam file: String): String {
        return "redirect:/files/public/$file"
    }

    @GetMapping("/kontakt.xhtml")
    fun contact(): String {
        return "redirect:/kontakt"
    }

    @GetMapping("/impressum.xhtml")
    fun imprint(): String {
        return "redirect:/impressum"
    }
}