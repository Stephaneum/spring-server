package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

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