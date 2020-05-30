package de.stephaneum.spring

import de.stephaneum.spring.database.Menu
import de.stephaneum.spring.database.Post
import de.stephaneum.spring.database.ROLE_ADMIN
import de.stephaneum.spring.database.User
import de.stephaneum.spring.helper.ErrorCode
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

data class StephSession(var permission: Permission = Permission.NONE, var user: User? = null, val unlockedSections: MutableSet<Int> = mutableSetOf(), val unlockedPosts: MutableSet<Int> = mutableSetOf())
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
     * @return the user and true if session has been created during method call
     */
    fun createIfNotExists(): Pair<StephSession, Boolean> {
        val attr = RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes
        val session = attr.request.getSession(true)
        var stephSession = session.getAttribute(KEY) as StephSession?
        return if (stephSession == null) {
            // create new if not exists
            stephSession = StephSession()
            session.setAttribute(KEY, stephSession)
            Pair(stephSession, true)
        } else {
            Pair(stephSession, false)
        }
    }

    fun getUser(adminOnly: Boolean = false): User {
        val user = get().user ?: throw ErrorCode(401, "Unauthorized")
        if(adminOnly && user.code.role != ROLE_ADMIN)
            throw ErrorCode(403, "admin only")

        return user
    }

    fun hasAccess(menu: Menu): Boolean {
        val (session, _) = createIfNotExists()
        return session.unlockedSections.contains(menu.id)
    }

    fun hasAccess(post: Post): Boolean {
        val (session, _) = createIfNotExists()
        return session.unlockedPosts.contains(post.id)
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

    fun mustHaveAccess(permission: Permission) {
        if(get().permission != permission)
            throw ErrorCode(403, "no permission")
    }
}

