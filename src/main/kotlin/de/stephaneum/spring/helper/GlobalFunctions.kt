package de.stephaneum.spring.helper

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import javax.servlet.http.HttpServletRequest

val windows = System.getProperty("os.name").toLowerCase().contains("windows")

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
    return ProcessBuilder(if(windows) "cmd.exe" else "/bin/sh", if(windows) "/c" else "-c", commandEdited)
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
    val userAgent = request.getHeader("user-agent") ?: return false
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