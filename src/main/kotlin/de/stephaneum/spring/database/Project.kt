package de.stephaneum.spring.database

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity
@Table(name="projekt")
data class Project(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                   var id: Int = 0,

                   @Column(nullable = false, length = 100)
                   var name: String = "",

                   @ManyToOne(optional = false) @OnDelete(action = OnDeleteAction.CASCADE)
                   @JoinColumn(name = "leiter_nutzer_id")
                   var leader: User = User(),

                   @Column(nullable = false, name = "akzeptiert")
                   var accepted: Boolean = false,

                   @Column(nullable = false)
                   var chat: Boolean = false,

                   @Column(nullable = false, name="lehrerchat")
                   var teacherChat: Boolean = false)

@Repository
interface ProjectRepo: CrudRepository<Project, Int>