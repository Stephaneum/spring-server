package de.stephaneum.spring.helper

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import javax.servlet.http.HttpServletRequest

val windows = System.getProperty("os.name").lowercase().contains("windows")

/**
 * @param command the linux/windows command
 * @param workingDir the location in the harddrive where this command will be executed
 * @return exit status
 */
fun cmd(command: String, workingDir: String = if(windows) "C:/" else "/", sudoPassword: String? = null): Int {
    var commandEdited = command
    if(sudoPassword != null) {
        commandEdited = "echo $sudoPassword | sudo -S $command"
    }

    val shell = when {
        windows -> "cmd.exe"
        else -> {
            val shell = System.getenv("SHELL") ?: run {
                val candidates = listOf("/bin/bash", "/bin/sh", "/usr/bin/bash", "/usr/bin/sh")
                candidates.firstOrNull { File(it).exists() } ?: "/bin/sh"
            }
            shell
        }
    }

    return ProcessBuilder(shell, if(windows) "/c" else "-c", commandEdited)
            .directory(File(workingDir))
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor()
}

/**
 *  @return true if client is using IE, otherwise false
 */
fun checkIE(request: HttpServletRequest): Boolean {
    val userAgent = request.getHeader("user-agent")?.lowercase() ?: return false
    return userAgent.contains("msie") || userAgent.contains("trident")
}

val mapper = jacksonObjectMapper()

/*
 * Extension function to convert anything to json
 * usage: val s = a.toJSON(), where a is any object
 */
fun Any.toJSON(): String {
    return mapper.writeValueAsString(this)
}

/*
 * Extension function to parse json
 * usage: val obj = s.parseJSON(), where s is a string
 */
inline fun <reified T> String.parseJSON(): T {
    return mapper.readValue(this)
}

fun resolveIP(forwardedIP: String?, request: HttpServletRequest): String {
    return forwardedIP?.split(",")?.first()?.trim() ?: request.remoteAddr
}