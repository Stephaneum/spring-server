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
data class Post(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                var id: Int = 0,

                @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                var user: User? = null,

                @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                var menu: Menu? = null,

                @Column(nullable = false, length = 128)
                var title: String = "",

                @Column(nullable = true)
                var content: String? = null,

                @Column(nullable = false)
                var timestamp: Timestamp = Timestamp(0),

                @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                var userUpdate: User? = null,

                @Column(nullable = true, length = 32)
                var password: String? = null,

                @Column(nullable = false)
                var approved: Boolean = false,

                @Column(nullable = false)
                var preview: Int = 0,

                @Column(nullable = false)
                var layoutPost: Int = 0,

                @Column(nullable = false)
                var layoutPreview: Int = 0,

                @JsonInclude
                @Transient
                var images: List<File>? = null) {

    fun simplify() {
        user?.code?.code = ""
        user?.password = ""
        user?.storage = 0
        user?.email = ""

        userUpdate?.code?.code = ""
        userUpdate?.password = ""
        userUpdate?.storage = 0
        userUpdate?.email = ""
    }
}

@Repository
interface PostRepo: CrudRepository<Post, Int> {

    fun findByMenuIdOrderByTimestampDesc(menuID: Int): List<Post>
    fun findByMenuIdOrderByTimestampDesc(menuID: Int, pageable: Pageable): List<Post>
    fun findByUserAndApproved(user: User, approved: Boolean): List<Post>

    @Query("SELECT p FROM Post p WHERE p.approved = FALSE ORDER BY p.timestamp")
    fun findUnapproved(): List<Post>

    @Query("SELECT p FROM Post p WHERE p.approved = FALSE AND p.user.id = ?1 ORDER BY p.timestamp")
    fun findUnapproved(userID: Int): List<Post>

    fun countByApproved(approved: Boolean): Int
    fun countByApprovedAndUser(approved: Boolean, user: User): Int
    fun countByMenuAndApproved(menu: Menu, approved: Boolean): Int

    @Transactional
    fun deleteByUserAndApproved(user: User, approved: Boolean)
}