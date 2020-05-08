package de.stephaneum.spring.rest.objects

import com.fasterxml.jackson.annotation.JsonInclude
import de.stephaneum.spring.database.Menu
import de.stephaneum.spring.database.User

object Response {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Feedback(val success: Boolean, val needLogin: Boolean = false, val message: String? = null)

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Info(val user: User, val hasMenuWriteAccess: Boolean, val menu: List<Menu>, val copyright: String?, val plan: Plan, val history: String?, val euSa: String?, val unapproved: Int?)

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Plan(val exists: Boolean, val info: String?)

    data class WritableMenu(val menu: List<Menu>, val menuAdmin: Boolean)
    data class Priority(val priority: Int)
}