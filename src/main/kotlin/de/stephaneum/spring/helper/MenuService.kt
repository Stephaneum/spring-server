package de.stephaneum.spring.helper

import de.stephaneum.spring.database.Menu
import de.stephaneum.spring.database.MenuRepo
import de.stephaneum.spring.database.User
import de.stephaneum.spring.database.UserMenuRepo
import org.springframework.data.repository.findByIdOrNull
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

    fun getCategory(userID: Int): List<Menu> {
        val menu = menuRepo.findAll().toList()
        val category = menuRepo.findCategory(userID) ?: return emptyList()

        addChildren(category, menu)
        sortPriority(listOf(category))
        category.simplify()
        return listOf(category)
    }

    fun canWrite(user: User, menu: Menu): Boolean {
        return userMenuRepo.existsByUserAndMenuIsNull(user) || userMenuRepo.existsByUserAndMenu(user, menu)
    }

    fun isMenuAdmin(user: User): Boolean {
        return userMenuRepo.existsByUserAndMenuIsNull(user)
    }

    fun hasMenuWriteAccess(user: User): Boolean {
        return userMenuRepo.existsByUser(user)
    }

    /**
     * @return true if the user owns the menu because he is owner of the category
     */
    @Deprecated("permissions are now handled without categories", ReplaceWith("canWrite(user, menu)"))
    fun ownsCategory(userID: Int, menuID: Int): Boolean {
        val rootCategory = menuRepo.findCategory(userID) ?: return false

        var curr: Menu = menuRepo.findByIdOrNull(menuID) ?: return false
        var owns = false
        do {
            if(curr.id == rootCategory.id) {
                owns = true
                break
            }
            curr = curr.parent ?: break
        } while (true)
        return owns
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