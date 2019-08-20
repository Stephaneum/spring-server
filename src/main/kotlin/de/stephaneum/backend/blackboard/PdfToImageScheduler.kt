package de.stephaneum.backend.blackboard

import de.stephaneum.backend.services.ImageService
import de.stephaneum.backend.database.BlackboardRepo
import de.stephaneum.backend.database.Type
import de.stephaneum.backend.database.now
import de.stephaneum.backend.scheduler.ConfigFetcher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import java.io.File
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.ImageType
import java.io.IOException
import org.apache.pdfbox.text.PDFTextStripper
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

    val logger = LoggerFactory.getLogger(PdfToImageScheduler::class.java)

    @Autowired
    private lateinit var configFetcher: ConfigFetcher

    @Autowired
    private lateinit var imageService: ImageService

    @Autowired
    private lateinit var blackboardRepo: BlackboardRepo

    // only this variable should be accessed from outside
    var instances = listOf<PdfImages>()

    @Scheduled(initialDelay=5000, fixedDelay = 10000)
    fun update() {

        val boards = blackboardRepo.findByOrderByOrder()
        val nextInstances = mutableListOf<PdfImages>()
        boards.forEach { board ->
            when(board.type) {
                Type.PLAN -> {
                    val pdfLocation = configFetcher.planLocation
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
                                val title = resolveDate(file)
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
        logger.info("")
        logger.info("--- updating images ---")
        val images = mutableListOf<ByteArray>()
        val document = PDDocument.load(file)
        val pdfRenderer = PDFRenderer(document)
        for (page in 0 until document.numberOfPages) {
            val image = pdfRenderer.renderImageWithDPI(page, 300f, ImageType.RGB)
            val trimmed = imageService.trimImage(image)
            val resized = imageService.reduceSize(trimmed, 1600, 9999)
            images.add(imageService.convertToJPG(resized))
        }
        document.close()
        logger.info("--- images generated ---")
        logger.info("")
        return images
    }

    private fun updateTimestamp(blackboardId: Int): Long {
        val now = now()
        blackboardRepo.findByIdOrNull(blackboardId)?.apply { lastUpdate = now }?.also { blackboardRepo.save(it) }
        return now.time
    }

    private val days = arrayOf("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag")
    private val months = arrayOf("Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember")

    private fun resolveDate(file: File): String? {
        java.util.logging.Logger.getLogger("org.apache.pdfbox").level = java.util.logging.Level.SEVERE

        var result: String? = null
        try {
            val document = PDDocument.load(file)
            val pdfStripper = PDFTextStripper()
            val text = pdfStripper.getText(document)

            val splitted = text.split(System.lineSeparator().toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            for (line in splitted) {
                var weekDay: String?
                var month: String?
                var day: String? = null
                var year: String? = null

                for (i in 0 until days.size) {

                    val occurenceAt = line.indexOf(days[i])
                    if (occurenceAt != -1) {
                        //Wochentag gefunden
                        logger.info("[Vertretungsplan-Analyse] " + days[i])
                        weekDay = days[i]

                        for (x in 0 until months.size) {

                            val occurenceMonatAt = line.indexOf(months[x])
                            if (occurenceMonatAt != -1) {

                                //Monat gefunden
                                logger.info("[Vertretungsplan-Analyse] " + months[x])

                                month = if (x >= 9) (x + 1).toString() else "0" + (x + 1)

                                val indexTagBegin = occurenceAt + days[i].length + 2
                                val indexTagEnd = occurenceMonatAt - 2
                                logger.info("[Vertretungsplan-Analyse] tag: $indexTagBegin bis $indexTagEnd")

                                if (indexTagEnd > indexTagBegin && indexTagEnd < line.length)
                                    day = line.substring(indexTagBegin, indexTagEnd)

                                if (day!!.length == 1)
                                    day = "0$day"

                                val indexJahrBegin = occurenceMonatAt + months[x].length + 1
                                val indexJahrEnd = occurenceMonatAt + months[x].length + 1 + 4
                                logger.info("[Vertretungsplan-Analyse] jahr: $indexJahrBegin bis $indexJahrEnd")

                                if (indexJahrEnd > indexJahrBegin && indexJahrEnd < line.length)
                                    year = line.substring(indexJahrBegin, indexJahrEnd)

                                if (year != null) {
                                    logger.info("\"$line\" >> $weekDay, $day.$month.$year")
                                    result = "$weekDay, $day.$month.$year"
                                }

                                break
                            }
                        }
                        break
                    }
                }

                if (result != null)
                    break
            }

            document.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return result
    }
}