package de.stephaneum.backend.blackboard

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service


@Service
class ActiveClientsScheduler {

    /**
     * this scheduler removes inactive clients from the list
     */

    private val TIMEOUT = 10000 // 10s without any requests will be considered as dead

    @Autowired
    private lateinit var public: Public

    @Scheduled(initialDelay=10000, fixedDelay = 10000)
    fun update() {
        public.activeClients = public.activeClients.filter { entry -> System.currentTimeMillis() - entry.value <= TIMEOUT }.toMutableMap()
    }
}