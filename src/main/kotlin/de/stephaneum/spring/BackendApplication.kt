package de.stephaneum.spring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import java.time.ZonedDateTime

val START_TIME = ZonedDateTime.now()

@SpringBootApplication
@EnableScheduling
@EnableAsync
class BackendApplication

fun main(args: Array<String>) {
    if(args.isEmpty())
        Gui.open(args)
    else
        runApplication<BackendApplication>(*args)
}