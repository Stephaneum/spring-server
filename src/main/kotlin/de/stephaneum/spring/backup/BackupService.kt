package de.stephaneum.spring.backup

import de.stephaneum.spring.helper.DBHelper
import de.stephaneum.spring.helper.FileService
import de.stephaneum.spring.helper.cmd
import de.stephaneum.spring.helper.windows
import de.stephaneum.spring.scheduler.ConfigFetcher
import org.apache.commons.io.FileUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class BackupService {

    private val dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")

    @Autowired
    private lateinit var dbHelper: DBHelper

    @Autowired
    private lateinit var fileService: FileService

    @Autowired
    private lateinit var configFetcher: ConfigFetcher

    @Value("\${database}")
    private lateinit var db: String

    @Value("\${spring.datasource.username}")
    private lateinit var dbUser: String

    @Value("\${spring.datasource.password}")
    private lateinit var dbPassword: String

    var running = false

    fun backup(type: ModuleType) {
        if(running)
            return

        running = true
        BackupLogs.clear()
        when(type) {
            ModuleType.HOMEPAGE -> {
                Thread {
                    BackupLogs.addLine("Backup von der Homepage wird gestartet.")
                    val result = homepageBackup()
                    if(result == null) {
                        BackupLogs.addLine("Backup ist fertig! Sie werden in Kürze weitergeleitet.")
                    } else {
                        BackupLogs.addLine("Backup fehlgeschlagen.")
                    }
                    Thread.sleep(3000)
                    running = false
                }.start()
            }
            ModuleType.MOODLE -> TODO()
            ModuleType.AR -> TODO()
        }
    }

    /**
     * @return empty string if it was successful, else then the error message
     */
    private fun homepageBackup(): String? {
        val backupPath = configFetcher.backupLocation ?: return "Backup-Pfad ist leer."
        val filePath = configFetcher.fileLocation ?: return "Datei-Pfad ist leer."
        Thread.sleep(2000)

        val tempPath = "$backupPath/tmp"

        // create temporary folders
        BackupLogs.addLine("[1/5] Temporäre Ordner werden erstellt.");
        val file = File("$tempPath/dateien");
        if (!file.exists() && !file.mkdirs())
            return "Temporäre Ordner (${file.absolutePath} konnte nicht erstellt werden."

        Thread.sleep(2000)

        // database
        BackupLogs.addLine("[2/5] Datenbank wird gesichert.")
        var windowsMySQLPath: String? = null
        var dumpPath = "$tempPath/datenbank.sql"
        if(windows) {
            windowsMySQLPath = dbHelper.getDatabaseLocation()
            dumpPath = dumpPath.replace("/", "\\")
        }
        val result = backupDump(windowsMySQLPath, dbUser, dbPassword, db, dumpPath)
        if(result != null)
            return result

        Thread.sleep(2000)

        // files
        BackupLogs.addLine("[3/5] Dateien werden kopiert.")
        try {
            FileUtils.copyDirectory(File(filePath), file)
        } catch (e: IOException) {
            e.printStackTrace()
            return e.message
        }

        Thread.sleep(2000)

        // zip
        BackupLogs.addLine("[4/5] Zip-Archiv wird erstellt.")
        val time = LocalDateTime.now().format(dateTimeFormat)
        val destination = "$backupPath/homepage/homepage_$time.zip"
        try {
            fileService.zip(tempPath, destination)
        } catch (e: IOException) {
            e.printStackTrace()
            return e.message
        }

        // clear temporary files
        BackupLogs.addLine("[5/5] Temporäre Dateien werden gelöscht.")
        fileService.deleteFolder(File(tempPath), true)
        return null
    }

    private fun backupDump(windowsMySQLPath: String?, username: String, password: String, database: String, destination: String): String? {

        try {
            val file = File(destination)
            file.parentFile.mkdirs()
            var exitStatus = 0
            if(windowsMySQLPath != null) {
                // on windows, dump file path cannot have spaces
                exitStatus = cmd("\"" + windowsMySQLPath + "bin\\mysqldump.exe\" -u\"" + username + "\" -p\"" + password + "\" \"" + database + "\" > " + destination)
            } else {
                exitStatus = cmd("mysqldump -u\"$username\" -p\"$password\" \"$database\" > \"$destination\"")
            }

            return if (exitStatus == 0) {
                null
            } else {
                "Error-Code: $exitStatus"
            }

        } catch (ex: IOException) {
            return ex.message
        } catch (ex: InterruptedException) {
            return ex.message
        }

    }
}