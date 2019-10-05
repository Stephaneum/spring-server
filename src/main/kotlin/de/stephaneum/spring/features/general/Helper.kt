package de.stephaneum.spring.features.general

import com.fasterxml.jackson.annotation.JsonInclude
import de.stephaneum.spring.database.Menu
import de.stephaneum.spring.database.User

object Request {
    data class Login(val email: String?, val password: String?)
}

object Response {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Feedback(val success: Boolean, val needLogin: Boolean = false, val message: String? = null)

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Info(val user: User, val menu: List<Menu>, val copyright: String?, val plan: Plan, val unapproved: Int?)

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Plan(val exists: Boolean, val info: String?)
}