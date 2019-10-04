package de.stephaneum.spring.security

import de.stephaneum.spring.database.User
import de.stephaneum.spring.database.UserRepo
import io.jsonwebtoken.Claims
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

    lateinit var jwtKey: Key // will be set in post construct

    @Value("\${security.jwt.timeout}")
    private var validityInMilliseconds: Long = 0

    @Autowired
    private lateinit var userRepo: UserRepo

    @PostConstruct
    protected fun init() {
        val jwtKeyBytes = Base64.getDecoder().decode(jwtKeyString)
        jwtKey = SecretKeySpec(jwtKeyBytes, 0, jwtKeyBytes.size, "HmacSHA256")
    }

    fun generateToken(map: Map<String, String>): String {
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
            if(isValid(token)) {
                val userID = Jwts.parser().setSigningKey(jwtKey).parseClaimsJws(token).body["userID"] as Int
                return userRepo.findByIdOrNull(userID) ?: return null
            } else {
                return null
            }
        } catch (e: Exception) {
            return null
        }
    }

    fun getData(token: String): Claims? {
        try {
            if(isValid(token))
                return Jwts.parser().setSigningKey(jwtKey).parseClaimsJws(token).body
            else
                return null
        } catch (e: Exception) {
            return null
        }
    }

    fun isValid(token: String): Boolean {
        try {
            val claims = Jwts.parser().setSigningKey(jwtKey).parseClaimsJws(token)
            return !claims.body.expiration.before(Date())
        } catch (e: Exception) {
            return false
        }
    }

    fun generateKey(): String {
        val key = Keys.secretKeyFor(SignatureAlgorithm.HS256)
        return Base64.getEncoder().encodeToString(key.encoded)
    }
}