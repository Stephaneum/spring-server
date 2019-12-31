package de.stephaneum.spring.features.backup

import de.stephaneum.spring.scheduler.Element
import de.stephaneum.spring.scheduler.ConfigScheduler
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
    private lateinit var configScheduler: ConfigScheduler

    @Scheduled(initialDelay=5000, fixedDelay = 10000)
    fun update() {
        configScheduler.get(Element.backupLocation)?.let {
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