package de.stephaneum.spring.features.cloud

object Request {
}

object Response {
    data class Feedback(val success: Boolean, val needLogin: Boolean = false, val message: String? = null)
}