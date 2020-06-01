package de.stephaneum.spring.helper

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

enum class GlobalState {
    INITIALIZING, NEED_INIT, BACKUP, RESTORE, OK
}

@Service
class GlobalStateService {

    private val logger = LoggerFactory.getLogger(GlobalStateService::class.java)

    var state: GlobalState = GlobalState.INITIALIZING
        set(value) {
            logger.info("Change global state: $field -> $value")
            field = value
        }

    val noScheduler: Boolean
        get() {
            return state != GlobalState.OK
        }

    val noVisitCounting: Boolean
        get() {
            return state != GlobalState.OK
        }
}