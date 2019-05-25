package de.stephaneum.backend.scheduler

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File
import java.awt.image.BufferedImage
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.ImageType
import javax.imageio.ImageIO
import java.io.ByteArrayOutputStream
import java.awt.Color
import java.io.IOException
import org.apache.pdfbox.text.PDFTextStripper

@Component
class ImageGenerator {

    val logger = LoggerFactory.getLogger(ImageGenerator::class.java)

    @Autowired
    private lateinit var configFetcher: ConfigFetcher

    private var lastModified: Long = 0

    var images: List<ByteArray> = emptyList()
    var date: String? = null

    @Scheduled(initialDelay=5000, fixedDelay = 10000)
    fun update() {

        if(configFetcher.pdfLocation == null)
            return

        val file = File(configFetcher.pdfLocation)

        if(file.isFile && lastModified != file.lastModified()) {
            logger.info("")
            logger.info("--- updating images ---")

            val nextImages = mutableListOf<ByteArray>()
            val document = PDDocument.load(file)
            val pdfRenderer = PDFRenderer(document)
            for (page in 0 until document.numberOfPages) {
                val bim = pdfRenderer.renderImageWithDPI(page, 300f, ImageType.RGB)
                val trimmed = trimImage(bim)
                nextImages.add(convertToJPG(trimmed))
            }
            document.close()

            date = resolveDate(file)
            lastModified = file.lastModified()
            images = nextImages
            logger.info("--- images generated ---")
            logger.info("")
        }
    }

    fun trimImage(img: BufferedImage): BufferedImage {
        val x = getWhiteLeft(img)
        val y = getWhiteTop(img)
        val width = getWhiteRight(img) - x
        val height = getWhiteBottom(img) - y

        return img.getSubimage(x, y, width, height)
    }

    private fun getWhiteLeft(img: BufferedImage): Int {
        val height = img.height
        val width = img.width
        var left = img.width

        for(y in 0 until height) {
            for(x in 0 until width) {
                if(img.getRGB(x, y) != Color.WHITE.rgb && x < left) {
                    left = x
                    break
                }
            }
        }
        return left
    }

    private fun getWhiteRight(img: BufferedImage): Int {
        val height = img.height
        val width = img.width
        var right = 0

        for (y in 0 until height) {
            for (x in width - 1 downTo 0) {
                if (img.getRGB(x, y) != Color.WHITE.rgb && x > right) {
                    right = x
                    break
                }
            }
        }

        return right
    }

    private fun getWhiteBottom(img: BufferedImage): Int {
        val width = img.width
        val height = img.height
        var bottom = 0

        for (x in 0 until width) {
            for (y in height - 1 downTo 0) {
                if (img.getRGB(x, y) != Color.WHITE.rgb && y > bottom) {
                    bottom = y
                    break
                }
            }
        }

        return bottom
    }

    private fun getWhiteTop(img: BufferedImage): Int {
        val width = img.width
        val height = img.height
        var top = img.height

        for (x in 0 until width) {
            for (y in 0 until height) {
                if (img.getRGB(x, y) != Color.WHITE.rgb && y < top) {
                    top = y
                    break
                }
            }
        }

        return top
    }

    fun convertToJPG(img: BufferedImage): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        ImageIO.write(img, "jpg", byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    private val days = arrayOf("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag")
    private val months = arrayOf("Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember")

    fun resolveDate(file: File): String? {
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