package de.stephaneum.backend.database

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import javax.persistence.*

enum class Type {
    PLAN, // show cover plan
    TEXT, // show text only
    IMG // show image only
}

@Entity
data class Blackboard(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                      var id: Int = 0,

                      @Column(nullable = false)
                      var type: Type = Type.TEXT,

                      @Column(nullable = false)
                      var value: String = "",

                      @Column(nullable = false)
                      var duration: Int = 5000,

                      @Column(nullable = false, name = "\"order\"")
                      var order: Int = 0,

                      @Column(nullable = false)
                      var activated: Boolean = true,

                      @Column(nullable = false) @JsonIgnore
                      var lastUpdate: Timestamp = Timestamp(System.currentTimeMillis()))

@Repository
interface BlackboardRepo : CrudRepository<Blackboard, Int> {

    fun findByOrderByOrder(): List<Blackboard>

    @Query("select max(b.order) from Blackboard b")
    fun findMaxOrder(): Int
}