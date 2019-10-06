package de.stephaneum.spring.features.plan

import de.stephaneum.spring.scheduler.ConfigFetcher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/plan")
class PlanAPI {

    private val logger = LoggerFactory.getLogger(PlanAPI::class.java)

    @Autowired
    private lateinit var configFetcher: ConfigFetcher

    @PostMapping("/text")
    fun updateText(@RequestParam(required = false) text: String?) {

    }


}