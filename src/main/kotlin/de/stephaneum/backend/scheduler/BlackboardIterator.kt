package de.stephaneum.backend.scheduler

import de.stephaneum.backend.database.Blackboard
import de.stephaneum.backend.database.BlackboardRepo
import de.stephaneum.backend.database.Type
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class BlackboardIterator {

    final val logger = LoggerFactory.getLogger(BlackboardIterator::class.java)
    final val EMPTY_BLACKBOARD = Blackboard(-1, Type.TEXT, "Noch nicht konfiguriert")
    final val FETCH_DELAY = 10000

    @Autowired
    private lateinit var blackboardRepo: BlackboardRepo

    private val boards = mutableListOf<Blackboard>()

    var active = EMPTY_BLACKBOARD
    var next = 0L
    var nextFetch = 0L

    @Scheduled(initialDelay=10000, fixedDelay = 1000)
    fun update() {

        if(System.currentTimeMillis() > nextFetch) {
            boards.clear()
            boards.addAll(blackboardRepo.findByOrderByOrder())
            nextFetch = System.currentTimeMillis() + FETCH_DELAY
        }

        if(System.currentTimeMillis() > next) {
            // next board
            if(boards.isEmpty() || boards.all { !it.visible }) {
                active = EMPTY_BLACKBOARD
                next = 0 // next tick, triggers next board
                return
            }

            // find next order
            var activeIndex = boards.indexOf(active)

            do {
                activeIndex++
                if(activeIndex == boards.size)
                    activeIndex = 0
            } while (!boards[activeIndex].visible)


            active = boards[activeIndex]
            next = System.currentTimeMillis() + (active.duration * 1000)
        }
    }
}