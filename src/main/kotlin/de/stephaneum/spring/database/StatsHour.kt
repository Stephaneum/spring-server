package de.stephaneum.spring.database

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity
@Table(name="stats_stunden")
data class StatsHour(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                     var idx: Int = 0,

                     @Column(nullable = false, name = "uhrzeit")
                     var hour: Int = 0)

@Repository
interface StatsHourRepo: CrudRepository<StatsHour, Int>