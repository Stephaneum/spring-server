package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonInclude
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import javax.persistence.*

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name="datei")
data class File(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                var id: Int = 0,

                @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                @JoinColumn(name = "nutzer_id")
                var user: User? = null,

                @Column(nullable = false, name="pfad", length = 1024)
                var path: String = "",

                @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                @JoinColumn(name = "projekt_id")
                var project: Project? = null,

                @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                @JoinColumn(name = "klasse_id")
                var schoolClass: SchoolClass? = null,

                @Column(nullable = false, name = "datum")
                var timestamp: Timestamp = Timestamp(0),

                @Column(nullable = false)
                var size: Int = 0,

                @Column(nullable = false, name="mime_type", length = 255)
                var mime: String = "",

                @Column(nullable = false)
                var public: Boolean = false,

                @Column(nullable = false, name = "lehrerchat")
                var teacherChat: Boolean = false,

                @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                @JoinColumn(name = "ordner_id")
                var folder: Folder? = null,

                @JsonInclude
                @Transient
                var fileName: String = "",

                @JsonInclude
                @Transient
                var fileNameWithID: String = "")

@Repository
interface FileRepo: CrudRepository<File, Int> {

    @Query("SELECT COALESCE(SUM(f.size),0) FROM File f WHERE f.user.id = ?1")
    fun calcStorageUsed(userID: Int): Int

    @Query("SELECT f FROM File f WHERE f.user.id = ?1 AND f.project IS NULL AND f.schoolClass IS NULL AND f.teacherChat = FALSE AND f.mime LIKE CONCAT(?2, '%') ORDER BY f.id DESC")
    fun findMyImages(userID: Int, mime: String): List<File>
}