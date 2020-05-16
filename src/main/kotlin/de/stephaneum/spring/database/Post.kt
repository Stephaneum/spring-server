package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonInclude
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import javax.persistence.*

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name="beitrag")
data class Post(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                var id: Int = 0,

                @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                @JoinColumn(name = "nutzer_id")
                var user: User? = null,

                @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                @JoinColumn(name = "grp_id")
                var menu: Menu? = null,

                @Column(nullable = false, name = "titel", length = 128)
                var title: String = "",

                @Column(nullable = true, name = "text", columnDefinition = "TEXT CHARACTER SET utf8mb4")
                var content: String? = null,

                @Column(nullable = false, name = "datum")
                var timestamp: Timestamp = Timestamp(0),

                @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                @JoinColumn(name = "nutzer_id_update")
                var userUpdate: User? = null,

                @Column(nullable = true, name = "passwort", length = 32)
                var password: String? = null,

                @Column(nullable = false, name = "genehmigt")
                var approved: Boolean = false,

                @Column(nullable = false, name = "vorschau")
                var preview: Int = 0,

                @Column(nullable = false, name = "show_autor")
                var showAutor: Boolean = false,

                @Column(nullable = false, name = "layout_beitrag")
                var layoutPost: Int = 0,

                @Column(nullable = false, name = "layout_vorschau")
                var layoutPreview: Int = 0,

                @JsonInclude
                @Transient
                var images: List<File>? = null) {

    fun simplify() {
        user?.code?.code = ""
        user?.password = ""
        user?.storage = 0
        user?.email = ""
        user?.gender = null

        userUpdate?.code?.code = ""
        userUpdate?.password = ""
        userUpdate?.storage = 0
        userUpdate?.email = ""
        userUpdate?.gender = null
    }
}

@Repository
interface PostRepo: CrudRepository<Post, Int> {

    fun countByApproved(approved: Boolean): Int
    fun countByApprovedAndUser(approved: Boolean, user: User): Int

    fun findByMenuIdOrderByTimestampDesc(menuID: Int): List<Post>
    fun findByMenuIdOrderByTimestampDesc(menuID: Int, pageable: Pageable): List<Post>
    fun findByUserAndApproved(user: User, approved: Boolean): List<Post>

    @Query("SELECT p FROM Post p WHERE p.approved = FALSE ORDER BY p.timestamp")
    fun findUnapproved(): List<Post>

    @Query("SELECT p FROM Post p WHERE p.approved = FALSE AND p.user.id = ?1 ORDER BY p.timestamp")
    fun findUnapproved(userID: Int): List<Post>

    @Transactional
    fun deleteByUserAndApproved(user: User, approved: Boolean)
}