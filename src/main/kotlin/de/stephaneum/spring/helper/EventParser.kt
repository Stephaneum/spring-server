package de.stephaneum.spring.helper

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Event(val title: String, val link: String?, val allDay: Boolean, val start: LocalDateTime, val end: LocalDateTime?)

@Service
class EventParser {

    private val patternDateTime = DateTimeFormatterBuilder()
            .appendPattern("dd.MM.yyyy")
            .optionalStart()
            .appendPattern(" HH:mm")
            .optionalEnd()
            .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
            .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
            .toFormatter()

    fun parse(raw: String): List<Event> {
        return raw.lines().map { line ->
            val comma = line.indexOf(',')

            if(comma == -1) {
                null
            } else {
                val postComma = line.substring(comma+1, line.length)
                val (title, link) = when (val http = postComma.indexOf("http")) {
                    -1 -> Pair(postComma, null)
                    else -> Pair(postComma.substring(0, http), postComma.substring(http, postComma.length))
                }

                val time = line.substring(0, comma)
                val timeSplit = time.split("-")

                try {
                    val (start, end) = if(timeSplit.size == 2) {
                        // start and end
                        val start = parseTime(timeSplit[0].trim())
                        val end = parseTime(timeSplit[1].trim())

                        if(end.hour == 0 && end.minute == 0)
                            end.plusDays(1) // because last day is exclusive

                        Pair(start, end)
                    } else {
                        // start only
                        Pair(parseTime(time.trim()), null)
                    }

                    val allDay = start.hour == 0 && start.minute == 0

                    Event(title, link, allDay, start, end)
                } catch (e: DateTimeParseException) {
                    println("RIP: ${e.message}")
                    null
                }
            }
        }.filterNotNull().sortedBy { it.start }
    }

    private fun parseTime(time: String): LocalDateTime {
        return LocalDateTime.parse(time, patternDateTime)
    }
}