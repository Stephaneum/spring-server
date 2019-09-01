package de.stephaneum.spring.backup

import de.stephaneum.spring.scheduler.ConfigFetcher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.File

@Service
class DirectoryScheduler {

    /**
     * this scheduler make sure that all modules have their directories
     */

    private val logger = LoggerFactory.getLogger(DirectoryScheduler::class.java)

    @Autowired
    private lateinit var configFetcher: ConfigFetcher

    @Scheduled(initialDelay=5000, fixedDelay = 10000)
    fun update() {
        configFetcher.backupLocation?.let {
            MODULES.forEach { module ->
                val file = File("$it/${module.code}")
                if(!file.exists()) {
                    logger.info("create missing folder: ${file.absolutePath}")
                    file.mkdirs()
                }
            }
        }
    }
}