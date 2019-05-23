package de.stephaneum.backend.database

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity
@Table(name="Konfig")
data class Config(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                  var id: Int = 0,

                  @Column(nullable = false, name="variable")
                  var key: String = "",

                  @Column(nullable = true, name="wert")
                  var value: String? = null)

@Repository
interface ConfigRepo: CrudRepository<Config, Int> {

    fun findByKey(key: String): Config?
}