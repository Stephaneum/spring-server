package de.stephaneum.spring.features.cms

import de.stephaneum.spring.database.File
import de.stephaneum.spring.database.Menu

object Request {
    data class CreatePost(val title: String?,
                          val text: String?,
                          val images: List<File>,
                          val layoutPost: Int,
                          val layoutPreview: Int,
                          val preview: Int,
                          val menuID: Int?)
}

object Response {
    data class Feedback(val success: Boolean, val needLogin: Boolean = false, val message: String? = null)

    data class PostManager(val maxPictureSize: Int, val category: List<Menu>)
}