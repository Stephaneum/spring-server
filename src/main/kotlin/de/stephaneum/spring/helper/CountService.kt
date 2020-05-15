package de.stephaneum.spring.helper

import de.stephaneum.spring.database.StatsDay
import de.stephaneum.spring.database.StatsDayRepo
import de.stephaneum.spring.database.StatsHourRepo
import de.stephaneum.spring.scheduler.ConfigScheduler
import de.stephaneum.spring.scheduler.Element
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class CountService (
        private val configScheduler: ConfigScheduler,
        private val userAgentDetector: UserAgentDetector,
        private val statsHourRepo: StatsHourRepo,
        private val statsDayRepo: StatsDayRepo
) {

    private val logger = LoggerFactory.getLogger(CountService::class.java)
    private val logFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss")
    private val germanTimezone = ZoneId.of("Europe/Berlin")
    private var lastIP: String? = null

    // non-persistent stats
    private val statsBrowser = mutableMapOf<Browser, Int>()
    private val statsOS = mutableMapOf<OS, Int>()
    private val history = arrayOfNulls<String>(100)

    /**
     * userAgent must be in lowercase beforehand
     */
    fun count(ip: String, userAgent: String) {

        if (lastIP == ip) {
            logger.info("same ip: $ip ($userAgent)")
            return
        }

        val now = LocalDateTime.now(germanTimezone)

        if (userAgentDetector.isBot(userAgent)) {
            logBot(now, ip, userAgent)
            return
        }

        countHour(now)
        countDay(now)
        val browser = countBrowser(userAgent)
        val os = countOS(userAgent)

        log(now, ip, userAgent, os, browser)
        lastIP = ip
    }

    fun getStatsBrowser(): Map<Browser, Int> {
        return statsBrowser
    }

    fun getStatsOS(): Map<OS, Int> {
        return statsOS
    }

    fun getHistory(): Array<String?> {
        return history
    }

    private fun countHour(now: LocalDateTime) {
        val hourIndex = configScheduler.get(Element.indexStatsHour)?.toIntOrNull() ?: throw ErrorCode(500, "no hour index")
        val stats = statsHourRepo.findByIdOrNull(hourIndex) ?: throw ErrorCode(500, "no row with index $hourIndex")
        stats.hour = now.hour
        statsHourRepo.save(stats)

        // update index
        val nextHourIndex = (hourIndex+1) % 20000
        configScheduler.save(Element.indexStatsHour, nextHourIndex.toString())
    }

    private fun countDay(now: LocalDateTime) {
        val date = now.toLocalDate()
        val stats = statsDayRepo.findByDate(date) ?: StatsDay(0, date, 0)
        stats.count++
        statsDayRepo.save(stats)
    }

    private fun countBrowser(userAgent: String): Browser {
        val browser = userAgentDetector.getBrowser(userAgent)
        statsBrowser[browser] = (statsBrowser[browser] ?: 0) + 1
        return browser
    }

    private fun countOS(userAgent: String): OS {
        val os = userAgentDetector.getOS(userAgent)
        statsOS[os] = (statsOS[os] ?: 0) + 1
        return os
    }

    private fun logBot(now: LocalDateTime, ip: String, userAgent: String) {
        shiftHistory()
        history[0] = "${logFormat.format(now)} [IP / $ip] [ ~ BOT ~ ] --- $userAgent"
    }

    private fun log(now: LocalDateTime, ip: String, userAgent: String, os: OS, browser: Browser) {
        shiftHistory()
        history[0] = "${logFormat.format(now)} [IP / $ip] [ ${os.repr} / ${browser.repr} ] --- $userAgent"
    }

    private fun shiftHistory() {
        history.forEachIndexed { index, _ ->
            if(index < history.size-1)
                history[index+1] = history[index]
        }
    }
}