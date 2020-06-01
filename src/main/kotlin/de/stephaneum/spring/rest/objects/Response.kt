package de.stephaneum.spring.rest.objects

import com.fasterxml.jackson.annotation.JsonInclude
import de.stephaneum.spring.database.Menu
import de.stephaneum.spring.database.User
import de.stephaneum.spring.helper.GlobalState

object Response {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Feedback(val success: Boolean, val needLogin: Boolean = false, val message: String? = null)

    data class AccountInfo(val schoolClass: String, val used: Int, val total: Int)

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Info(val state: GlobalState, val user: User, val hasMenuWriteAccess: Boolean, val menu: List<Menu>, val copyright: String?, val plan: Plan, val history: String?, val euSa: String?, val unapproved: Int?)

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Plan(val exists: Boolean, val info: String?)

    data class MenuInfo(val menuAdmin: Boolean, val defaultMenu: Menu)
    data class Priority(val priority: Int)

    data class Variables(
            val storageTeacher: Int,
            val storageStudent: Int,
            val fileLocation: String,
            val backupLocation: String,
            val maxPictureSize: Int,
            val passwordResetTimeout: Int
    )

    // user api
    data class UserInfo(val firstName: String, val lastName: String, val email: String, val role: Int, val storage: Int, val permissionLogin: Boolean, val permissionPlan: Boolean)
}