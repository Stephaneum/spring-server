package de.stephaneum.spring.database

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity
@Table(name="datei_beitrag")
data class FilePost(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                    var id: Int = 0,

                    @ManyToOne(optional = false) @OnDelete(action = OnDeleteAction.CASCADE)
                    @JoinColumn(name = "datei_id")
                    var file: File = File(),

                    @ManyToOne(optional = false) @OnDelete(action = OnDeleteAction.CASCADE)
                    @JoinColumn(name = "beitrag_id")
                    var post: Post = Post())

@Repository
interface FilePostRepo: CrudRepository<FilePost, Int>