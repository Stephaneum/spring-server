package de.stephaneum.spring.rest.objects

object Request {
    data class Login(val email: String?, val password: String?)
    data class Password(val password: String?)

    data class MenuWriteRule(val user: Int, val menu: Int?)
}