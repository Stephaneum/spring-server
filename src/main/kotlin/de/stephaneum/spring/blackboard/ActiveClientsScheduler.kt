package de.stephaneum.spring.blackboard

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service


@Service
class ActiveClientsScheduler {

    /**
     * this scheduler handles and removes inactive clients from the list
     */

    private val TIMEOUT = 10000 // 10s without any requests will be considered as dead

    var activeClients = mutableMapOf<String, Long>()

    @Scheduled(initialDelay=10000, fixedDelay = 10000)
    fun update() {
        activeClients = activeClients.filter { entry -> System.currentTimeMillis() - entry.value <= TIMEOUT }.toMutableMap()
    }
}