package de.stephaneum.spring.features.general

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.EMPTY_USER
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
        val user = Session.get().user ?: EMPTY_USER
        val copyright = configFetcher.copyright
        return Info(user, copyright)
    }
}