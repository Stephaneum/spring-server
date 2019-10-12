package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import javax.persistence.*


enum class EventType(val id: Int, val description: String, val className: String) {
    LOGIN(0, "Login", "auth-badge"),
    REGISTER(1, "Registrierung", "auth-badge"),
    UPLOAD(2, "Datei hochgeladen", "file-badge"),
    CREATE_POST(3, "Beitrag erstellt", "post-badge"),
    EDIT_POST(4, "Beitrag bearbeitet", "post-badge"),
    CREATE_PROJECT(5, "Projekt erstellt", "project-badge"),
    JOIN_PROJECT(6, "Projekt beigetreten", "project-badge"),
    QUIT_PROJECT(7, "Projekt verlassen", "project-badge"),
    DELETE_PROJECT(8, "Projekt gelöscht", "project-badge"),
    JOIN_CLASS(9, "Klasse festgelegt", "other-badge"),
    QUIT_CLASS(10, "Klasse verlassen", "other-badge"),
    DELETE_FILE(11, "Datei gelöscht", "file-badge"),
    APPROVE_POST(12, "Beitrag genehmigt", "post-badge"),
    CREATE_CHAT_ROOM(13, "Chatraum erstellt", "other-badge");

    companion object {
        fun valueOf(id: Int): EventType {
            return values().first { it.id == id }
        }
    }
}

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name="log")
data class Log(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
               var id: Int = 0,

               @Column(nullable = false, name = "datum")
               var timestamp: Timestamp = Timestamp(0),

               @Column(nullable = false, name="typ")
               var type: Int = 0,

               @Column(nullable = false, name="ereignis")
               var info: String = "")

@Repository
interface LogRepo: CrudRepository<Log, Int> {

    fun findByOrderByTimestampDesc(): List<Log>
}