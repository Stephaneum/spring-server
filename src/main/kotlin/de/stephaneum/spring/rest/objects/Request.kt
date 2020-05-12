package de.stephaneum.spring.rest.objects

object Request {
    data class Login(val email: String?, val password: String?)
    data class Password(val password: String?)
    data class ChangePassword(val oldPassword: String, val newPassword: String)
    data class Email(val email: String)
    data class MenuWriteRule(val user: Int, val menu: Int?)

    // user api
    data class ChangePasswordBatch(val password: String, val role: Int)
    data class DeleteByRole(val password: String, val role: Int)
    data class UpdateQuotasBatch(val storage: Int, val grade: Int)
    data class UpdateUser(val user: Int, val firstName: String, val lastName: String, val email: String, val password: String?, val storage: Int, val permissionLogin: Boolean, val permissionPlan: Boolean)
    data class ImportUsers(val data: String, val format: Int, val separator: String, val password: String?, val role: Int)
}