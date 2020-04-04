package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonInclude
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

enum class AreaType {
    TEXT, IMAGE, PDF
}

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
data class BoardArea(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                     var id: Int = 0,

                     @ManyToOne(optional = false) @OnDelete(action = OnDeleteAction.CASCADE)
                     var board: GroupBoard = GroupBoard(),

                     @Column(nullable = false)
                     var x: Int = 0,

                     @Column(nullable = false)
                     var y: Int = 0,

                     @Column(nullable = false)
                     var width: Int = 0,

                     @Column(nullable = false)
                     var height: Int = 0,

                     @Column(nullable = true)
                     var text: String? = null,

                     @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                     var file: File? = null,

                     @Column(nullable = false) @Enumerated(EnumType.STRING)
                     var type: AreaType = AreaType.TEXT)

@Repository
interface BoardAreaRepo : CrudRepository<BoardArea, Int> {

    fun findByBoard(board: GroupBoard): List<BoardArea>
}