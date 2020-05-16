package de.stephaneum.spring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import java.time.ZonedDateTime

val START_TIME = ZonedDateTime.now()

@SpringBootApplication
@EnableScheduling
@EnableAsync
class BackendApplication

fun main(args: Array<String>) {
    runApplication<BackendApplication>(*args)
}

class ServletInitializer : SpringBootServletInitializer() {

    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {
        return application.sources(BackendApplication::class.java)
    }

}