package de.stephaneum.spring.helper

import de.stephaneum.spring.database.Menu
import de.stephaneum.spring.database.MenuRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class MenuService {

    @Autowired
    private lateinit var menuRepo: MenuRepo

    fun getPublic(): List<Menu> {
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

    fun getCategory(userID: Int): List<Menu> {
        val menu = menuRepo.findAll().toList()
        val category = menuRepo.findCategory(userID) ?: return emptyList()

        addChildren(category, menu)
        sortPriority(listOf(category))
        simplify(category)
        return category.children
    }

    /**
     * @return true if the user owns the menu because he is owner of the category
     */
    fun ownsCategory(userID: Int, menuID: Int): Boolean {
        val rootCategory = menuRepo.findCategory(userID) ?: return false
        val target = menuRepo.findByIdOrNull(menuID) ?: return false

        var curr: Menu? = target
        var owns = false
        do {
            if(curr!!.id == rootCategory.id) {
                owns = true
                break
            }
            curr = curr.parent
        } while (curr != null);
        return owns
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