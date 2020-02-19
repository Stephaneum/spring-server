package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonInclude
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import javax.persistence.*

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name="datei_beitrag")
data class FilePost(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                    var id: Int = 0,

                    @ManyToOne(optional = false) @OnDelete(action = OnDeleteAction.CASCADE)
                    @JoinColumn(name = "datei_id")
                    var file: File = File(),

                    @ManyToOne(optional = false) @OnDelete(action = OnDeleteAction.CASCADE)
                    @JoinColumn(name = "beitrag_id")
                    var post: Post = Post())

// simple version, post is simplified to just the id
data class FilePostSimple(var id: Int, var file: File, var postID: Int)

@Repository
interface FilePostRepo: CrudRepository<FilePost, Int> {

    fun findByPostId(postID: Int): List<FilePost>

    fun countByFile(file: File): Int

    @Query("SELECT NEW de.stephaneum.spring.database.FilePostSimple(fp.id, fp.file, fp.post.id) FROM FilePost fp WHERE fp.file.id IN ?1")
    fun findImagesByPostIdIn(postID: List<Int>): List<FilePostSimple>

    @Transactional
    fun deleteByPostId(postID: Int)
}