package de.stephaneum.spring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import java.awt.GraphicsEnvironment
import java.time.ZonedDateTime

val START_TIME = ZonedDateTime.now()

@SpringBootApplication
@EnableScheduling
@EnableAsync
class BackendApplication

fun main(args: Array<String>) {
    if (System.console() != null || GraphicsEnvironment.isHeadless())
        runApplication<BackendApplication>(*args) // start console mode
    else
        Gui.open(args) // start graphical mode
}