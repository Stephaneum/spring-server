package de.stephaneum.spring.helper

import de.stephaneum.spring.database.StatsCloud
import de.stephaneum.spring.database.StatsCloudRepo
import de.stephaneum.spring.database.now
import org.springframework.stereotype.Service
import java.time.LocalDate

data class CloudCount(val date: LocalDate, val size: Long)

@Service
class CloudStatsService (
        private val statsCloudRepo: StatsCloudRepo
) {

    private var stats: List<CloudCount>? = null

    fun add(bytes: Int) {
        statsCloudRepo.save(StatsCloud(0, now(), bytes))
        stats = null // trigger generateStats() on next get call
    }

    fun getStats(): List<CloudCount> {
        val nextStats = stats ?: generateStats()
        stats = nextStats
        return nextStats
    }

    private fun generateStats(): List<CloudCount> {
        val raw = statsCloudRepo.findAll().toList()

        if(raw.isEmpty())
            return emptyList()

        val result = mutableListOf<CloudCount>()
        var date = raw.first().timestamp.toLocalDateTime().toLocalDate()
        var size: Long = raw.first().size.toLong()
        val converter = 1024 * 1024 // bytes to mega bytes
        raw.drop(1).forEach { curr ->
            val currDate = curr.timestamp.toLocalDateTime().toLocalDate()
            if (currDate != date) {
                result.add(CloudCount(date, size / converter))
                date = currDate
            }
            size += curr.size
        }

        return result
    }

}