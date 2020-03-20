package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonIgnore
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
                @JsonIgnore
                var path: String = "",

                @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                @JoinColumn(name = "projekt_id")
                var group: Group? = null,

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
                @JsonIgnore
                var teacherChat: Boolean = false,

                @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                @JoinColumn(name = "ordner_id")
                var folder: Folder? = null,

                @JsonInclude
                @Transient
                var fileName: String = "",

                @JsonInclude
                @Transient
                var isFolder: Boolean = false) {

    fun simplifyForPosts() {
        fileName = generateFileName()
        user = null
        folder = null
    }

    fun simplifyForCloud() {
        fileName = generateFileName()
        group = null
        schoolClass = null
        folder = null
        user?.email = ""
        user?.password = ""
    }

    fun generateFileName(): String {
        val fileNameWithID = path.substring(path.lastIndexOf('/') + 1)
        return fileNameWithID.substring(fileNameWithID.indexOf('_')+1)
    }
}

@Repository
interface FileRepo: CrudRepository<File, Int> {

    @Query("SELECT COALESCE(SUM(f.size),0) FROM File f WHERE f.user.id = ?1")
    fun calcStorageUsed(userID: Int): Int

    @Query("SELECT COALESCE(SUM(f.size),0) FROM File f WHERE f.user.id = ?1 AND f.group IS NULL AND f.schoolClass IS NULL AND f.teacherChat = FALSE")
    fun calcStorageUsedPrivate(userID: Int): Int

    @Query("SELECT COALESCE(SUM(f.size),0) FROM File f WHERE f.user.id = ?1 AND f.group IS NOT NULL AND f.schoolClass IS NULL AND f.teacherChat = FALSE")
    fun calcStorageUsedProject(userID: Int): Int

    @Query("SELECT COALESCE(SUM(f.size),0) FROM File f WHERE f.user.id = ?1 AND f.group IS NULL AND f.schoolClass IS NOT NULL AND f.teacherChat = FALSE")
    fun calcStorageUsedClass(userID: Int): Int

    @Query("SELECT COALESCE(SUM(f.size),0) FROM File f WHERE f.user.id = ?1 AND f.group IS NULL AND f.schoolClass IS NULL AND f.teacherChat = TRUE")
    fun calcStorageUsedTeacherChat(userID: Int): Int

    fun countByUserId(userID: Int): Int

    @Query("SELECT f FROM File f WHERE f.user.id = ?1 AND f.group IS NULL AND f.schoolClass IS NULL AND f.teacherChat = FALSE AND f.mime LIKE CONCAT(?2, '%') ORDER BY f.id DESC")
    fun findMyImages(userID: Int, mime: String): List<File>

    // map file ids to full file objects
    fun findByIdIn(ids: List<Int>): List<File>

    // root directory
    @Query("SELECT f FROM File f WHERE f.folder IS NULL AND f.user = ?1 AND f.group IS NULL AND f.schoolClass IS NULL AND f.teacherChat = FALSE ORDER BY f.id DESC")
    fun findPrivateInRoot(user: User): List<File>

    @Query("SELECT f FROM File f WHERE f.folder IS NULL AND f.group = ?1 AND f.schoolClass IS NULL AND f.teacherChat = FALSE ORDER BY f.id DESC")
    fun findGroupInRoot(group: Group?): List<File>

    @Query("SELECT f FROM File f WHERE f.folder IS NULL AND f.group IS NULL AND f.schoolClass = ?1 AND f.teacherChat = FALSE ORDER BY f.id DESC")
    fun findClassInRoot(schoolClass: SchoolClass): List<File>

    @Query("SELECT f FROM File f WHERE f.folder IS NULL AND f.group IS NULL AND f.schoolClass IS NULL AND f.teacherChat = TRUE ORDER BY f.id DESC")
    fun findTeacherInRoot(): List<File>

    fun findByFolder(folder: Folder): List<File>
    fun findByFolderOrderByIdDesc(folder: Folder): List<File>

    @Query("SELECT f FROM File f WHERE f.user IS NULL AND f.group IS NULL AND f.schoolClass IS NULL AND f.teacherChat = FALSE")
    fun findPotentialUnusedFiles(): List<File>
}