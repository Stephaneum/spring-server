package de.stephaneum.spring.rest

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.EMPTY_USER
import de.stephaneum.spring.database.PostRepo
import de.stephaneum.spring.database.ROLE_ADMIN
import de.stephaneum.spring.database.ROLE_NO_LOGIN
import de.stephaneum.spring.helper.MenuService
import de.stephaneum.spring.rest.objects.Response
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.scheduler.Element
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class Imprint(val imprint: String)

@RestController
@RequestMapping("/api")
class PublicAPI (
        private val configScheduler: ConfigScheduler,
        private val menuService: MenuService,
        private val postRepo: PostRepo
) {

    @GetMapping("/info")
    fun get(): Response.Info {
        val user = Session.get().user ?: EMPTY_USER
        val copyright = configScheduler.get(Element.copyright)
        val plan = Response.Plan(configScheduler.get(Element.planLocation) != null, configScheduler.get(Element.planInfo))
        val history = configScheduler.get(Element.history)
        val euSa = configScheduler.get(Element.euSa)
        val unapproved = when {
            user.code.role == ROLE_ADMIN || menuService.isMenuAdmin(user)   -> postRepo.countByApproved(false)
            user.code.role != ROLE_NO_LOGIN                                 -> postRepo.countByApprovedAndUser(false, user)
            else                                                            -> null
        }
        return Response.Info(user, menuService.hasMenuWriteAccess(user), menuService.getPublic(), copyright, plan, history, euSa, unapproved)
    }

    @GetMapping("/imprint")
    fun imprint(): Imprint {
        val content = configScheduler.get(Element.imprint) ?: ""
        return Imprint(content)
    }
}