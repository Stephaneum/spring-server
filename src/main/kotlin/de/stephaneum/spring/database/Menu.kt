package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonInclude
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Menu(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                var id: Int = 0,

                @Column(nullable = false, length = 32)
                var name: String = "",

                @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                var parent: Menu? = null,

                @Column(nullable = false)
                var priority: Int = 0,

                @Column(nullable = true)
                var link: String? = null,

                @Column(nullable = true, length = 32)
                var password: String? = null,

                @JsonInclude
                @Transient
                var children: List<Menu> = emptyList()) {

    fun simplify(keepPassword: Boolean = false) {
        parent = null
        if(!keepPassword)
            password = null
        children.forEach { it.simplify(keepPassword) } // recursive call
    }
}

@Repository
interface MenuRepo: CrudRepository<Menu, Int> {

    fun findByParentId(parentId: Int?): List<Menu>
}