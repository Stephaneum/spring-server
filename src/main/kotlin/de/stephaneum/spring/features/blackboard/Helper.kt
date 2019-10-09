package de.stephaneum.spring.features.blackboard

import de.stephaneum.spring.database.Blackboard
import de.stephaneum.spring.database.Type

val REDIRECT_LOGIN = "redirect:/blackboard/login"
val REDIRECT_ADMIN = "redirect:/blackboard/admin"

object Request {
    data class Login(val password: String?)
    data class Type(val type: de.stephaneum.spring.database.Type)
    data class Duration(val duration: Int?)
    data class Value(val value: String?)
}

object Response {

    data class Timestamp(val timestamp: Long?)

    data class Feedback(val success: Boolean, val needLogin: Boolean = false, val message: String? = null)
    data class AdminData(val paused: Boolean, val types: List<Type>, val boards: List<Blackboard>)
    data class AdminInfo(val activeID: Int, val activeSeconds: Int, val activeClients: Int, val timeToRefresh: Int)
}