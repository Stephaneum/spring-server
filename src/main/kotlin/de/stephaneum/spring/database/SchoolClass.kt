package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SchoolClass(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                       var id: Int = 0,

                       @Column(nullable = false)
                       var grade: Int = 0,

                       @Column(nullable = false, length=3)
                       var suffix: String = "")

@Repository
interface SchoolClassRepo: CrudRepository<SchoolClass, Int> {

    fun findByGradeAndSuffix(grade: Int, suffix: String): SchoolClass?
}