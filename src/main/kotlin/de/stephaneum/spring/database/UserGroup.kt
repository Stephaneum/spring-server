package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonInclude
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserGroup(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                     var id: Int = 0,

                     @ManyToOne(optional = false) @OnDelete(action = OnDeleteAction.CASCADE)
                     var user: User = User(),

                     @ManyToOne(optional = false) @OnDelete(action = OnDeleteAction.CASCADE)
                     var group: Group = Group(),

                     @Column(nullable = false)
                     var teacher: Boolean = false,

                     @Column(nullable = false)
                     var accepted: Boolean = false,

                     @Column(nullable = false)
                     var chat: Boolean = true,

                     @Column(nullable = false)
                     var writeBoard: Boolean = false)

@Repository
interface UserGroupRepo: CrudRepository<UserGroup, Int> {

    fun findByUserAndGroupParent(user: User, parent: Group?): List<UserGroup>
    fun findByUserAndGroupParentOrderByGroupName(user: User, parent: Group?): List<UserGroup>
    fun findByUserAndGroupParentAndAcceptedOrderByGroupName(user: User, parent: Group?, accepted: Boolean): List<UserGroup>
    fun findByUserAndGroup(user: User, group: Group): UserGroup?
    fun findByGroupOrderByUserFirstNameAscUserLastNameAsc(group: Group): List<UserGroup>
    fun countByGroup(group: Group): Int

    fun existsByUserAndGroup(user: User, group: Group): Boolean
}