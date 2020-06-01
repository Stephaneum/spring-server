package de.stephaneum.spring.rest

import de.stephaneum.spring.START_TIME
import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.*
import de.stephaneum.spring.rest.objects.Response
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.scheduler.Coop
import de.stephaneum.spring.scheduler.Element
import de.stephaneum.spring.security.CryptoService
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.*
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

data class TextResponse(val text: String)
data class Stats(
        val studentCount: Int, val teacherCount: Int, val postCount: Int, val visitCount: Int,
        val statsDay: List<DayCount>, val statsHour: List<HourCount>, val statsBrowser: List<BrowserCount>, val statsOS: List<OSCount>, val statsCloud: List<CloudCount>,
        val upTime: Long, val startTime: ZonedDateTime,
        val dev: String?
)
data class HomeData(val slider: List<Slider>, val menu: Menu, val liveticker: String?, val posts: List<Post>, val events: List<Event>, val studentCount: Int, val teacherCount: Int, val years: Int, val coop: List<Coop>, val coopLink: String?)
data class SectionData(val slider: List<Slider>, val menu: Menu, val locked: Boolean, val posts: List<Post>, val postCount: Int, val events: List<Event>)
data class UnlockSection(val menu: Int, val password: String)
data class UnlockPost(val post: Int, val password: String)

@RestController
@RequestMapping("/api")
class PublicAPI (
        private val globalStateService: GlobalStateService,
        private val cryptoService: CryptoService,
        private val cloudStatsService: CloudStatsService,
        private val postService: PostService,
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

        val state = globalStateService.state
        if(state != GlobalState.OK) {
            return Response.Info(state, EMPTY_USER, false, emptyList(), null, Response.Plan(false, null), null, null, null)
        }

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
        return Response.Info(state, user, menuService.hasMenuWriteAccess(user), menuService.getPublic(), copyright, plan, history, euSa, unapproved)
    }

    @GetMapping("/home")
    fun home(): HomeData {
        val menu = configScheduler.get(Element.defaultMenu)?.toIntOrNull() ?: 0
        val posts = postService.getPosts(menu, pageable = PageRequest.of(0, 3))
                               .onEach { post -> if(post.password != null && !Session.hasAccess(post)) post.content = "" }
        return HomeData(
                slider = sliderRepo.findByOrderByIndex(),
                menu = menuRepo.findByIdOrNull(menu) ?: Menu(name = "Error"),
                liveticker = configScheduler.get(Element.liveticker),
                posts = posts,
                events = getNextEvents(),
                studentCount = userRepo.countByCodeRoleAndCodeUsed(ROLE_STUDENT, true),
                teacherCount = userRepo.countByCodeRoleAndCodeUsed(ROLE_TEACHER, true),
                years = LocalDate.now().year - 1325,
                coop = configScheduler.getDigestedCoop(),
                coopLink = configScheduler.get(Element.coopURL)
        )
    }

    @GetMapping("/section/{menuId}")
    fun section(@PathVariable menuId: Int, @RequestParam(required = false) page: Int?): SectionData {

        // menu
        val menu = menuRepo.findByIdOrNull(menuId) ?: throw ErrorCode(404, "menu not found")
        val locked = menu.password != null && !Session.hasAccess(menu)
        menu.password = null // remove password, so that client cannot see it
        menuService.addChildrenPlain(menu)

        // posts
        val posts = if (locked) emptyList() else postService.getPosts(menu.id, pageable = PageRequest.of(page?.minus(1) ?: 0, 5))
        posts.filter { post -> post.password != null && !Session.hasAccess(post) }
             .forEach { post -> post.content = "" }

        return SectionData(
                slider = sliderRepo.findByOrderByIndex(),
                menu = menu,
                locked = locked,
                posts = posts,
                postCount = postRepo.countByMenuAndApproved(menu, true),
                events = getNextEvents()
        )
    }

    @PostMapping("/unlock/section")
    fun unlockSection(@RequestBody request: UnlockSection) {
        val (session, _) = Session.createIfNotExists()
        val menu = menuRepo.findByIdOrNull(request.menu) ?: throw ErrorCode(404, "menu not found")
        if(request.password != menu.password)
            throw ErrorCode(403, "invalid password")

        session.unlockedSections.add(menu.id)
    }

    @ExperimentalUnsignedTypes
    @PostMapping("/unlock/post")
    fun unlockPost(@RequestBody request: UnlockPost) {
        val (session, _) = Session.createIfNotExists()
        val post = postRepo.findByIdOrNull(request.post) ?: throw ErrorCode(404, "post not found")
        if(cryptoService.hashMD5(request.password) != post.password)
            throw ErrorCode(403, "invalid password")

        session.unlockedPosts.add(post.id)
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
                statsDay = statsDay, statsHour = countService.getStatsHour(), statsBrowser = countService.getStatsBrowser(), statsOS = countService.getStatsOS(), statsCloud = cloudStatsService.getStats(),
                upTime = Duration.between(START_TIME, ZonedDateTime.now()).seconds, startTime = START_TIME,
                dev = dev
        )
    }

    private fun getNextEvents(): List<Event> {
        val now = LocalDateTime.now()
        return configScheduler
                .getDigestedEvents()
                .filter { (it.end != null && it.end > now) || it.start > now }
                .take(3)
    }
}