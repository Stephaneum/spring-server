package de.stephaneum.spring.helper

import de.stephaneum.spring.database.SchoolClass
import org.springframework.stereotype.Service

@Service
class ClassService {

    private val classRegex = Regex("(\\d+)(\\D+\\d*)")

    fun parse(raw: String): SchoolClass {
        val match = classRegex.find(raw) ?: throw ErrorCode(418, "invalid school class: $raw")
        val (gradeRaw, suffix) = match.destructured
        val grade = gradeRaw.toIntOrNull() ?: throw ErrorCode(418, "invalid school class: $raw")

        return SchoolClass(grade = grade, suffix = suffix)
    }
}