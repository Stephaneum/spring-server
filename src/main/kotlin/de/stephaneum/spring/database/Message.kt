package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import javax.persistence.*

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name="nachricht")
data class Message(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                   var id: Int = 0,

                   @Column(nullable = false, name = "string")
                   var text: String = "",

                   @ManyToOne(optional = false) @OnDelete(action = OnDeleteAction.CASCADE)
                   @JoinColumn(name = "nutzer_id")
                   var user: User = User(),

                   @ManyToOne(optional = false) @OnDelete(action = OnDeleteAction.CASCADE)
                   @JoinColumn(name = "projekt_id")
                   var group: Group = Group(),

                   @Column(nullable = false, name = "lehrerchat")
                   @JsonIgnore
                   var teacherChat: Boolean = false,

                   @Column(nullable = false, name = "datum")
                   var timestamp: Timestamp = Timestamp(0))

@Repository
interface MessageRepo: CrudRepository<Message, Int> {

    fun findByGroupOrderByTimestamp(group: Group): List<Message>
}