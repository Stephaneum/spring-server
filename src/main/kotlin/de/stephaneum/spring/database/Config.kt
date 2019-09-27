package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name="konfig")
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