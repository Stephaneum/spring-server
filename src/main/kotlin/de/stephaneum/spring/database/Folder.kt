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
data class Folder(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                  var id: Int = 0,

                  @Column(nullable = false)
                  var name: String = "",

                  @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                  var user: User? = null,

                  @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                  var group: Group? = null,

                  @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                  var parent: Folder? = null,

                  @Column(nullable = false)
                  var locked: Boolean = false,

                  @JsonInclude
                  @Transient
                  var isFile: Boolean = false,

                  @JsonInclude
                  @Transient
                  var size: Int = 0) {

    fun simplifyForCloud() {
        group = null
        parent = null
        user?.email = ""
        user?.password = ""
    }
}

@Repository
interface FolderRepo: CrudRepository<Folder, Int> {

    @Query("SELECT f FROM Folder f WHERE f.parent IS NULL AND f.user = ?1 AND f.name = ?2 AND f.group IS NULL")
    fun findPrivateFolderInRoot(user: User, folderName: String): List<Folder>

    @Query("SELECT f FROM Folder f WHERE f.parent IS NULL AND f.user = ?1 AND f.group IS NULL ORDER BY f.name")
    fun findPrivateFolderInRoot(user: User): List<Folder>

    @Query("SELECT f FROM Folder f WHERE f.parent IS NULL AND f.group = ?1 ORDER BY f.name")
    fun findGroupFolderInRoot(group: Group): List<Folder>

    @Query("SELECT f FROM Folder f WHERE f.parent IS NULL AND f.group = ?1 AND f.name = ?2")
    fun findGroupFolderInRoot(group: Group, folderName: String): List<Folder>

    fun findByParent(parent: Folder): List<Folder>
}