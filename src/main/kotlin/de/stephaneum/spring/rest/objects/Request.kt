package de.stephaneum.spring.rest.objects

object Request {
    data class Login(val email: String?, val password: String?)
    data class Password(val password: String?)
    data class ChangePassword(val oldPassword: String, val newPassword: String)
    data class Email(val email: String)

    data class ChangePasswordBatch(val password: String, val role: Int)
    data class DeleteByRole(val password: String, val role: Int)
    data class UpdateQuotasBatch(val storage: Int, val grade: Int)

    data class MenuWriteRule(val user: Int, val menu: Int?)
}