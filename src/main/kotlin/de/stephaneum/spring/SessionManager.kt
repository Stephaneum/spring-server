package de.stephaneum.spring

import de.stephaneum.spring.database.ROLE_ADMIN
import de.stephaneum.spring.database.User
import de.stephaneum.spring.helper.ErrorCode
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

data class StephSession(var permission: Permission = Permission.NONE, var user: User? = null, var toast: Toast? = null)
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

    fun addToast(title: String) = addToast(title, null)

    fun addToast(title: String, content: String?) {
        get().toast = Toast(title, content)
    }

    fun getAndDeleteToast(): Toast? {
        val session = get()
        val toast = session.toast
        session.toast = null
        return toast
    }
}

