package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.sql.Timestamp
import javax.persistence.*

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name="stats_cloud")
data class StatsCloud(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                      var id: Int = 0,

                      @Column(nullable = false, name = "datum")
                      var timestamp: Timestamp = Timestamp(0),

                      @Column(nullable = false)
                      var size: Int = 0)

@Repository
interface StatsCloudRepo: CrudRepository<StatsCloud, Int>