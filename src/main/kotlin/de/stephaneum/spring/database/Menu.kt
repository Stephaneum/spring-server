package de.stephaneum.spring.database

import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity
@Table(name="gruppe")
data class Menu(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                var id: Int = 0,

                @Column(nullable = false, length = 32)
                var name: String = "",

                @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                @JoinColumn(name = "grp_id")
                var parent: Menu? = null,

                @Column(nullable = false, name="priory")
                var priority: Int = 0,

                @Column(nullable = true)
                var link: String? = null,

                @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.NO_ACTION)
                @JoinColumn(name = "rubrik_leiter")
                var user: User? = null,

                @ManyToOne(optional = true) @OnDelete(action = OnDeleteAction.CASCADE)
                @JoinColumn(name = "datei_id")
                var image: File? = null,

                @Column(nullable = true, name = "passwort", length = 32)
                var password: String? = null,

                @Column(nullable = true, name = "genehmigt")
                var approved: Boolean? = null)

@Repository
interface MenuRepo: CrudRepository<Menu, Int>