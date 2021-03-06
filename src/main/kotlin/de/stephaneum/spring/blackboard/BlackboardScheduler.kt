package de.stephaneum.spring.blackboard

import de.stephaneum.spring.database.Blackboard
import de.stephaneum.spring.database.BlackboardRepo
import de.stephaneum.spring.database.Type
import de.stephaneum.spring.database.now
import de.stephaneum.spring.helper.GlobalStateService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service


@Service
class BlackboardScheduler {

    /**
     * this scheduler cycles through the boards
     * every 10min a refresh is forced by updating all the timestamps
     */

    private val EMPTY_BLACKBOARD = Blackboard(-1, Type.TEXT, "Leere Konfiguration")
    private val FETCH_DELAY = 10000 // sync every 10s with the database
    private val REFRESH_DELAY = 10*60*1000 // force refresh every 10min

    @Autowired
    private lateinit var blackboardRepo: BlackboardRepo

    @Autowired
    private lateinit var globalStateService: GlobalStateService

    private var boards = emptyList<Blackboard>()
    private var nextBoard = 0L // the time of the next board
    private var nextFetch = 0L // the time to sync with the database
    private var nextRefresh = System.currentTimeMillis() // the time to force a refresh

    // only these variables should be accessed from outside
    var active = EMPTY_BLACKBOARD
    var activeSince = System.currentTimeMillis()
    var paused = false

    @Scheduled(initialDelay=10000, fixedDelay = 1000)
    fun update() {

        if(globalStateService.noScheduler)
            return

        if(paused)
            return

        if(System.currentTimeMillis() > nextFetch) {
            // fetch new data from database
            boards = blackboardRepo.findByOrderByOrder()
            nextFetch = System.currentTimeMillis() + FETCH_DELAY
        }

        // cycle to the next board
        val activeIndex = boards.indexOf(active)
        if(System.currentTimeMillis() > nextBoard || activeIndex == -1 || !boards[activeIndex].visible) {
            // next board
            if(boards.isEmpty() || boards.all { !it.visible }) {
                active = EMPTY_BLACKBOARD
                nextBoard = 0 // next tick, triggers next board
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
            activeSince = System.currentTimeMillis()
            nextBoard = System.currentTimeMillis() + (active.duration * 1000)
        }

        // force refresh
        if(System.currentTimeMillis() > nextRefresh) {
            boards = blackboardRepo.saveAll(updateTimestamps(blackboardRepo.findAll())).toList() // update db and cache
            nextRefresh = System.currentTimeMillis() + REFRESH_DELAY
        }
    }

    fun timeToNextRefreshSec() = (nextRefresh - System.currentTimeMillis()) / 1000

    private fun updateTimestamps(boards: Iterable<Blackboard>): Iterable<Blackboard> {
        return boards.mapIndexed{ index, board ->
            board.lastUpdate = now(index*2000) // we are adding a delta to the timestamp to have unique timestamps
            board
        }
    }
}