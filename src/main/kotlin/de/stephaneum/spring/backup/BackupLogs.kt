package de.stephaneum.spring.backup

import org.slf4j.LoggerFactory
import java.lang.StringBuilder

object BackupLogs {

    val logger = LoggerFactory.getLogger(BackupLogs::class.java)

    private val logsHTML = StringBuilder()

    fun getLogsHTML(): String {
        return logsHTML.toString()
    }

    fun clear() {
        logsHTML.clear()
    }

    fun addLine(s: String, level: Int = 0) {
        if(level == 0) {
            logsHTML.append("&gt;   $s<br>".replace(" ", "&nbsp;"))
            logger.info(s)
        } else {
            var s2 = s
            repeat(level) { s2 = "            $s2" }
            logsHTML.append("&gt;     $s2<br>".replace(" ", "&nbsp;"))
            logger.info(s)
        }
    }
}