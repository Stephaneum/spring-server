package de.stephaneum.spring.database

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

// TODO: implement enum-native approach
enum class SliderDirection (val code: String, val description: String) {
    LEFT("left-align", "von links"),
    TOP("center-align", "von oben"),
    RIGHT("right-align", "von rechts")
}

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Slider(@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
                  var id: Int = 0,

                  @Column(nullable = false)
                  var index: Int = 0,

                  @Column(nullable = false, length = 1024)
                  @JsonIgnore
                  var path: String = "",

                  @Column(nullable = true)
                  var title: String? = null,

                  @Column(nullable = true)
                  var subTitle: String? = null,

                  @Column(nullable = false)
                  var direction: String = "") {

    // for json
    fun getName(): String {
        val index = path.indexOf("slider_")
        return if(index != -1) {
            path.substring(index+7, path.length-4)
        } else {
            ""
        }
    }
}

@Repository
interface SliderRepo: CrudRepository<Slider, Int> {

    fun findByOrderByIndex(): List<Slider>
}