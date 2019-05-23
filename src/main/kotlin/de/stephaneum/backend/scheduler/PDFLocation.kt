package de.stephaneum.backend.scheduler

import de.stephaneum.backend.database.ConfigRepo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PDFLocation {

    val logger = LoggerFactory.getLogger(PDFLocation::class.java)

    @Autowired
    private lateinit var configRepo: ConfigRepo

    var location: String? = null

    @Scheduled(initialDelay=5000, fixedDelay = 10000)
    fun update() {
        val newLocation = configRepo.findByKey("str_vertretung")?.value

        if(location != newLocation) {
            location = newLocation
            logger.info("PDF location: $location")
        }
    }
}