package de.stephaneum.backend.scheduler

import de.stephaneum.backend.database.ConfigRepo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ConfigFetcher {

    val logger = LoggerFactory.getLogger(ConfigFetcher::class.java)

    @Autowired
    private lateinit var configRepo: ConfigRepo

    var location: String? = null
    var pdfLocation: String? = null

    @Scheduled(initialDelay=5000, fixedDelay = 10000)
    fun update() {
        val newLocation = configRepo.findByKey("speicherort")?.value
        val newPDFLocation = configRepo.findByKey("str_vertretung")?.value

        if(location != newLocation) {
            location = newLocation
            logger.info("Main Location: $location")
        }

        if(pdfLocation != newPDFLocation) {
            pdfLocation = newPDFLocation
            logger.info("PDF Location: $pdfLocation")
        }
    }
}