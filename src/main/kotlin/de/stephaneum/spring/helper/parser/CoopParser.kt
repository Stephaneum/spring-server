package de.stephaneum.spring.helper.parser

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.stereotype.Service

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Coop(val country: String, val tooltip: String?, val link: String?)

@Service
class CoopParser {

    fun parse(raw: String): List<Coop> {
        return raw.split(";").map { coopRaw ->
            val link = when(val index = coopRaw.indexOf('[')) {
                -1 -> null
                else -> coopRaw.substring(index + 1, coopRaw.length - 1)
            }
            val tooltip = when(val index = coopRaw.indexOf('(')) {
                -1 -> null
                else -> when(val closeIndex = coopRaw.indexOf(')')) {
                    -1 -> coopRaw.substring(index + 1)
                    else -> coopRaw.substring(index + 1, closeIndex)
                }
            }
            val country = when {
                tooltip != null -> coopRaw.substring(0, coopRaw.indexOf('('))
                link != null -> coopRaw.substring(0, coopRaw.indexOf('['))
                else -> coopRaw
            }
            Coop(country, tooltip, link)
        }
    }
}