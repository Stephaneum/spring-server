package de.stephaneum.spring.helper

import de.stephaneum.spring.database.Menu
import de.stephaneum.spring.database.MenuRepo
import de.stephaneum.spring.database.User
import de.stephaneum.spring.database.UserMenuRepo
import org.springframework.stereotype.Service

@Service
class MenuService (
        private val menuRepo: MenuRepo,
        private val userMenuRepo: UserMenuRepo
) {

    fun getPublic(keepPassword: Boolean = false): List<Menu> {
        val menu = menuRepo.findPublic()

        var rootMenu = menu.filter { it.parent == null } // only the top level menus

        // add children for each top level menu
        rootMenu.forEach { addChildren(it, menu) }

        // sort by priority
        rootMenu = sortPriority(rootMenu)

        // remove unnecessary information
        rootMenu.forEach { it.simplify(keepPassword) }

        return rootMenu
    }

    fun getWritable(user: User): List<Menu> {

        if(userMenuRepo.existsByUserAndMenuIsNull(user))
            return getPublic(keepPassword = true)

        val menus = userMenuRepo.findByMenu(user)
        menus.forEach { addChildren(it, menuRepo.findAll().toList()) }
        menus.forEach { it.simplify() }
        return menus
    }

    fun canWrite(user: User, menu: Menu): Boolean {
        if(isMenuAdmin(user))
            return true

        var curr: Menu? = menu
        while (curr != null) {
            if (userMenuRepo.existsByUserAndMenu(user, curr))
                return true

            curr = curr.parent
        }

        return false
    }

    fun isMenuAdmin(user: User): Boolean {
        return userMenuRepo.existsByUserAndMenuIsNull(user)
    }

    fun hasMenuWriteAccess(user: User): Boolean {
        return userMenuRepo.existsByUser(user)
    }

    /**
     * @param parent children will be added to the parent
     * @param menu all menu items one layer, children will be find using this list
     */
    private fun addChildren(parent: Menu, menu: List<Menu>) {
        parent.children = menu.filter { it.parent == parent }
        parent.children.forEach { addChildren(it, menu) }
    }

    private fun sortPriority(menu: List<Menu>): List<Menu> {
        val sorted = menu.sortedByDescending { it.priority }
        sorted.forEach { it.children = sortPriority(it.children) }
        return sorted
    }
}