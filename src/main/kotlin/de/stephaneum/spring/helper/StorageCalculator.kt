package de.stephaneum.spring.helper

import org.springframework.stereotype.Service
import java.text.DecimalFormat

enum class SizeType(val bytes: Long) {
    KB(1024L),
    MB(1024L * 1024L),
    GB(1024L * 1024L * 1024L)
}

@Service
class StorageCalculator {

    private val formatter = DecimalFormat("#.#")

    /**
     * @param bytes the amount of bytes
     * @return human readable string
     */
    fun convertSizeToString(bytes: Long): String {
        val s = when {
            bytes < 1024                -> "$bytes Bytes"
            bytes < 1024 * 1024         -> formatter.format(bytes.toDouble() / 1024) + " KB"
            bytes < 1024 * 1024 * 1024  -> formatter.format(bytes.toDouble() / (1024 * 1024)) + " MB"
            else                        -> formatter.format(bytes.toDouble() / (1024 * 1024 * 1024)) + " GB"
        }
        return s.replace('.', ',')
    }

    fun convertToBytes(count: Long, size: SizeType): Long {
        return count * size.bytes
    }
}