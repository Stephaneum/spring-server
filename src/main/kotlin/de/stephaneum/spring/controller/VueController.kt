package de.stephaneum.spring.controller

import de.stephaneum.spring.Session
import de.stephaneum.spring.helper.CountService
import de.stephaneum.spring.security.JwtService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import javax.servlet.http.HttpServletRequest

@Controller
class VueController (
        private val jwtService: JwtService,
        private val countService: CountService
) {

    @GetMapping("/", "/m/{id}", "/beitrag/{id}", "/login",
            "/statistiken", "/termine", "/geschichte", "/eu-sa", "/kontakt", "/impressum", "/sitemap",
            "/home", "/user-manager", "/config-manager", "/static-manager", "/code-manager", "/logs",
            "/plan-manager", "/menu-manager", "/post-manager", "/groups", "/groups/{id}", "/cloud", "/account")
    fun html(@RequestParam(required = false) key: String?,
             @PathVariable(required = false) id: Int?,
             @RequestHeader(value="User-Agent", required = false) userAgent: String?,
             @RequestHeader(value="X-Forwarded-For", required = false) forwardedIP: String?,
             request: HttpServletRequest): String {
        // login
        if(key != null) {
            Session.get().user = jwtService.getUser(key)
            if(Session.get().user != null) {
                return "redirect:${request.requestURL}"
            }
        }

        val (_, created) = Session.createIfNotExists()
        if (created) {
            val ip = resolveIP(forwardedIP, request)
            countService.count(ip, userAgent ?: "")
        }

        return "forward:/static/index.html"
    }

    private fun resolveIP(forwardedIP: String?, request: HttpServletRequest): String {
        return forwardedIP?.split(",")?.first()?.trim() ?: request.remoteAddr
    }

}