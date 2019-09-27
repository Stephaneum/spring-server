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
@Table(name="datei")
data class File(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                var id: Int = 0,

                @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                @JoinColumn(name = "nutzer_id")
                var user: User = User(),

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
                var folder: Folder? = null)

@Repository
interface FileRepo: CrudRepository<File, Int>