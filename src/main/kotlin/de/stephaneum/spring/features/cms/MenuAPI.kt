package de.stephaneum.spring.features.cms

import de.stephaneum.spring.database.Menu
import de.stephaneum.spring.database.MenuRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/menu")
class MenuAPI {

    @Autowired
    private lateinit var menuRepo: MenuRepo

    @GetMapping
    fun get(): List<Menu> {
        val menu = menuRepo.findPublic()

        var rootMenu = menu.filter { it.parent == null } // only the top level menus

        // add children for each top level menu
        rootMenu.forEach { addChildren(it, menu) }

        // sort by priority
        rootMenu = sortPriority(rootMenu)

        // remove unnecessary information
        rootMenu.forEach { simplify(it) }

        return rootMenu
    }

    private fun addChildren(parent: Menu, menu: List<Menu>) {
        parent.children = menu.filter { it.parent == parent }
        parent.children.forEach { addChildren(it, menu) }
    }

    private fun sortPriority(menu: List<Menu>): List<Menu> {
        val sorted = menu.sortedByDescending { it.priority }
        sorted.forEach { it.children = sortPriority(it.children) }
        return sorted
    }

    private fun simplify(menu: Menu) {
        menu.parent = null
        menu.user = null
        menu.image = null
        menu.password = null
        menu.approved = null
        menu.children.forEach { simplify(it) } // recursive call
    }
}