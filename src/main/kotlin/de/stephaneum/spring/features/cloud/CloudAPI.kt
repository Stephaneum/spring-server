package de.stephaneum.spring.features.cloud

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.File
import de.stephaneum.spring.database.FileRepo
import de.stephaneum.spring.database.Folder
import de.stephaneum.spring.database.FolderRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/cloud")
class CloudAPI {

    @Autowired
    private lateinit var fileRepo: FileRepo

    @Autowired
    private lateinit var folderRepo: FolderRepo

    @GetMapping("/user")
    fun getCloudUserRoot(): Any {

        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)

        val folders = folderRepo.findByUserAndParentAndProjectAndSchoolClassAndTeacherChatOrderByName(user, null, null, null, false)
        val files = fileRepo.findByUserAndFolderOrderByIdDesc(user, null)

        return digestResults(folders, files)
    }

    @GetMapping("/user/{folder}")
    fun getCloudUser(@PathVariable folder: Int): Any {
        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)
        val folderObj = Folder(folder)
        val folders = folderRepo.findByUserAndParentAndProjectAndSchoolClassAndTeacherChatOrderByName(user, folderObj, null, null, false)
        val files = fileRepo.findByUserAndFolderOrderByIdDesc(user, folderObj)

        return digestResults(folders, files)
    }

    private fun digestResults(folders: List<Folder>, files: List<File>): List<Any> {
        folders.forEach { it.simplifyForCloud() }
        files.forEach { it.simplifyForCloud() }

        return List<Any>(folders.size + files.size) { i ->
            if(i < folders.size)
                folders[i]
            else
                files[i-folders.size]
        }
    }

}