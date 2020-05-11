package de.stephaneum.spring.helper

import de.stephaneum.spring.database.*
import org.springframework.stereotype.Service

@Service
class GroupService (
        private val logService: LogService,
        private val fileService: FileService,
        private val groupRepo: GroupRepo,
        private val userGroupRepo: UserGroupRepo,
        private val fileRepo: FileRepo
) {

    fun deleteGroupAndChildren(initiator: User, group: Group) {
        deleteFilesGroupRecursive(initiator, group)

        groupRepo.delete(group) // folder will be deleted also
        logService.log(EventType.DELETE_GROUP, initiator, group.name)
    }

    /**
     * deletes all files of a group and all its sub groups
     */
    private fun deleteFilesGroupRecursive(user: User, group: Group) {
        val files = fileRepo.findByGroup(group)
        files.forEach { fileService.deleteFileStephaneum(user, it) } // delete all files

        groupRepo.findByParent(group).forEach { deleteFilesGroupRecursive(user, it) }
    }

    /**
     * removes a user from a group, and all its sub groups
     */
    fun removeUserFromGroupRecursive(userGroup: UserGroup) {
        val files = fileRepo.findByUserAndGroup(userGroup.user, userGroup.group)
        files.forEach { fileService.deleteFileStephaneum(userGroup.user, it) }
        userGroupRepo.delete(userGroup)
        logService.log(EventType.QUIT_GROUP, userGroup.user, userGroup.group.name)
        userGroupRepo.findByUserAndGroupParent(userGroup.user, userGroup.group).forEach { removeUserFromGroupRecursive(it) }
    }
}