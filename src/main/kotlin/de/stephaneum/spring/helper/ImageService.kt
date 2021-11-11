package de.stephaneum.spring.helper

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.exif.ExifIFD0Directory
import de.stephaneum.spring.database.File
import de.stephaneum.spring.database.FileRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.awt.Color
import java.awt.Image
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin


@Service
class ImageService {

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var fileRepo: FileRepo

    /**
     * compress the image and apply the changes to the hard drive and also to the database
     * @param file image which should be compressed.
     */
    fun reduceSizeOfFile(file: File) {
        val image = ImageIO.read(java.io.File(file.path))
        val imageCompressed = reduceSizeBuffered(image, 1000, 1000)

        // update in hard drive
        Files.delete(Paths.get(file.path))
        val newPath = fileService.getPathWithNewExtension(file.path, "jpg")
        ImageIO.write(imageCompressed, "jpg", java.io.File(newPath))

        // update path and size in database
        file.path = newPath
        file.size = java.io.File(newPath).length().toInt()
        file.mime = Files.probeContentType(Paths.get(newPath))
        fileRepo.save(file)
    }

    /**
     * returns a byte array with a proper rotation based on meta data of the image
     * alpha channel will get lost if rotation is applied using (JPEG)
     * @return byte array containing the new image if rotation is applied, null otherwise
     */
    fun digestImageRotation(bytes: ByteArray): ByteArray? {
        val metadata = ImageMetadataReader.readMetadata(bytes.inputStream())
        val exifIFD0 = metadata.getFirstDirectoryOfType(ExifIFD0Directory::class.java) ?: return null
        if(!exifIFD0.containsTag(ExifIFD0Directory.TAG_ORIENTATION))
            return null
        val orientation =  exifIFD0.getInt(ExifIFD0Directory.TAG_ORIENTATION)

        val rotation = when (orientation) {
            1 -> null
            6 -> 90.0 // CW 90
            3 -> 180.0 // CW 180
            8 -> 270.0 // CW 270
            else -> null // should not happen
        }

        if(rotation != null) {
            val image = ImageIO.read(bytes.inputStream())
            val rotated = rotate(image, rotation)
            val outputStream = ByteArrayOutputStream()
            ImageIO.write(rotated, "jpg", outputStream)
            outputStream.flush()
            val result = outputStream.toByteArray()
            outputStream.close()
            return result
        } else {
            // no rotation
            return null
        }
    }

    /**
     * @param img image which should be compressed
     * @param maxWidth max width of output image
     * @param maxHeight max height of output image
     * @return resized BufferedImage
     */
    fun reduceSizeBuffered(img: BufferedImage, maxWidth: Int, maxHeight: Int): BufferedImage {
        var scaledWidth = img.getWidth(null)
        var scaledHeight = img.getHeight(null)

        // adjust width
        if (scaledWidth > maxWidth) {

            scaledWidth = maxWidth
            scaledHeight = scaledWidth * img.getHeight(null) / img.getWidth(null)
        }

        // adjust height
        if (scaledHeight > maxHeight) {

            scaledHeight = maxHeight
            scaledWidth = scaledHeight * img.getWidth(null) / img.getHeight(null)
        }

        val tmp = img.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH)
        val resized = BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB)
        val g2d = resized.createGraphics()
        g2d.drawImage(tmp, 0, 0, null)
        g2d.dispose()
        return resized
    }

    /**
     * @param bytes which should be converted
     * @return BufferedImage from Byte Array
     */
    fun convertToBufferedImage(bytes: ByteArray): BufferedImage {
        return ImageIO.read(ByteArrayInputStream(bytes))
    }

    /**
     * @param img image which should be converted
     * @return jpg file as byte[]
     */
    fun convertToJPG(img: BufferedImage): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        ImageIO.write(img, "jpg", byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    /**
     * @param img image which should be trimmed
     * @return trimmed image
     */
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

    fun rotate(img: BufferedImage, angle: Double): BufferedImage {
        val rads = Math.toRadians(angle)
        val sin = abs(sin(rads))
        val cos = abs(cos(rads))
        val w = img.width
        val h = img.height
        val newWidth = floor(w * cos + h * sin).toInt()
        val newHeight = floor(h * cos + w * sin).toInt()
        val rotated = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB)
        val g2d = rotated.createGraphics()
        val at = AffineTransform()
        at.translate((newWidth - w) / 2.toDouble(), (newHeight - h) / 2.toDouble())
        val x = w / 2
        val y = h / 2
        at.rotate(rads, x.toDouble(), y.toDouble())
        g2d.transform = at
        g2d.drawImage(img, 0, 0, null)
        g2d.dispose()
        return rotated
    }
}
