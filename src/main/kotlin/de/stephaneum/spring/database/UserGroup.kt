package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonInclude
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name="nutzer_projekt")
data class UserGroup(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                     var id: Int = 0,

                     @ManyToOne(optional = false) @OnDelete(action = OnDeleteAction.CASCADE)
                     @JoinColumn(name = "nutzer_id")
                     var user: User = User(),

                     @ManyToOne(optional = false) @OnDelete(action = OnDeleteAction.CASCADE)
                     @JoinColumn(name = "projekt_id")
                     var group: Group = Group(),

                     @Column(nullable = false, name = "betreuer")
                     var teacher: Boolean = false,

                     @Column(nullable = false, name = "akzeptiert")
                     var accepted: Boolean = false,

                     @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
                     var chat: Boolean = true)

@Repository
interface UserGroupRepo: CrudRepository<UserGroup, Int> {

    fun findByUserAndGroupParent(user: User, parent: Group?): List<UserGroup>
    fun findByUserAndGroupParentOrderByGroupName(user: User, parent: Group?): List<UserGroup>
    fun findByUserAndGroup(user: User, group: Group): UserGroup?
    fun findByGroupOrderByUserFirstNameAscUserLastNameAsc(group: Group): List<UserGroup>
    fun countByGroup(group: Group): Int

    fun existsByUserAndGroup(user: User, group: Group): Boolean
}