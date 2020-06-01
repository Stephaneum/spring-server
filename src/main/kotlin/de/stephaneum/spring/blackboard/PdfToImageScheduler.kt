package de.stephaneum.spring.blackboard

import de.stephaneum.spring.helper.ImageService
import de.stephaneum.spring.database.BlackboardRepo
import de.stephaneum.spring.database.Type
import de.stephaneum.spring.database.now
import de.stephaneum.spring.helper.GlobalStateService
import de.stephaneum.spring.helper.PlanService
import de.stephaneum.spring.scheduler.Element
import de.stephaneum.spring.scheduler.ConfigScheduler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import java.io.File
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.ImageType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

/**
 * this file handles the generated images from the pdf files
 * when the file on hard drive or the instance in the database change, a new image will be generated
 */
data class PdfImages(val boardId: Int,
                     val images: List<ByteArray> = emptyList(),
                     val lastModified: Long = 0,
                     val title: String? = null)

@Service
class PdfToImageScheduler {

    private val logger = LoggerFactory.getLogger(PdfToImageScheduler::class.java)

    @Autowired
    private lateinit var configScheduler: ConfigScheduler

    @Autowired
    private lateinit var globalStateService: GlobalStateService

    @Autowired
    private lateinit var imageService: ImageService

    @Autowired
    private lateinit var planService: PlanService

    @Autowired
    private lateinit var blackboardRepo: BlackboardRepo

    // only this variable should be accessed from outside
    var instances = listOf<PdfImages>()

    @Scheduled(initialDelay=5000, fixedDelay = 10000)
    fun update() {

        if(globalStateService.noScheduler)
            return

        val boards = blackboardRepo.findByOrderByOrder()
        val nextInstances = mutableListOf<PdfImages>()
        boards.forEach { board ->
            when(board.type) {
                Type.PLAN -> {
                    val pdfLocation = configScheduler.get(Element.planLocation)
                    if(pdfLocation != null) {
                        val file = File(pdfLocation) // the pdf file
                        var instance = instances.find { it.boardId == board.id } ?: PdfImages(board.id)

                        if(file.isFile) {
                            var changed = false
                            if(instance.lastModified < board.lastUpdate.time) {
                                changed = true
                                logger.info("updating (instance in database is newer)")
                            }

                            if(instance.lastModified < file.lastModified()) {
                                changed = true
                                logger.info("updating (instance on harddrive is newer)")
                            }
                            if(changed) {
                                val images = generateImages(file)
                                val title = planService.resolveDate(file)
                                val lastModified = updateTimestamp(board.id) // also update database
                                instance = PdfImages(board.id, images, lastModified, title)
                            }
                            nextInstances.add(instance)
                        }

                    }
                }
                Type.PDF -> {
                    val pdfLocation = board.value
                    val file = File(pdfLocation) // the pdf file
                    var instance = instances.find { it.boardId == board.id } ?: PdfImages(board.id)

                    if(file.isFile) {
                        var changed = false
                        if(instance.lastModified < board.lastUpdate.time) {
                            changed = true
                            logger.info("updating (instance in database is newer)")
                        }

                        if(instance.lastModified < file.lastModified()) {
                            changed = true
                            logger.info("updating (instance on harddrive is newer)")
                        }
                        if(changed) {
                            val images = generateImages(file)
                            val lastModified = updateTimestamp(board.id) // also update database
                            instance = PdfImages(board.id, images, lastModified)
                        }
                        nextInstances.add(instance)
                    }
                }
                else -> {}
            }
        }
        instances = nextInstances
    }

    private fun generateImages(file: File): List<ByteArray> {
        val images = mutableListOf<ByteArray>()
        val document = PDDocument.load(file)
        val pdfRenderer = PDFRenderer(document)
        for (page in 0 until document.numberOfPages) {
            val image = pdfRenderer.renderImageWithDPI(page, 300f, ImageType.RGB)
            val trimmed = imageService.trimImage(image)
            val resized = imageService.reduceSizeBuffered(trimmed, 1600, 9999)
            images.add(imageService.convertToJPG(resized))
        }
        document.close()
        logger.info("images generated")
        return images
    }

    private fun updateTimestamp(blackboardId: Int): Long {
        val now = now()
        blackboardRepo.findByIdOrNull(blackboardId)?.apply { lastUpdate = now }?.also { blackboardRepo.save(it) }
        return now.time
    }
}