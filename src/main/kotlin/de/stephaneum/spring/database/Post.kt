package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonInclude
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
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

                @Column(nullable = false, name = "text", columnDefinition = "TEXT CHARACTER SET utf8mb4")
                var content: String = "",

                @Column(nullable = false, name = "datum")
                var timestamp: Timestamp = Timestamp(0),

                @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                @JoinColumn(name = "nutzer_id_update")
                var userUpdate: User? = null,

                @Column(nullable = false, name = "passwort", length = 32)
                var password: String = "",

                @Column(nullable = false, name = "genehmigt")
                var approved: Boolean = false,

                @Column(nullable = false, name = "vorschau")
                var preview: Int = 0,

                @Column(nullable = false, name = "show_autor")
                var showAutor: Boolean = false,

                @Column(nullable = false, name = "layout_beitrag")
                var layoutPost: Int = 0,

                @Column(nullable = false, name = "layout_vorschau")
                var layoutPreview: Int = 0)

@Repository
interface PostRepo: CrudRepository<Post, Int>