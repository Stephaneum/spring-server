package de.stephaneum.spring.database

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity
@Table(name="ordner")
data class Folder(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                  var id: Int = 0,

                  @Column(nullable = false)
                  var name: String = "",

                  @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                  @JoinColumn(name = "eigentum")
                  var user: User? = null,

                  @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                  @JoinColumn(name = "projekt_id")
                  var project: Project? = null,

                  @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                  @JoinColumn(name = "klasse_id")
                  var schoolClass: SchoolClass? = null,

                  @Column(nullable = false, name="lehrerchat")
                  var teacherChat: Boolean = false,

                  @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                  @JoinColumn(name = "parent")
                  var parent: Folder? = null)

@Repository
interface FolderRepo: CrudRepository<Folder, Int>