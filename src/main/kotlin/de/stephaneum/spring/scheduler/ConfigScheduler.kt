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

    private val configs = Element.values().toList()
    private var digestedEvents: List<Event> = emptyList()

    @Scheduled(initialDelay=3000, fixedDelay = 10000)
    fun update() {
        val db = configRepo.findAll()
        configs.forEach { c ->
            val dbConfig = db.first { it.key == c.code }
            if(c.value != dbConfig.value) {
                val value = if(dbConfig.value?.length ?: 0 <= 100) dbConfig.value?.replace("\n", " ") else dbConfig.value?.substring(0, 100)?.replace("\n", " ") + "..."
                logger.info("${c.name}: ${c.value} -> $value")
                c.value = dbConfig.value

                // update digested
                when (c) {
                    Element.events -> digestedEvents = eventParser.parse(dbConfig.value ?: "")
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

    fun save(element: Element, value: String?) {
        val config = configRepo.findByKey(element.code) ?: return
        config.value = value
        configRepo.save(config)
        element.value = value // also set the locally stored one
    }
}