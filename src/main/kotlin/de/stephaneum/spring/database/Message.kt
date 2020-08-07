package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonInclude
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import javax.persistence.*

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Message(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                   var id: Int = 0,

                   @ManyToOne(optional = false) @OnDelete(action = OnDeleteAction.CASCADE)
                   var user: User = User(),

                   @ManyToOne(optional = false) @OnDelete(action = OnDeleteAction.CASCADE)
                   var group: Group = Group(),

                   @Column(nullable = false)
                   var text: String = "",

                   @Column(nullable = false)
                   var timestamp: Timestamp = Timestamp(0))

data class SimpleMessage(val id: Int, val text: String, val user: MiniUser, val timestamp: Timestamp)

@Repository
interface MessageRepo: CrudRepository<Message, Int> {

    fun countByGroup(group: Group): Int

    fun findByGroupOrderById(group: Group): List<Message>

    @Transactional
    fun deleteByGroup(group: Group)
}