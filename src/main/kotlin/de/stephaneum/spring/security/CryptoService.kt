package de.stephaneum.spring.security

import org.springframework.stereotype.Service
import java.security.MessageDigest

@Service
class CryptoService {

    @ExperimentalUnsignedTypes
    fun hashMD5(string: String): String {
        return MessageDigest.getInstance("MD5").digest(string.toByteArray()).toHexString()
    }

    @ExperimentalUnsignedTypes
    fun ByteArray.toHexString() = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }
}