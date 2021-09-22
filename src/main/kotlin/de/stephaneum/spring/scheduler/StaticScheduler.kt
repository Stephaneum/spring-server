package de.stephaneum.spring.scheduler

import de.stephaneum.spring.database.Static
import de.stephaneum.spring.database.StaticRepo
import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.helper.GlobalStateService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.File

@Service
class StaticScheduler {

    /**
     * this scheduler make sure that the static folder exists
     * this scheduler also updates the database according to the filesystem
     */

    private val logger = LoggerFactory.getLogger(StaticScheduler::class.java)

    @Autowired
    private lateinit var configScheduler: ConfigScheduler

    @Autowired
    private lateinit var globalStateService: GlobalStateService

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var staticRepo: StaticRepo

    @Scheduled(initialDelay=5000, fixedDelay = 30*1000) // check every half minute
    fun update() {

        if(globalStateService.noScheduler)
            return

        configScheduler.get(Element.fileLocation)?.let {
            val staticPath = "$it/${Static.FOLDER_NAME}/"

            // create missing folder
            val file = File(staticPath)
            if(!file.exists()) {
                logger.info("create missing static folder")
                file.mkdirs()
            }

            val files = fileService.listFilesRecursive(staticPath)
                    .filter { f -> f.absolutePath.endsWith(".html") || f.absolutePath.endsWith(".htm") }
                    .map { f -> f.absolutePath.replace("\\", "/").replace(staticPath, "") }

            val db = staticRepo.findAll().toList()

            // check for files not existing anymore
            val remove = db.filter { d -> files.none { f -> f == d.path } }

            // check for new files
            val new = files.filter { f -> db.none { d -> f == d.path } }

            if(remove.isNotEmpty()) {
                remove.forEach { f -> logger.info("remove: $f") }
                staticRepo.deleteAll(remove)
            }

            if(new.isNotEmpty()) {
                new.forEach { f -> logger.info("new: $f") }
                staticRepo.saveAll(new.map { n -> Static(path = n) })
            }
        }
    }
}