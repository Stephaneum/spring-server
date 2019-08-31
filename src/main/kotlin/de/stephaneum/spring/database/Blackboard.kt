package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.io.File
import java.sql.Timestamp
import javax.persistence.*

enum class Type {
    PLAN, // show cover plan
    TEXT, // show text
    PDF, // show pdf
    IMG; // show image

    fun getString(): String {
        return when(this) {
            PLAN -> "Vertretungsplan"
            TEXT -> "Text"
            PDF -> "PDF"
            IMG -> "Bild"
        }
    }
}

@Entity
data class Blackboard(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                      var id: Int = 0,

                      @Column(nullable = false) @Enumerated(EnumType.STRING)
                      var type: Type = Type.TEXT,

                      @Column(nullable = false)
                      var value: String = "",

                      @Column(nullable = false)
                      var duration: Int = 0, // in seconds

                      @Column(nullable = false, name = "\"order\"")
                      var order: Int = 0,

                      @Column(nullable = false)
                      var visible: Boolean = true,

                      @Column(nullable = false)
                      var lastUpdate: Timestamp = now()) {

    @JsonIgnore
    fun getValueWithoutBreaks() = value.replace("<br>", " ")

    @JsonIgnore
    fun isUploaded() = File(value).isFile

    @JsonIgnore
    fun getFileName() = value.substring(value.lastIndexOf("/")+1)
}

@Repository
interface BlackboardRepo : CrudRepository<Blackboard, Int> {

    fun findByOrderByOrder(): List<Blackboard>

    @Query("select max(b.order) from Blackboard b")
    fun findMaxOrder(): Int
}