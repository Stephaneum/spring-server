package de.stephaneum.spring.security

import org.springframework.stereotype.Service
import java.security.MessageDigest

@Service
class CryptoService {

    private val PEPPER = "A43w8pa0M245qga4293zt9o4mc3z98TA3nQ9mzvTa943cta43mTaoz47tz3loIhbiKh"

    @ExperimentalUnsignedTypes
    fun checkPassword(password: String, hash: String): Boolean {
        val salt = password.substring(32)
        return password == hashMD5(password+salt+PEPPER)+salt
    }

    @ExperimentalUnsignedTypes
    fun hashMD5(string: String): String {
        return MessageDigest.getInstance("MD5").digest(string.toByteArray()).toHexString()
    }

    @ExperimentalUnsignedTypes
    private fun ByteArray.toHexString() = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }
}