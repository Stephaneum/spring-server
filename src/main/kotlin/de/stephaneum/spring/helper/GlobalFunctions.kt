package de.stephaneum.spring.helper

import java.io.File

val windows = System.getProperty("os.name").toLowerCase().contains("windows")

/**
 * @param command the linux/windows command
 * @param workingDir the location in the harddrive where this command will be executed
 * @return exit status
 */
fun cmd(command: String, workingDir: String = if(windows) "C:/" else "/"): Int {
    println(command)
    return ProcessBuilder(if(windows) "cmd.exe" else "/bin/sh", if(windows) "/c" else "-c", command)
            .directory(File(workingDir))
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor()
}