package de.stephaneum.spring.features.chat

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.ErrorCode
import de.stephaneum.spring.helper.obj
import de.stephaneum.spring.helper.toSimpleUser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chat")
class ChatAPI {

    @Autowired
    private lateinit var messageRepo: MessageRepo

    @Autowired
    private lateinit var userGroupRepo: UserGroupRepo

    @Autowired
    private lateinit var groupRepo: GroupRepo

    @Autowired
    private lateinit var schoolClassRepo: SchoolClassRepo

    @GetMapping("/group/{groupID}/count", "/class/{classID}/count", "/teacher/count")
    fun getMessageCount(@PathVariable(required = false) groupID: Int?,
                        @PathVariable(required = false) classID: Int?): MessageCount {

        // no checking needed, message count is not important anyway
        val count = when {
            groupID != null -> messageRepo.countByGroup(groupID.obj())
            classID != null -> messageRepo.countBySchoolClass(classID.obj())
            else -> messageRepo.countByTeacherChat(true)
        }
        return MessageCount(count)
    }

    @GetMapping("/group/{groupID}", "/class/{classID}", "/teacher")
    fun getMessages(@PathVariable(required = false) groupID: Int?,
                    @PathVariable(required = false) classID: Int?): List<SimpleMessage> {

        val user = Session.get().user ?: throw ErrorCode(401, "login")

        val messages = doWhenNormalAccess(user, groupID, classID) { group, schoolClass, teacherChat ->
            when {
                group != null -> messageRepo.findByGroupOrderById(group)
                schoolClass != null -> messageRepo.findBySchoolClassOrderById(schoolClass)
                teacherChat -> messageRepo.findByTeacherChatOrderById(true)
                else -> throw ErrorCode(500, "internal error")
            }
        }
        return messages.map { SimpleMessage(it.id, it.text, it.user.toSimpleUser(), it.timestamp) }
    }

    @PostMapping("/group/{groupID}", "/class/{classID}", "/teacher")
    fun addMessage(@PathVariable(required = false) groupID: Int?,
                   @PathVariable(required = false) classID: Int?,
                   @RequestBody request: AddMessage) {

        val user = Session.get().user ?: throw ErrorCode(401, "login")

        if(request.message.isEmpty())
            throw ErrorCode(400, "empty message")

        doWhenNormalAccess(user, groupID, classID) { group, schoolClass, teacherChat ->
            when {
                group != null -> messageRepo.save(Message(0, request.message, user, group, null, false, now()))
                schoolClass != null -> messageRepo.save(Message(0, request.message, user, null, schoolClass, false, now()))
                teacherChat -> messageRepo.save(Message(0, request.message, user, null, null, true, now()))
                else -> throw ErrorCode(500, "internal error")
            }
        }
    }

    @PostMapping("/group/{groupID}/clear", "/class/{classID}/clear", "/teacher/clear")
    fun clear(@PathVariable(required = false) groupID: Int?,
              @PathVariable(required = false) classID: Int?) {

        val user = Session.get().user ?: throw ErrorCode(401, "login")

        doWhenAdminAccess(user, groupID, classID) { group, schoolClass, teacherChat ->
            when {
                group != null -> messageRepo.deleteByGroup(group)
                schoolClass != null -> messageRepo.deleteBySchoolClass(schoolClass)
                teacherChat -> messageRepo.deleteByTeacherChat(true)
                else -> throw ErrorCode(500, "internal error")
            }
        }
    }

    @PostMapping("/delete/{id}")
    fun deleteMessage(@PathVariable id: Int) {

        val user = Session.get().user ?: throw ErrorCode(401, "login")
        val message = messageRepo.findByIdOrNull(id) ?: throw ErrorCode(404, "message not found")

        doWhenAdminAccess(user, message.group?.id, message.schoolClass?.id) { _, _, _ ->
            messageRepo.delete(message)
        }
    }

    /**
     * do something if the user has normal or administrative access to the group / class / teacher chat
     */
    fun<T> doWhenNormalAccess(user: User, groupID: Int?, classID: Int?, action: (group: Group?, schoolClass: SchoolClass?, teacherChat: Boolean) -> T): T {
        return when {
            groupID != null -> {
                val group = groupRepo.findByIdOrNull(groupID) ?: throw ErrorCode(404, "group not found")
                if(user.code.role != ROLE_ADMIN && !userGroupRepo.existsByUserAndGroup(user, groupID.obj()))
                    throw ErrorCode(403, "you are not member of this group")

                action(group, null, false)
            }
            classID != null -> {
                val schoolClass = schoolClassRepo.findByIdOrNull(classID) ?: throw ErrorCode(404, "class not found")
                if(user.code.role != ROLE_ADMIN && user.schoolClass?.id != classID)
                    throw ErrorCode(403, "you are not member of this class")

                action(null, schoolClass, false)
            }
            else -> {
                if(user.code.role != ROLE_ADMIN && user.code.role != ROLE_TEACHER)
                    throw ErrorCode(403, "you are not teacher nor admin")

                action(null, null, true)
            }
        }
    }

    /**
     * do something if the user has administrative access to the group / class / teacher chat
     */
    fun<T> doWhenAdminAccess(user: User, groupID: Int?, classID: Int?, action: (group: Group?, schoolClass: SchoolClass?, teacherChat: Boolean) -> T): T {
        return when {
            groupID != null -> {
                val group = groupRepo.findByIdOrNull(groupID) ?: throw ErrorCode(404, "group not found")

                if(user.code.role == ROLE_ADMIN) {
                    action(group, null, false)
                } else {
                    val connection = userGroupRepo.findByUserAndGroup(user, groupID.obj()) ?: throw ErrorCode(404, "you are not member of this group")

                    if(group.leader.id != user.id && !connection.teacher)
                        throw ErrorCode(403, "you are not admin nor teacher")

                    action(group, null, false)
                }
            }
            classID != null -> {
                val schoolClass = schoolClassRepo.findByIdOrNull(classID) ?: throw ErrorCode(404, "class not found")
                if(user.code.role != ROLE_ADMIN && user.code.role != ROLE_TEACHER)
                    throw ErrorCode(403, "you are not admin or teacher")

                action(null, schoolClass, false)
            }
            else -> {
                if(user.code.role != ROLE_ADMIN && user.code.role != ROLE_TEACHER)
                    throw ErrorCode(403, "you are not teacher nor admin")

                action(null, null, true)
            }
        }
    }

}