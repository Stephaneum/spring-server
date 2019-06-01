package de.stephaneum.backend

import org.springframework.stereotype.Service
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@Service
class ImageService {

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
}
