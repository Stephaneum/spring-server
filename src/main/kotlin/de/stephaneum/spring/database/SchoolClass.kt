package de.stephaneum.spring.database

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity
@Table(name="klasse")
data class SchoolClass(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                       var id: Int = 0,

                       @Column(nullable = false, name="stufe")
                       var grade: Int = 0,

                       @Column(nullable = false, length=3)
                       var suffix: String = "")

@Repository
interface SchoolClassRepo: CrudRepository<SchoolClass, Int>