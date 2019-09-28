package de.stephaneum.spring.scheduler

import de.stephaneum.spring.database.ConfigRepo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class ConfigFetcher {

    /**
     * instead of fetching every time in the repo,
     * we should access these variables to reduce database calls
     */

    val logger = LoggerFactory.getLogger(ConfigFetcher::class.java)

    @Autowired
    private lateinit var configRepo: ConfigRepo

    var fileLocation: String? = null
    var planLocation: String? = null
    var backupLocation: String? = null
    var copyright: String? = null

    @Scheduled(initialDelay=3000, fixedDelay = 10000)
    fun update() {
        val newLocation = configRepo.findByKey("speicherort")?.value
        val newPlanLocation = configRepo.findByKey("str_vertretung")?.value
        val newBackupLocation = configRepo.findByKey("backup_dir")?.value
        val newCopyright = configRepo.findByKey("str_bottom")?.value

        if(fileLocation != newLocation) {
            fileLocation = newLocation
            logger.info("Main Location: $fileLocation")
        }

        if(planLocation != newPlanLocation) {
            planLocation = newPlanLocation
            logger.info("PDF Location: $planLocation")
        }

        if(backupLocation != newBackupLocation) {
            backupLocation = newBackupLocation
            logger.info("Backup Location: $backupLocation")
        }

        if(copyright != newCopyright) {
            copyright = newCopyright
            logger.info("Copyright: $copyright")
        }
    }
}