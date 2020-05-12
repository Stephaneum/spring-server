package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonIgnore
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
@Table(name="nachricht")
data class Message(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                   var id: Int = 0,

                   @Column(nullable = false, name = "string")
                   var text: String = "",

                   @ManyToOne(optional = false) @OnDelete(action = OnDeleteAction.CASCADE)
                   @JoinColumn(name = "nutzer_id")
                   var user: User = User(),

                   @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                   @JoinColumn(name = "projekt_id")
                   var group: Group? = null,

                   @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                   var schoolClass: SchoolClass? = null, // only true for the root chat room

                   @Column(nullable = false, name = "lehrerchat")
                   @JsonIgnore
                   var teacherChat: Boolean = false, // only true for root chat room

                   @Column(nullable = false, name = "datum")
                   var timestamp: Timestamp = Timestamp(0))

data class SimpleMessage(val id: Int, val text: String, val user: MiniUser, val timestamp: Timestamp)

@Repository
interface MessageRepo: CrudRepository<Message, Int> {

    fun countByGroup(group: Group): Int
    fun countByTeacherChat(teacherChat: Boolean): Int
    fun countBySchoolClass(schoolClass: SchoolClass): Int

    fun findByGroupOrderById(group: Group): List<Message>
    fun findByTeacherChatOrderById(teacherChat: Boolean): List<Message>
    fun findBySchoolClassOrderById(schoolClass: SchoolClass): List<Message>

    @Transactional
    fun deleteByGroup(group: Group)

    @Transactional
    fun deleteBySchoolClass(schoolClass: SchoolClass)

    @Transactional
    fun deleteByTeacherChat(teacherChat: Boolean)
}