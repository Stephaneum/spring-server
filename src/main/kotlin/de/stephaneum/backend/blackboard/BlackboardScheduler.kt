package de.stephaneum.backend.blackboard

import de.stephaneum.backend.database.Blackboard
import de.stephaneum.backend.database.BlackboardRepo
import de.stephaneum.backend.database.Type
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service


@Service
class BlackboardScheduler {

    /**
     * this scheduler cycles through the boards
     */

    final val logger = LoggerFactory.getLogger(BlackboardScheduler::class.java)
    final val EMPTY_BLACKBOARD = Blackboard(-1, Type.TEXT, "Leere Konfiguration")
    final val FETCH_DELAY = 10000

    @Autowired
    private lateinit var blackboardRepo: BlackboardRepo

    private var boards = emptyList<Blackboard>()
    private var next = 0L
    private var nextFetch = 0L

    // only this variable should be accessed from outside
    var active = EMPTY_BLACKBOARD

    @Scheduled(initialDelay=10000, fixedDelay = 1000)
    fun update() {

        if(System.currentTimeMillis() > nextFetch) {
            boards = blackboardRepo.findByOrderByOrder()
            nextFetch = System.currentTimeMillis() + FETCH_DELAY
        }

        val activeIndex = boards.indexOf(active)
        if(System.currentTimeMillis() > next || activeIndex == -1 || !boards[activeIndex].visible) {
            // next board
            if(boards.isEmpty() || boards.all { !it.visible }) {
                active = EMPTY_BLACKBOARD
                next = 0 // next tick, triggers next board
                return
            }

            // find next order
            var nextIndex = activeIndex
            do {
                nextIndex++
                if(nextIndex >= boards.size)
                    nextIndex = 0
            } while (!boards[nextIndex].visible)


            active = boards[nextIndex]
            next = System.currentTimeMillis() + (active.duration * 1000)
        }
    }
}