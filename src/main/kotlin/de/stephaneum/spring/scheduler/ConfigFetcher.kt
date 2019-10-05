package de.stephaneum.spring.scheduler

import de.stephaneum.spring.database.ConfigRepo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

enum class Element(val code: String, var value: String? = null) {
    fileLocation("speicherort"),
    planLocation("str_vertretung"),
    planInfo("str_vertretung_info"),
    backupLocation("backup_dir"),
    maxPictureSize("picture_size"),

    // special
    contact("str_kontakt"),
    imprint("str_impressum"),
    history("str_history"),
    euSa("str_eu_sa"),
    copyright("str_bottom"),
    dev("str_entwickler"),
    liveticker("str_liveticker"),
    events("str_termine"),
    coop("str_koop"),
    coopURL("str_koop_url")
}

@Service
class ConfigFetcher {

    /**
     * instead of fetching every time in the repo,
     * we should access these variables to reduce database calls
     */

    val logger = LoggerFactory.getLogger(ConfigFetcher::class.java)

    @Autowired
    private lateinit var configRepo: ConfigRepo

    private val configs = Element.values().toList()

    @Scheduled(initialDelay=3000, fixedDelay = 10000)
    fun update() {
        val db = configRepo.findAll()
        configs.forEach { c ->
            val dbConfig = db.first { it.key == c.code }
            if(c.value != dbConfig.value) {
                if(dbConfig.value?.length ?: 0 <= 200)
                    logger.info("${c.name}: ${c.value} -> ${dbConfig.value}")
                else
                    logger.info("${c.name} updated")
                c.value = dbConfig.value
            }
        }
    }

    fun get(element: Element): String? {
        return configs.first { it == element }.value
    }

    fun save(element: Element, value: String?) {
        val config = configRepo.findByKey(element.code) ?: return
        config.value = value
        configRepo.save(config)
    }
}