package de.stephaneum.spring.features.cms

object Response {
    data class Feedback(val success: Boolean, val needLogin: Boolean = false, val message: String? = null)

    data class PostManager(val maxPictureSize: Int)
}