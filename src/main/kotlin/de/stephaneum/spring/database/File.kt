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
data class File(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                var id: Int = 0,

                @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                var user: User? = null,

                @Column(nullable = false, length = 1024)
                @JsonIgnore
                var path: String = "",

                @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                var group: Group? = null,

                @Column(nullable = false)
                var timestamp: Timestamp = Timestamp(0),

                @Column(nullable = false)
                var size: Int = 0,

                @Column(nullable = false, length = 255)
                var mime: String = "",

                @Column(nullable = false)
                var public: Boolean = false,

                @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                var folder: Folder? = null,

                @JsonInclude
                @Transient
                var fileName: String = "",

                @JsonInclude
                @Transient
                var isFile: Boolean = true) {

    fun simplifyForPosts() {
        fileName = generateFileName()
        user = null
        folder = null
    }

    fun simplifyForCloud() {
        fileName = generateFileName()
        group = null
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

    fun countByUserId(userID: Int): Int

    // map file ids to full file objects
    fun findByIdIn(ids: List<Int>): List<File>

    fun findByGroup(group: Group): List<File>
    fun findByUser(user: User): List<File>
    fun findByUserAndGroup(user: User, group: Group): List<File>

    @Query("SELECT COALESCE(SUM(f.size),0) FROM File f WHERE f.user.id = ?1")
    fun calcStorageUsed(userID: Int): Int

    @Query("SELECT COALESCE(SUM(f.size),0) FROM File f WHERE f.user.id = ?1 AND f.group IS NULL")
    fun calcStorageUsedPrivate(userID: Int): Int

    @Query("SELECT COALESCE(SUM(f.size),0) FROM File f WHERE f.user.id = ?1 AND f.group IS NOT NULL")
    fun calcStorageUsedProject(userID: Int): Int

    @Query("SELECT f FROM File f WHERE f.user.id = ?1 AND f.group IS NULL AND f.mime LIKE CONCAT(?2, '%') ORDER BY f.id DESC")
    fun findMyImages(userID: Int, mime: String): List<File>

    // root directory
    @Query("SELECT f FROM File f WHERE f.folder IS NULL AND f.user = ?1 AND f.group IS NULL ORDER BY f.id DESC")
    fun findPrivateInRoot(user: User): List<File>

    @Query("SELECT f FROM File f WHERE f.folder IS NULL AND f.group = ?1 ORDER BY f.id DESC")
    fun findGroupInRoot(group: Group?): List<File>

    fun findByFolder(folder: Folder): List<File>
    fun findByFolderOrderByIdDesc(folder: Folder): List<File>

    @Query("SELECT f FROM File f WHERE f.user IS NULL AND f.group IS NULL")
    fun findPotentialUnusedFiles(): List<File>
}