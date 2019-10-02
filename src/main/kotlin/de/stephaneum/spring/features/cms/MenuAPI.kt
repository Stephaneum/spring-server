package de.stephaneum.spring.features.cms

import de.stephaneum.spring.database.Menu
import de.stephaneum.spring.helper.MenuService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/menu")
class MenuAPI {

    @Autowired
    private lateinit var menuService: MenuService

    @GetMapping
    fun get(): List<Menu> {
        return menuService.getPublic()
    }
}