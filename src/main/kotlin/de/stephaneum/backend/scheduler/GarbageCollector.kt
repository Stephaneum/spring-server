package de.stephaneum.backend.scheduler

import de.stephaneum.backend.database.BlackboardRepo
import de.stephaneum.backend.database.Type
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.nio.file.Files

@Service
class GarbageCollector {

    private val logger = LoggerFactory.getLogger(GarbageCollector::class.java)

    @Autowired
    private lateinit var configFetcher: ConfigFetcher

    @Autowired
    private lateinit var blackboardRepo: BlackboardRepo

    @Scheduled(initialDelay=10000, fixedDelay = 10000)
    fun update() {

        val blackboardPath = configFetcher.location + "/blackboard"
        val boards = blackboardRepo.findAll()
        listFiles(blackboardPath).forEach { file ->
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

    }

    private fun listFiles(directoryName: String): List<File> {

        val directory = File(directoryName)
        val files = mutableListOf<File>()

        // Get all files from a directory.
        val fList = directory.listFiles()
        if (fList != null) {
            for (file in fList) {
                if (file.isFile) {
                    files.add(file)
                }
            }
        }
        return files
    }
}