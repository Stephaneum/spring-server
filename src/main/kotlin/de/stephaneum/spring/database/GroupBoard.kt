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
data class GroupBoard(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                      var id: Int = 0,

                      @ManyToOne(optional = false) @OnDelete(action = OnDeleteAction.CASCADE)
                      var group: Group = Group(),

                      @Column(nullable = false)
                      var lastUpdate: Timestamp = now())

@Repository
interface GroupBoardRepo : CrudRepository<GroupBoard, Int> {

    fun findByGroup(group: Group): GroupBoard?
}