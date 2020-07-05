package de.stephaneum.spring.scheduler

import de.stephaneum.spring.database.Config
import de.stephaneum.spring.database.ConfigRepo
import de.stephaneum.spring.helper.parser.Event
import de.stephaneum.spring.helper.parser.EventParser
import de.stephaneum.spring.helper.GlobalState
import de.stephaneum.spring.helper.GlobalStateService
import de.stephaneum.spring.helper.parser.Coop
import de.stephaneum.spring.helper.parser.CoopParser
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

// using lowercase because frontend uses those as keys
enum class Element(val code: String, val info: String, val defaultValue: String?, var value: String? = null) {
    fileLocation("speicherort", "Speicherort", null),
    backupLocation("backup_dir", "Backup-Ort", null),
    planLocation("str_vertretung", "Vertretungsplan", null),
    planInfo("str_vertretung_info", "Vertretungsplan-Info", null),
    maxPictureSize("picture_size", "Max. Bildgröße in Beiträgen", "256000"),
    defaultMenu("default_gruppe_id", "Menüeintrag für die Startseite", null),
    storageTeacher("storage_lehrer", "Speicherplatz Lehrer", "209715200"),
    storageStudent("storage_schueler", "Speicherplatz Schueler", "104857600"),
    passwordResetTimeout("timeout_passwort_vergessen", "Timeout Passwort Zuruecksetzen", "7200000"),
    indexStatsHour("stats_stunden_index", "Index stats-hour", "0"),

    // special
    contact("str_kontakt", "Kontakt", null),
    imprint("str_impressum", "Impressum", null),
    history("str_history", "Geschichte", null),
    euSa("str_eu_sa", "EU und S.-A.", null),
    copyright("str_bottom", "Copyright", null),
    dev("str_entwickler", "Entwicklung", null),
    liveticker("str_liveticker", "Live-Ticker", null),
    events("str_termine", "Termine", null),
    coop("str_koop", "Koop.-partner", null),
    coopURL("str_koop_url", "Koop.-partner (URL)", null)
}

@Service
class ConfigScheduler {

    /**
     * instead of fetching every time in the repo,
     * we should access these variables to reduce database calls
     */

    private val logger = LoggerFactory.getLogger(ConfigScheduler::class.java)

    @Autowired
    private lateinit var configRepo: ConfigRepo

    @Autowired
    private lateinit var globalStateService: GlobalStateService

    @Autowired
    private lateinit var eventParser: EventParser

    @Autowired
    private lateinit var coopParser: CoopParser

    private val configs = Element.values().toList()
    private var digestedEvents: List<Event> = emptyList()
    private var digestedCoop: List<Coop> = emptyList()

    @Scheduled(initialDelay=3000, fixedDelay = 5000)
    fun tick() {
        val db = configRepo.findAll()

        if(globalStateService.state == GlobalState.INITIALIZING) {
            if(db.none()) {
                globalStateService.state = GlobalState.NEED_INIT
                logger.info("Detected empty database. Visit website to initialize!")
            } else {
                globalStateService.state = GlobalState.OK
            }
        }

        if(globalStateService.noScheduler)
            return

        update(db)
    }

    fun update(db: Iterable<Config> = configRepo.findAll()) {
        configs.forEach { element ->
            val dbConfig = db.firstOrNull { it.key == element.code }

            if(dbConfig == null) {
                logger.error("${element.code} has no database entry")
            } else if(element.value != dbConfig.value) {
                val value = if(dbConfig.value?.length ?: 0 <= 100) dbConfig.value?.replace("\n", " ") else dbConfig.value?.substring(0, 100)?.replace("\n", " ") + "..."
                logger.info("${element.name}: ${element.value} -> $value")
                element.value = dbConfig.value
                digest(element, dbConfig.value)
            }
        }
    }

    fun get(element: Element): String? {
        return configs.first { it == element }.value
    }

    fun getDigestedEvents(): List<Event> {
        return digestedEvents
    }

    fun getDigestedCoop(): List<Coop> {
        return digestedCoop
    }

    fun save(element: Element, value: String?) {

        // digest value
        val actualValue = when(element) {
            Element.fileLocation, Element.backupLocation -> if(value != null) digestPath(value) else null
            else -> value
        }

        val config = configRepo.findByKey(element.code) ?: return
        config.value = actualValue
        configRepo.save(config)
        element.value = actualValue // also set the locally stored one
        digest(element, value) // update the digested values (events, coop)
    }

    // initialize based on backup or new instance
    fun initialize(fileLocation: String, backupLocation: String, defaultMenu: Int = 0) {

        val configs = Element.values().map { c ->
            val value = when (c) {
                Element.fileLocation -> digestPath(fileLocation)
                Element.backupLocation -> digestPath(backupLocation)
                Element.defaultMenu -> defaultMenu.toString()
                else -> c.defaultValue
            }
            Config(key = c.code, value = value)
        }

        configRepo.deleteAll()
        configRepo.saveAll(configs)
        update()
    }

    // drop last / and replace \ to /
    private fun digestPath(path: String): String {
        var curr = path.replace('\\', '/')
        if(curr.endsWith('/')) {
            curr = curr.dropLast(1)
        }
        return curr
    }

    // some elements require digestion (i.e. events, coop)
    private fun digest(element: Element, raw: String?) {
        when (element) {
            Element.events -> digestedEvents = eventParser.parse(raw ?: "")
            Element.coop -> digestedCoop = coopParser.parse(raw ?: "")
            else -> {}
        }
    }
}