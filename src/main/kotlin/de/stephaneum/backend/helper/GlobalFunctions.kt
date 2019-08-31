package de.stephaneum.backend.helper

import java.io.File
import java.util.concurrent.TimeUnit

fun cmd(command: String, workingDir: String = "/") {
    println(command)
    ProcessBuilder("/bin/sh", "-c", command)
            .directory(File(workingDir))
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor(60, TimeUnit.MINUTES)
}