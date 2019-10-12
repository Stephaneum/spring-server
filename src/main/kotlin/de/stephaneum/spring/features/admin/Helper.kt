package de.stephaneum.spring.features.admin


object Response {
    data class LogInfo(val amount: Long)
    data class Log(val id: Int, val date: String, val type: String, val className: String, val typeID: Int, val info: String)
}