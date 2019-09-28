package de.stephaneum.spring.features.general

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.Code
import de.stephaneum.spring.database.ROLE_NO_LOGIN
import de.stephaneum.spring.database.User
import de.stephaneum.spring.scheduler.ConfigFetcher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class API {

    @Autowired
    private lateinit var configFetcher: ConfigFetcher

    @GetMapping("/info")
    fun get(): Info {
        val user = Session.get().user ?: User(-1, Code(-1, "", ROLE_NO_LOGIN))
        val copyright = configFetcher.copyright
        return Info(user, copyright)
    }
}