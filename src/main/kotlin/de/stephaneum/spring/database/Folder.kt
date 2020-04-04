package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonInclude
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name="ordner")
data class Folder(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                  var id: Int = 0,

                  @Column(nullable = false)
                  var name: String = "",

                  @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                  @JoinColumn(name = "eigentum")
                  var user: User? = null,

                  @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                  @JoinColumn(name = "projekt_id")
                  var group: Group? = null,

                  @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                  @JoinColumn(name = "klasse_id")
                  var schoolClass: SchoolClass? = null,

                  @Column(nullable = false, name="lehrerchat")
                  var teacherChat: Boolean = false,

                  @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                  @JoinColumn(name = "parent")
                  var parent: Folder? = null,

                  @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
                  var locked: Boolean = false,

                  @JsonInclude
                  @Transient
                  var isFolder: Boolean = true,

                  @JsonInclude
                  @Transient
                  var size: Int = 0) {

    fun simplifyForCloud() {
        group = null
        schoolClass = null
        parent = null
        user?.email = ""
        user?.password = ""
    }
}

@Repository
interface FolderRepo: CrudRepository<Folder, Int> {

    @Query("SELECT f FROM Folder f WHERE f.parent IS NULL AND f.user = ?1 AND f.name = ?2 AND f.group IS NULL AND f.schoolClass IS NULL AND f.teacherChat = FALSE")
    fun findPrivateFolderInRoot(user: User, folderName: String): List<Folder>

    @Query("SELECT f FROM Folder f WHERE f.parent IS NULL AND f.user = ?1 AND f.group IS NULL AND f.schoolClass IS NULL AND f.teacherChat = FALSE ORDER BY f.name")
    fun findPrivateFolderInRoot(user: User): List<Folder>

    @Query("SELECT f FROM Folder f WHERE f.parent IS NULL AND f.group = ?1 AND f.schoolClass IS NULL AND f.teacherChat = FALSE ORDER BY f.name")
    fun findGroupFolderInRoot(group: Group): List<Folder>

    @Query("SELECT f FROM Folder f WHERE f.parent IS NULL AND f.group IS NULL AND f.schoolClass = ?1 AND f.teacherChat = FALSE ORDER BY f.name")
    fun findClassFolderInRoot(schoolClass: SchoolClass): List<Folder>

    @Query("SELECT f FROM Folder f WHERE f.parent IS NULL AND f.group IS NULL AND f.schoolClass IS NULL AND f.teacherChat = TRUE ORDER BY f.name")
    fun findTeacherFolderInRoot(): List<Folder>

    fun findByParent(parent: Folder): List<Folder>
}