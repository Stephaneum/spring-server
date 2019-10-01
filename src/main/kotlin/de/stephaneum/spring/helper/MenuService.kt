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
}