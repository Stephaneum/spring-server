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
data class UserMenu(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                    var id: Int = 0,

                    @ManyToOne(optional = false) @OnDelete(action = OnDeleteAction.CASCADE)
                    var user: User = User(),

                    @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                    var menu: Menu? = null)

@Repository
interface UserMenuRepo : CrudRepository<UserMenu, Int> {

    fun findByUser(user: User): List<UserMenu>

    @Query("SELECT m.menu FROM UserMenu m WHERE m.user = ?1")
    fun findByMenu(user: User): List<Menu>

    fun existsByUserAndMenuIsNull(user: User): Boolean
    fun existsByUserAndMenu(user: User, menu: Menu): Boolean

    @Transactional
    fun deleteByUserAndMenu(user: User, menu: Menu?)

    @Transactional
    fun deleteByUser(user: User)
}