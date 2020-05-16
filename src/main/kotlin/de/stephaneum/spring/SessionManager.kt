package de.stephaneum.spring

import de.stephaneum.spring.database.ROLE_ADMIN
import de.stephaneum.spring.database.User
import de.stephaneum.spring.helper.ErrorCode
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

data class StephSession(var permission: Permission = Permission.NONE, var user: User? = null)
data class Toast(var title: String, var content: String? = null)
enum class Permission { NONE, BLACKBOARD, BACKUP }

object Session {

    private const val KEY = "stephaneum"

    fun get(): StephSession {
        val attr = RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes
        val session = attr.request.getSession(true)
        var voteSession = session.getAttribute(KEY) as StephSession?
        if (voteSession == null) {
            // create new if not exists
            voteSession = StephSession()
            session.setAttribute(KEY, voteSession)
        }
        return voteSession
    }

    /**
     * @return true if session has been created during method call
     */
    fun createIfNotExists(): Boolean {
        val attr = RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes
        val session = attr.request.getSession(true)
        var stephSession = session.getAttribute(KEY) as StephSession?
        return if (stephSession == null) {
            // create new if not exists
            stephSession = StephSession()
            session.setAttribute(KEY, stephSession)
            true
        } else {
            false
        }
    }

    fun getUser(adminOnly: Boolean = false): User {
        val user = get().user ?: throw ErrorCode(401, "Unauthorized")
        if(adminOnly && user.code.role != ROLE_ADMIN)
            throw ErrorCode(403, "admin only")

        return user
    }

    fun login(permission: Permission) {
        val session = get()
        session.permission = permission
    }

    fun logout() {
        val session = get()
        session.permission = Permission.NONE
        session.user = null
    }
}

