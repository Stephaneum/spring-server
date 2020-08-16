package de.stephaneum.spring.security

import de.stephaneum.spring.database.User
import de.stephaneum.spring.database.UserRepo
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtParser
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.security.Key
import java.util.*
import javax.annotation.PostConstruct
import javax.crypto.spec.SecretKeySpec

@Service
class JwtService {

    @Value("\${security.jwt.key}")
    lateinit var jwtKeyString: String

    lateinit var jwtKey: Key
    lateinit var jwtParser: JwtParser

    @Value("\${security.jwt.timeout}")
    private var validityInMilliseconds: Long = 0

    @Autowired
    private lateinit var userRepo: UserRepo

    @PostConstruct
    protected fun init() {
        val jwtKeyBytes = Base64.getDecoder().decode(jwtKeyString)
        jwtKey = SecretKeySpec(jwtKeyBytes, 0, jwtKeyBytes.size, "HmacSHA256")
        jwtParser = Jwts.parserBuilder().setSigningKey(jwtKey).build()
    }

    fun generateToken(map: Map<String, Any>): String {
        val now = Date()
        val validity = Date(now.time + validityInMilliseconds)
        val builder = Jwts.builder()
        map.forEach { (key, value) ->
            builder.claim(key, value)
        }
        return builder
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(jwtKey)
                .compact()
    }

    fun generateToken(userID: Int): String {
        val now = Date()
        val validity = Date(now.time + validityInMilliseconds)

        return Jwts.builder()
                .claim("userID", userID)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(jwtKey)
                .compact()
    }

    fun getUser(token: String): User? {
        try {
            val userID = jwtParser.parseClaimsJws(token).body["userID"] as Int
            return userRepo.findByIdOrNull(userID) ?: return null
        } catch (e: Exception) {
            return null
        }
    }

    fun getData(token: String): Claims? {
        try {
            return jwtParser.parseClaimsJws(token).body
        } catch (e: Exception) {
            return null
        }
    }

    fun generateKey(): String {
        val key = Keys.secretKeyFor(SignatureAlgorithm.HS256)
        return Base64.getEncoder().encodeToString(key.encoded)
    }
}