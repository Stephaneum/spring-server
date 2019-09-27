package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

const val ROLE_UNDEFINED = -2
const val ROLE_NO_LOGIN = -1
const val ROLE_STUDENT = 0
const val ROLE_TEACHER = 1
const val ROLE_GUEST = 2
const val ROLE_ADMIN = 100

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name="zugangscode")
data class Code(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                var id: Int = 0,

                @Column(nullable = false, length=32)
                var code: String = "",

                @Column(nullable = false, name="rang")
                var role: Int = 0,

                @Column(nullable = false, name="benutzt")
                var used: Boolean = false)

@Repository
interface CodeRepo: CrudRepository<Code, Int>