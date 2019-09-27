package de.stephaneum.spring.features.cms

import de.stephaneum.spring.Session
import de.stephaneum.spring.database.UserRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class CMSController {

    @Autowired
    private lateinit var userRepo: UserRepo

    @GetMapping("/post-manager")
    fun get(@RequestParam(defaultValue = "0") key: Int): String {

        // login
        if(key != 0) {
            Session.get().user = userRepo.findByIdOrNull(key)
            if(Session.get().user != null) {
                println(Session.get().user)
                return "redirect:post-manager"
            }
        }

        return "post-manager"
    }

}