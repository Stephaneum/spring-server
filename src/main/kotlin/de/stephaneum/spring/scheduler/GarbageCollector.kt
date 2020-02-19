package de.stephaneum.spring.scheduler

import de.stephaneum.spring.database.BlackboardRepo
import de.stephaneum.spring.database.FileRepo
import de.stephaneum.spring.database.Type
import de.stephaneum.spring.helper.FileService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.Files

@Service
class GarbageCollector {

    /**
     * global garbage collector
     */

    private val logger = LoggerFactory.getLogger(GarbageCollector::class.java)

    @Autowired
    private lateinit var configScheduler: ConfigScheduler

    @Autowired
    private lateinit var blackboardRepo: BlackboardRepo

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var fileRepo: FileRepo

    @Scheduled(initialDelay=10000, fixedDelay = 1000*60)
    fun update() {

        val blackboardPath = configScheduler.get(Element.fileLocation) + "/blackboard"
        val boards = blackboardRepo.findAll()
        fileService.listFiles(blackboardPath).forEach { file ->
            val notInDatabase = boards.none { board -> (board.type == Type.IMG || board.type == Type.PDF) && board.value == file.absolutePath.replace("\\", "/") }
            if(notInDatabase) {
                try {
                    Files.delete(file.toPath())
                    logger.info("File deleted: '${file.absolutePath}'")
                } catch (e: IOException) {
                    logger.error("File deletion error", e)
                }
            }
        }

        val files = fileRepo.findPotentialUnusedFiles()
        for(f in files) {
            val hasConnections = fileService.hasConnections(f)

            if(!hasConnections) {
                logger.info("Delete file due to no connections: '${f.generateFileName()}'")
                fileService.deleteFileStephaneumFinal(f)
            }
        }
    }
}