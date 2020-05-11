package de.stephaneum.spring.database

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import javax.persistence.*

enum class StaticMode {
    MIDDLE,
    FULL_WIDTH,
    FULL_SCREEN
}

@Entity
data class Static(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Int = 0,

        @Column(nullable = false)
        var path: String = "",

        @Column(nullable = false)
        @Enumerated(EnumType.STRING)
        var mode: StaticMode = StaticMode.FULL_WIDTH) {

    companion object {
        const val FOLDER_NAME = "static"
    }
}

@Repository
interface StaticRepo: CrudRepository<Static, Int> {

    fun findByPath(path: String): Static?

    fun existsByPath(path: String): Boolean
}