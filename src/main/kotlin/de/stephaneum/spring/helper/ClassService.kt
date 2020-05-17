package de.stephaneum.spring.helper

import de.stephaneum.spring.database.SchoolClass
import org.springframework.stereotype.Service

@Service
class ClassService {

    private val classRegex = Regex("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")

    fun parse(raw: String): SchoolClass {
        val split = raw.split(classRegex)
        if (split.size <= 1)
            throw ErrorCode(418, "invalid school class")

        val grade = split.first().toIntOrNull() ?: throw ErrorCode(412, "invalid school class")
        val suffix = split.subList(1, split.size).joinToString(separator = "")
        return SchoolClass(grade = grade, suffix = suffix)
    }
}