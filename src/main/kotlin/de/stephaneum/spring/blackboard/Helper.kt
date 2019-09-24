package de.stephaneum.spring.blackboard

val REDIRECT_LOGIN = "redirect:/blackboard/login"
val REDIRECT_ADMIN = "redirect:/blackboard/admin"

data class TimestampJSON(val timestamp: Long)
data class InfoJSON(val activeID: Int, val activeSeconds: Int, val activeClients: Int, val timeToRefresh: Int)

object Request {
    data class Login(val password: String?)
}

object Response {
    data class Feedback(val success: Boolean)
}