package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import javax.persistence.*

enum class EventType(val id: Int, val description: String) {
    LOGIN(0, "Login"),
    REGISTER(1, "Registrierung"),
    UPLOAD(2, "Datei hochgeladen"),
    CREATE_POST(3, "Beitrag erstellt"),
    EDIT_POST(4, "Beitrag bearbeitet"),
    CREATE_PROJECT(5, "Projekt erstellt"),
    JOIN_PROJECT(6, "Projekt beigetreten"),
    QUIT_PROJECT(7, "Projekt verlassen"),
    DELETE_PROJECT(8, "Projekt gelöscht"),
    JOIN_CLASS(9, "Klasse festgelegt"),
    QUIT_CLASS(10, "Klasse verlassen"),
    DELETE_FILE(11, "Datei gelöscht"),
    APPROVE_POST(12, "Beitrag genehmigt"),
    CREATE_CHAT_ROOM(13, "Chatraum erstellt")
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
interface LogRepo: CrudRepository<Log, Int>