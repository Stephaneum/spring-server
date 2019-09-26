package de.stephaneum.spring.features.cms

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/cms/menu/api")
class MenuAPI {

    @GetMapping
    fun get() {

    }
}