package de.stephaneum.spring.scheduler

import de.stephaneum.spring.database.ConfigRepo
import de.stephaneum.spring.helper.Event
import de.stephaneum.spring.helper.EventParser
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

// using lowercase because frontend uses those as keys
enum class Element(val code: String, val info: String, var value: String? = null) {
    fileLocation("speicherort", "Speicherort"),
    backupLocation("backup_dir", "Backup-Ort"),
    planLocation("str_vertretung", "Vertretungsplan"),
    planInfo("str_vertretung_info", "Vertretungsplan-Info"),
    maxPictureSize("picture_size", "Max. Bildgröße in Beiträgen"),
    defaultMenu("default_gruppe_id", "Menüeintrag für die Startseite"),
    storageTeacher("storage_lehrer", "Speicherplatz Lehrer"),
    storageStudent("storage_schueler", "Speicherplatz Schueler"),
    passwordResetTimeout("timeout_passwort_vergessen", "Timeout Passwort Zuruecksetzen"),
    indexStatsHour("stats_stunden_index", "Index stats-hour"),

    // special
    contact("str_kontakt", "Kontakt"),
    imprint("str_impressum", "Impressum"),
    history("str_history", "Geschichte"),
    euSa("str_eu_sa", "EU und S.-A."),
    copyright("str_bottom", "Copyright"),
    dev("str_entwickler", "Entwicklung"),
    liveticker("str_liveticker", "Live-Ticker"),
    events("str_termine", "Termine"),
    coop("str_koop", "Koop.-partner"),
    coopURL("str_koop_url", "Koop.-partner (URL)")
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
    private lateinit var eventParser: EventParser

    final var initialized = false; private set
    final var needInit = true // set true after init from outside

    private val configs = Element.values().toList()
    private var digestedEvents: List<Event> = emptyList()
    private var digestedCoop: List<Coop> = emptyList()

    @Scheduled(initialDelay=3000, fixedDelay = 5000)
    fun update() {
        val db = configRepo.findAll()

        if(!initialized) {
            needInit = db.none()
            initialized = true

            if(needInit)
                logger.info("Detected empty database. Visit website to initialize!")
        }

        if(needInit)
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
}