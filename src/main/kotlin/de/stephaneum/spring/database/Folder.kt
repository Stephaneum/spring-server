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

                  @JsonInclude
                  @Transient
                  var isFolder: Boolean = true,

                  @JsonInclude
                  @Transient
                  var size: Int = 0) {

    fun simplifyForCloud() {
        user = null
    }
}

@Repository
interface FolderRepo: CrudRepository<Folder, Int> {

    @Query("SELECT f FROM Folder f WHERE f.parent IS NULL AND f.user = ?1 AND f.name = ?2 AND f.group IS NULL AND f.schoolClass IS NULL AND f.teacherChat = FALSE")
    fun findPrivateFolderInRoot(user: User, folderName: String): List<Folder>

    @Query("SELECT f FROM Folder f WHERE f.parent IS NULL AND f.user = ?1 AND (f.group = ?2 OR ?2 IS NULL) AND (f.schoolClass = ?3 OR ?3 IS NULL) AND f.teacherChat = ?4 ORDER BY f.name")
    fun findFolderInRoot(user: User, group: Group?, schoolClass: SchoolClass?, teacherChat: Boolean): List<Folder>

    fun findByParent(parent: Folder): List<Folder>
}