package de.stephaneum.spring.rest

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.*
import de.stephaneum.spring.helper.ErrorCode
import de.stephaneum.spring.helper.obj
import de.stephaneum.spring.helper.toMiniUser
import org.springframework.data.repository.findByIdOrNull
import org.springframework.web.bind.annotation.*

data class MessageCount(val count: Int)
data class AddMessage(val message: String)

@RestController
@RequestMapping("/api/chat")
class ChatAPI (
        private val messageRepo: MessageRepo,
        private val userGroupRepo: UserGroupRepo,
        private val groupRepo: GroupRepo
) {

    @GetMapping("/{groupID}/count")
    fun getMessageCount(@PathVariable groupID: Int): MessageCount {
        // no checking needed, message count is not important anyway
        return MessageCount(messageRepo.countByGroup(groupID.obj()))
    }

    @GetMapping("/{groupID}")
    fun getMessages(@PathVariable groupID: Int): List<SimpleMessage> {

        val user = Session.getUser()

        val group = groupRepo.findByIdOrNull(groupID) ?: throw ErrorCode(404, "group not found")
        if(user.code.role != ROLE_ADMIN && !userGroupRepo.existsByUserAndGroup(user, group))
            throw ErrorCode(403, "you are not member of this group")

        return messageRepo
                .findByGroupOrderById(group)
                .map { SimpleMessage(it.id, it.text, it.user.toMiniUser(), it.timestamp) }
    }

    @PostMapping("/{groupID}")
    fun addMessage(@PathVariable groupID: Int, @RequestBody request: AddMessage) {

        val user = Session.getUser()

        if(request.message.isBlank())
            throw ErrorCode(400, "empty message")

        val group = groupRepo.findByIdOrNull(groupID) ?: throw ErrorCode(404, "group not found")

        if (!group.chat)
            throw ErrorCode(403, "chat for all members deactivated")

        if (user.code.role != ROLE_ADMIN) {
            val connection = userGroupRepo.findByUserAndGroup(user, group)
                ?: throw ErrorCode(404, "you are not member of this group")

            if (group.leader.id != user.id && !connection.teacher && !connection.chat)
                throw ErrorCode(403, "not allowed to chat")
        }

        messageRepo.save(Message(0, user, group, request.message, now()))
    }

    @PostMapping("/{groupID}/clear")
    fun clear(@PathVariable groupID: Int) {

        val user = Session.getUser()
        val group = groupRepo.findByIdOrNull(groupID) ?: throw ErrorCode(404, "group not found")

        if (user.code.role != ROLE_ADMIN) {
            val connection = userGroupRepo.findByUserAndGroup(user, group) ?: throw ErrorCode(404, "you are not member of this group")

            if(group.leader.id != user.id && !connection.teacher)
                throw ErrorCode(403, "you are not admin nor teacher")
        }

        messageRepo.deleteByGroup(group)
    }

    @PostMapping("/delete/{id}")
    fun deleteMessage(@PathVariable id: Int) {

        val user = Session.getUser()
        val message = messageRepo.findByIdOrNull(id) ?: throw ErrorCode(404, "message not found")
        val group = groupRepo.findByIdOrNull(message.group.id) ?: throw ErrorCode(404, "group not found")

        if (user.code.role != ROLE_ADMIN) {
            val connection = userGroupRepo.findByUserAndGroup(user, group) ?: throw ErrorCode(404, "you are not member of this group")

            if(group.leader.id != user.id && !connection.teacher)
                throw ErrorCode(403, "you are not admin nor teacher")
        }

        messageRepo.delete(message)
    }
}