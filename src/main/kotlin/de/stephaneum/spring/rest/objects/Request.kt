package de.stephaneum.spring.rest.objects

object Request {
    data class Login(val email: String?, val password: String?)
}