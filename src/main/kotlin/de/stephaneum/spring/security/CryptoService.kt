package de.stephaneum.spring.security

import org.springframework.stereotype.Service
import java.security.MessageDigest

@Service
class CryptoService {

    private val PEPPER = "A43w8pa0M245qga4293zt9o4mc3z98TA3nQ9mzvTa943cta43mTaoz47tz3loIhbiKh"
    private val SALT_POOL = listOf('a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
        'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
        '0','1','2','3','4','5','6','7','8','9')

    @ExperimentalStdlibApi
    fun getRandomSalt(length: Int): String {
        return (CharArray(length) { SALT_POOL.random() }).concatToString()
    }

    @ExperimentalUnsignedTypes
    fun checkPassword(password: String, hash: String): Boolean {
        val salt = hash.substring(32)
        return hashMD5(password+salt+PEPPER)+salt == hash
    }

    @ExperimentalStdlibApi
    @ExperimentalUnsignedTypes
    fun hashPassword(password: String): String {
        val salt = getRandomSalt(223)
        return hashMD5(password+salt+PEPPER)+salt
    }

    @ExperimentalUnsignedTypes
    fun hashMD5(string: String): String {
        return MessageDigest.getInstance("MD5").digest(string.toByteArray()).toHexString()
    }

    @ExperimentalUnsignedTypes
    private fun ByteArray.toHexString() = asUByteArray().joinToString("") { it.toString(16).padStart(2, '0') }
}