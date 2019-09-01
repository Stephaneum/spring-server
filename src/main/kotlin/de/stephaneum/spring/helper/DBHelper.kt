package de.stephaneum.spring.helper

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class DBHelper {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    fun getDatabaseLocation(): String? {
        return jdbcTemplate.queryForObject("SELECT @@basedir", String::class.java)
    }
}