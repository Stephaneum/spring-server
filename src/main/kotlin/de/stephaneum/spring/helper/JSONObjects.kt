package de.stephaneum.spring.helper

object Response {
    data class Feedback(val success: Boolean, val needLogin: Boolean = false, val message: String? = null)
}