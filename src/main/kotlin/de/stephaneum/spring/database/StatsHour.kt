package de.stephaneum.spring.database

import de.stephaneum.spring.helper.HourCount
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity
data class StatsHour(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                     var idx: Int = 0,

                     @Column(nullable = false)
                     var hour: Int = 0)

@Repository
interface StatsHourRepo: CrudRepository<StatsHour, Int> {

    @Query("""
        SELECT new de.stephaneum.spring.helper.HourCount(s.hour, COUNT(s)) FROM StatsHour s
	        GROUP BY s.hour ORDER BY s.hour
    """)
    fun getStats(): List<HourCount>
}