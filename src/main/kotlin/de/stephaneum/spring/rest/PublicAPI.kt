package de.stephaneum.spring.rest

import de.stephaneum.spring.START_TIME
import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.*
import de.stephaneum.spring.rest.objects.Response
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.scheduler.Coop
import de.stephaneum.spring.scheduler.Element
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

data class TextResponse(val text: String)
data class Stats(
        val studentCount: Int, val teacherCount: Int, val postCount: Int, val visitCount: Int,
        val statsDay: List<DayCount>, val statsHour: List<HourCount>, val statsBrowser: List<BrowserCount>, val statsOS: List<OSCount>,
        val upTime: Long, val startTime: ZonedDateTime,
        val dev: String?
)
data class HomeData(val slider: List<Slider>, val menu: Menu, val posts: List<Post>, val events: List<Event>, val studentCount: Int, val teacherCount: Int, val years: Int, val coop: List<Coop>)

@RestController
@RequestMapping("/api")
class PublicAPI (
        private val postAPI: PostAPI,
        private val configScheduler: ConfigScheduler,
        private val countService: CountService,
        private val menuService: MenuService,
        private val postRepo: PostRepo,
        private val userRepo: UserRepo,
        private val sliderRepo: SliderRepo,
        private val menuRepo: MenuRepo
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

    @GetMapping("/home")
    fun home(): HomeData {
        val menu = configScheduler.get(Element.defaultMenu)?.toIntOrNull() ?: 0
        var posts = postAPI.get(null, null, menu, false) as List<Post>
        if(posts.size > 3)
            posts = posts.subList(0, 3)
        return HomeData(
                slider = sliderRepo.findByOrderByIndex(),
                menu = menuRepo.findByIdOrNull(menu) ?: Menu(name = "Error"),
                posts = posts,
                events = configScheduler.getDigestedEvents(),
                studentCount = userRepo.countByCodeRoleAndCodeUsed(ROLE_STUDENT, true),
                teacherCount = userRepo.countByCodeRoleAndCodeUsed(ROLE_TEACHER, true),
                years = LocalDate.now().year - 1325,
                coop = configScheduler.getDigestedCoop()
        )
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
        val statsDay = countService.getStatsDay()
        val visitCount = statsDay.sumBy { it.count }
        return Stats(
                studentCount = studentCount, teacherCount = teacherCount, postCount = postCount, visitCount = visitCount,
                statsDay = statsDay, statsHour = countService.getStatsHour(), statsBrowser = countService.getStatsBrowser(), statsOS = countService.getStatsOS(),
                upTime = Duration.between(START_TIME, ZonedDateTime.now()).toSeconds(), startTime = START_TIME,
                dev = dev
        )
    }
}