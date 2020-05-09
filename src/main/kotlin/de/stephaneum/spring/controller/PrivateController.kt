package de.stephaneum.spring.controller

import de.stephaneum.spring.Session
import de.stephaneum.spring.helper.checkIE
import de.stephaneum.spring.security.JwtService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import javax.servlet.http.HttpServletRequest

@Controller
class PrivateController (
        private val jwtService: JwtService
) {

    // mapping for internal routes (without /groups/{id})
    private val templateMapping = mapOf(
            "/admin-config" to "admin-config",
            "/admin-static" to "admin-static",
            "/admin-codes" to "admin-codes",
            "/admin-logs" to "admin-logs",
            "/plan-manager" to "plan-manager",
            "/menu-manager" to "menu-manager",
            "/post-manager" to "post-manager",
            "/groups" to "group-overview",
            "/cloud" to "user-cloud"
    )

    @GetMapping("/admin-config", "/admin-static", "/admin-codes", "/admin-logs",
            "/plan-manager", "/menu-manager", "/post-manager", "/groups", "/cloud")
    fun get(@RequestParam(required = false) key: String?, request: HttpServletRequest): String {

        if(checkIE(request))
            return "forward:/static/no-support-ie.html"

        // login
        if(key != null) {
            Session.get().user = jwtService.getUser(key)
            if(Session.get().user != null) {
                return "redirect:${request.requestURL}"
            }
        }

        return templateMapping[request.requestURI] ?: "404"
    }

    @GetMapping("/groups/{id}")
    fun singleGroup(@PathVariable id: String, @RequestParam(required = false) key: String?, request: HttpServletRequest): String {

        if(checkIE(request))
            return "forward:/static/no-support-ie.html"

        // login
        if(key != null) {
            Session.get().user = jwtService.getUser(key)
            if(Session.get().user != null) {
                return "redirect:${request.requestURL}"
            }
        }

        return "group"
    }

}