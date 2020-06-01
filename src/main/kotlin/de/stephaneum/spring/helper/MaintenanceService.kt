package de.stephaneum.spring.helper

import de.stephaneum.spring.scheduler.ConfigScheduler
import org.springframework.stereotype.Service

@Service
class MaintenanceService (
        private val configScheduler: ConfigScheduler
) {

    val noScheduler: Boolean
        get() {
            return !configScheduler.initialized || configScheduler.needInit
        }
}