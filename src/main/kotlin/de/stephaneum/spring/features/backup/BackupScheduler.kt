package de.stephaneum.spring.features.backup

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.*

@Service
class BackupScheduler {

    /**
     * this scheduler triggers a weekly backup
     */

    private val logger = LoggerFactory.getLogger(BackupScheduler::class.java)

    private val dateFull = DateTimeFormatter.ofPattern("EEEE, dd.MMMM yyyy", Locale.GERMANY)
    private val BACKUP_HOUR = 4
    private val BACKUP_MINUTE = 30
    private val BACKUP_DAY = DayOfWeek.SATURDAY
    private val MIN_INTERVAL = 30*60*1000 // 30min

    private var lastBackup = 0L

    @Autowired
    private lateinit var backupService: BackupService

    @Scheduled(initialDelay=5000, fixedDelay = 60*1000) // check every minute
    fun update() {
        val now = LocalDateTime.now()
        if(now.dayOfWeek == BACKUP_DAY && now.hour == BACKUP_HOUR && (now.minute == BACKUP_MINUTE || now.minute == BACKUP_MINUTE+1) && System.currentTimeMillis() - lastBackup >= MIN_INTERVAL) {
            logger.info("It is now BACKUP TIME!")
            backupService.backupFull()
        }
    }

    fun getNextBackup(): String {
        val now = LocalDateTime.now()

        var sameDay = false
        if (now.hour < BACKUP_HOUR)
            sameDay = true
        else if (now.hour == BACKUP_HOUR && now.minute < BACKUP_MINUTE)
            sameDay = true

        val nextDay: LocalDateTime
        if (sameDay)
            nextDay = now.with(TemporalAdjusters.nextOrSame(BACKUP_DAY))
        else
            nextDay = now.with(TemporalAdjusters.next(BACKUP_DAY))

        return dateFull.format(nextDay) + ", um " + BACKUP_HOUR + ":" + BACKUP_MINUTE + " Uhr"
    }
}