package de.stephaneum.spring.scheduler

import de.stephaneum.spring.database.Config
import de.stephaneum.spring.database.ConfigRepo
import de.stephaneum.spring.helper.Event
import de.stephaneum.spring.helper.EventParser
import de.stephaneum.spring.helper.GlobalState
import de.stephaneum.spring.helper.GlobalStateService
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

data class Coop(val country: String, val tooltip: String?, val link: String?)

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

    private val configs = Element.values().toList()
    private var digestedEvents: List<Event> = emptyList()
    private var digestedCoop: List<Coop> = emptyList()

    @Scheduled(initialDelay=3000, fixedDelay = 5000)
    fun update() {
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

        configs.forEach { c ->
            val dbConfig = db.firstOrNull { it.key == c.code }

            if(dbConfig == null) {
                logger.error("${c.code} has no database entry")
            } else if(c.value != dbConfig.value) {
                val value = if(dbConfig.value?.length ?: 0 <= 100) dbConfig.value?.replace("\n", " ") else dbConfig.value?.substring(0, 100)?.replace("\n", " ") + "..."
                logger.info("${c.name}: ${c.value} -> $value")
                c.value = dbConfig.value

                // update digested
                when (c) {
                    Element.events -> digestedEvents = eventParser.parse(dbConfig.value ?: "")
                    Element.coop -> {
                        val raw = dbConfig.value ?: ""
                        digestedCoop = raw.split(";").map { coopRaw ->
                            val link = when(val index = coopRaw.indexOf('[')) {
                                -1 -> null
                                else -> coopRaw.substring(index + 1, coopRaw.length - 1)
                            }
                            val tooltip = when(val index = coopRaw.indexOf('(')) {
                                -1 -> null
                                else -> when(val closeIndex = coopRaw.indexOf(')')) {
                                    -1 -> coopRaw.substring(index + 1)
                                    else -> coopRaw.substring(index + 1, closeIndex)
                                }
                            }
                            val country = when {
                                tooltip != null -> coopRaw.substring(0, coopRaw.indexOf('('))
                                link != null -> coopRaw.substring(0, coopRaw.indexOf('['))
                                else -> coopRaw
                            }
                            Coop(country, tooltip, link)
                        }
                    }
                    else -> {}
                }
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
        val config = configRepo.findByKey(element.code) ?: return
        config.value = value
        configRepo.save(config)
        element.value = value // also set the locally stored one
    }

    fun initialize(fileLocation: String, backupLocation: String, defaultMenu: Int = 0) {

        val configs = Element.values().map { c ->
            val value = when (c) {
                Element.fileLocation -> fileLocation
                Element.backupLocation -> backupLocation
                Element.defaultMenu -> defaultMenu.toString()
                else -> c.defaultValue
            }
            Config(key = c.code, value = value)
        }

        configRepo.deleteAll()
        configRepo.saveAll(configs)
        update()
    }
}