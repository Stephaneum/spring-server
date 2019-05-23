package de.stephaneum.backend

import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

data class StephSession(var loggedIn: Boolean = false)

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

    fun login() {
        val session = get()
        session.loggedIn = true
    }

    fun logout() {
        val session = get()
        session.loggedIn = false
    }
}

