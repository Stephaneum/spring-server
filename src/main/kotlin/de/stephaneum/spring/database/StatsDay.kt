package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import javax.persistence.*

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name="stats_tage")
data class StatsDay(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                    var id: Int = 0,

                    @Column(nullable = false, name = "datum")
                    var date: LocalDate,

                    @Column(nullable = false, name = "anzahl")
                    var count: Int = 0)

@Repository
interface StatsDayRepo: CrudRepository<StatsDay, Int> {

    fun findByDate(timestamp: LocalDate): StatsDay?
}