package de.stephaneum.spring.features.cloud

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
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

    @GetMapping("/info")
    fun getInfo(): Any {
        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)

        val used = fileRepo.calcStorageUsed(user.id)
        val total = user.storage

        return Response.CloudInfo(used, total)
    }

    @GetMapping("/user")
    fun getCloudUserRoot(): Any {

        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)

        val folders = folderRepo.findFolderInRoot(user, null, null, false)
        val files = fileRepo.findByUserAndFolderOrderByIdDesc(user, null)

        return digestResults(folders, files)
    }

    @GetMapping("/user/{folder}")
    fun getCloudUser(@PathVariable folder: Int): Any {
        val user = Session.get().user ?: return Response.Feedback(false, needLogin = true)
        val folderObj = Folder(folder)
        val folders = folderRepo.findByParent(folderObj)
        val files = fileRepo.findByUserAndFolderOrderByIdDesc(user, folderObj)

        return digestResults(folders, files)
    }

    private fun calcSizeRecursive(folder: Folder): Int {
        val fileSize = fileRepo.findByFolderOrderByIdDesc(folder).sumBy { it.size }
        folder.size = fileSize
        folderRepo.findByParent(folder).forEach { f ->
            folder.size += calcSizeRecursive(f)
        }
        return folder.size
    }

    private fun digestResults(folders: List<Folder>, files: List<File>): List<Any> {
        for (f in folders) {
            f.simplifyForCloud()
            calcSizeRecursive(f)
        }
        files.forEach { f -> f.simplifyForCloud() }

        return List<Any>(folders.size + files.size) { i ->
            if(i < folders.size)
                folders[i]
            else
                files[i-folders.size]
        }
    }

}