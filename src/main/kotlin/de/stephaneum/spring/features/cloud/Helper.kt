package de.stephaneum.spring.features.cloud

object Request {

    data class CreateFolder(val name: String?, val parentID: Int?, val projectID: Int?, val classID: Int?, val teacherChat: Boolean?)

    data class UpdatePublic(val fileID: Int?, val isPublic: Boolean?)

    data class MoveFile(val fileID: Int?, val parentFolderID: Int?)

    data class MoveFolder(val folderID: Int?, val parentFolderID: Int?)
}

object Response {
    data class Feedback(val success: Boolean, val needLogin: Boolean = false, val message: String? = null)

    data class CloudInfo(val used: Int, val total: Int)
}