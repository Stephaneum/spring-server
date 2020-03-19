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
    CREATE_GROUP(5, "Gruppe erstellt", "group-badge"),
    JOIN_GROUP(6, "Gruppe beigetreten", "group-badge"),
    QUIT_GROUP(7, "Gruppe verlassen", "group-badge"),
    DELETE_GROUP(8, "Gruppe gelöscht", "group-badge"),
    JOIN_CLASS(9, "Klasse festgelegt", "other-badge"),
    QUIT_CLASS(10, "Klasse verlassen", "other-badge"),
    DELETE_FILE(11, "Datei gelöscht", "file-badge"),
    APPROVE_POST(12, "Beitrag genehmigt", "post-badge"),
    CREATE_CHAT_ROOM(13, "Chatraum erstellt", "other-badge"),
    DELETE_POST(14,"Beitrag gelöscht", "post-badge"),
    CREATE_MENU(15, "Menü erstellt", "menu-badge"),
    EDIT_MENU(16, "Menü bearbeitet", "menu-badge"),
    DELETE_MENU(17, "Menü gelöscht", "menu-badge"),
    APPROVE_GROUP(18, "Gruppe genehmigt", "group-badge"),
    REJECT_GROUP(19, "Gruppe abgelehnt", "group-badge");

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