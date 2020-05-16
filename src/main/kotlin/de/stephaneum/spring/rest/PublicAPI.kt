package de.stephaneum.spring.rest

import de.stephaneum.spring.START_TIME
import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.Event
import de.stephaneum.spring.helper.MenuService
import de.stephaneum.spring.rest.objects.Response
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.scheduler.Element
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.time.ZonedDateTime

data class TextResponse(val text: String)
data class Stats(
        val studentCount: Int, val teacherCount: Int, val postCount: Int, val visitCount: Int,
        val upTime: Long, val startTime: ZonedDateTime,
        val dev: String?
)

@RestController
@RequestMapping("/api")
class PublicAPI (
        private val configScheduler: ConfigScheduler,
        private val menuService: MenuService,
        private val postRepo: PostRepo,
        private val userRepo: UserRepo
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
    fun imprint(): TextResponse {
        val content = configScheduler.get(Element.imprint) ?: ""
        return TextResponse(content)
    }

    @GetMapping("/contact")
    fun contact(): TextResponse {
        val content = configScheduler.get(Element.contact) ?: ""
        return TextResponse(content)
    }

    @GetMapping("/events")
    fun events(): List<Event> {
        return configScheduler.getDigestedEvents()
    }

    @GetMapping("/stats")
    fun stats(): Stats {
        val studentCount = userRepo.countByCodeRoleAndCodeUsed(ROLE_STUDENT, true)
        val teacherCount = userRepo.countByCodeRoleAndCodeUsed(ROLE_TEACHER, true)
        val postCount = postRepo.countByApproved(true)
        val dev = configScheduler.get(Element.dev)
        return Stats(
                studentCount = studentCount, teacherCount = teacherCount, postCount = postCount, visitCount = 420,
                upTime = Duration.between(START_TIME, ZonedDateTime.now()).toSeconds(), startTime = START_TIME,
                dev = dev
        )
    }
}