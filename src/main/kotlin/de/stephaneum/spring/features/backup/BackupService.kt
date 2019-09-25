package de.stephaneum.spring.features.backup

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
    var error = false
    var sudoPassword: String? = null
        set(value) {
            val result = cmd("echo test", sudoPassword = value)
            if(result == 0)
                field = value // only update password if success
        }

    fun backupFull() {
        running = true
        error = false
        BackupLogs.clear()
        Thread {
            BackupLogs.addLine("Vollständiges Backup gestartet.")
            BackupLogs.addLine("(A) Homepage")
            homepageBackup()
            Thread.sleep(3000)
            if(sudoPassword != null) {
                BackupLogs.addLine("(B) Homepage")
                moodleBackup()
            } else {
                BackupLogs.addLine("(B) Homepage wird übersprungen, weil sudo-Passwort nicht mitgeteilt wurde.")
            }
            Thread.sleep(3000)
            BackupLogs.addLine("(C) AR")
            arBackup()
            Thread.sleep(3000)
            running = false
        }.start()
    }

    fun backup(type: ModuleType) {
        if(running)
            return

        running = true
        error = false
        BackupLogs.clear()
        Thread {

            val result = when(type) {
                ModuleType.HOMEPAGE -> {
                    BackupLogs.addLine("Backup von der Homepage gestartet.")
                    homepageBackup()
                }
                ModuleType.MOODLE -> {
                    BackupLogs.addLine("Backup von Moodle gestartet.")
                    moodleBackup()
                }
                ModuleType.AR -> {
                    BackupLogs.addLine("Backup von AR gestartet.")
                    arBackup()
                }
            }

            if(result == null) {
                BackupLogs.addLine("Backup ist fertig! Sie werden in Kürze weitergeleitet.")
            } else {
                BackupLogs.addLine(result)
                BackupLogs.addLine("Backup fehlgeschlagen.")
                error = true
            }
            running = false
        }.start()

    }

    fun restore(type: ModuleType, file: String) {
        if(running)
            return

        running = true
        error = false
        BackupLogs.clear()
        Thread {
            val filePath = "${configFetcher.backupLocation}/${type.code}/$file"
            val result = when(type) {
                ModuleType.HOMEPAGE -> {
                    BackupLogs.addLine("Homepage wird wiederhergestellt. ($file)")
                    homepageRestore(filePath, configFetcher.backupLocation!!, configFetcher.fileLocation!!)
                }
                ModuleType.MOODLE -> {
                    BackupLogs.addLine("Moodle wird wiederhergestellt. ($file)")
                    moodleRestore(filePath)
                }
                ModuleType.AR -> {
                    BackupLogs.addLine("AR wird wiederhergestellt. ($file)")
                    arRestore(filePath)
                }
            }

            if(result == null) {
                BackupLogs.addLine("Wiederherstellung ist fertig! Sie werden in Kürze weitergeleitet.")
            } else {
                BackupLogs.addLine(result)
                BackupLogs.addLine("Wiederherstellung fehlgeschlagen.")
                error = true
            }
            running = false
        }.start()
    }

    /**
     * @return empty string if it was successful, non-empty else when an error occurs
     */
    private fun homepageBackup(): String? {
        val backupPath = configFetcher.backupLocation ?: return "Backup-Pfad ist leer."
        val filePath = configFetcher.fileLocation ?: return "Datei-Pfad ist leer."
        Thread.sleep(2000)

        val tempPath = "$backupPath/tmp"

        // create temporary folders
        BackupLogs.addLine("[1/5] Temporäre Ordner werden erstellt.")
        val file = File("$tempPath/dateien")
        if (!file.exists() && !file.mkdirs())
            return "Temporäre Ordner (${file.absolutePath} konnte nicht erstellt werden."

        Thread.sleep(2000)

        // database
        BackupLogs.addLine("[2/5] Datenbank wird gesichert.")
        var dumpPath = "$tempPath/datenbank.sql"
        if(windows) {
            dumpPath = dumpPath.replace("/", "\\")
        }
        val result = backupDump(dbUser, dbPassword, db, dumpPath)
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

    private fun homepageRestore(zipFile: String, backupPath: String, filePath: String): String? {
        Thread.sleep(2000)

        val tempPath = "$backupPath/tmp"

        // create temporary folders
        BackupLogs.addLine("[1/7] Temporäre Ordner werden erstellt.")
        val file = File("$tempPath/dateien")
        if (!file.exists() && !file.mkdirs())
            return "Temporäre Ordner (${file.absolutePath} konnte nicht erstellt werden."

        Thread.sleep(2000)

        // extract zip
        BackupLogs.addLine("[2/7] Zip-Archiv wird extrahiert.")
        try {
            fileService.unzip(zipFile, tempPath)
        } catch (e: IOException) {
            e.printStackTrace()
            return e.message
        }

        Thread.sleep(1000)

        // clear old files
        BackupLogs.addLine("[3/7] Alter Speicher wird gelöscht.")
        fileService.deleteFolder(File(filePath), false)

        Thread.sleep(1000)

        // copy files
        BackupLogs.addLine("[4/7] Neue Dateien werden kopiert.")
        try {
            FileUtils.copyDirectory(file, File(filePath))
        } catch (e: IOException) {
            e.printStackTrace()
            return e.message
        }

        Thread.sleep(1000)

        // load db
        BackupLogs.addLine("[5/7] Datenbank wird geladen und ggf. aktualisiert.")
        var dumpPath = tempPath+"/datenbank.sql"
        if(windows) {
            dumpPath = dumpPath.replace("/", "\\")
        }

        val result = restoreDump(dbUser, dbPassword, db, dumpPath)
        if(result != null)
            return result
        else
            configFetcher.update()

        Thread.sleep(1000)

        val oldFilePath = configFetcher.fileLocation!!
        val oldBackupPath = configFetcher.backupLocation!!

        // adjust paths
        BackupLogs.addLine("[6/7] Speicherort und Backup-Ordner werden gesetzt.")
        BackupLogs.addLine("-> Speicherort: $oldFilePath -> $filePath", 1)
        BackupLogs.addLine("-> Cloudspeicher", 2)
        BackupLogs.addLine("-> Vertretungsplan", 2)
        BackupLogs.addLine("-> Diashow", 2)
        dbHelper.updateFilePath(oldFilePath, filePath)
        BackupLogs.addLine("-> Backup-Ordner: $oldBackupPath -> $backupPath", 1)
        dbHelper.updateBackupPath(backupPath)

        Thread.sleep(1000)

        // clear temporary files
        BackupLogs.addLine("[7/7] Temporäre Dateien werden gelöscht.")
        fileService.deleteFolder(File(tempPath), true)
        return null
    }

    private fun moodleBackup(): String? {

        if(windows)
            return "Moodle wird auf Windows nicht unterstützt."

        sudoPassword?.let { password ->
            val backupPath = configFetcher.backupLocation ?: return "Backup-Pfad ist leer."
            val tempPath = "$backupPath/tmp"
            val moodlePath = File("$tempPath/moodle")
            val moodleDataPath = File("$tempPath/moodledata")

            // create temporary folders
            BackupLogs.addLine("[1/7] Temporäre Ordner werden erstellt.")

            if (!moodlePath.exists() && !moodlePath.mkdirs())
                return "Temporäre Ordner (${moodlePath.absolutePath} konnte nicht erstellt werden."
            if (!moodleDataPath.exists() && !moodleDataPath.mkdirs())
                return "Temporäre Ordner (${moodleDataPath.absolutePath} konnte nicht erstellt werden."

            Thread.sleep(2000)

            // database
            BackupLogs.addLine("[2/7] Datenbank wird gesichert.")
            var dumpPath = "$tempPath/moodle.sql"
            val result = backupDump(dbUser, dbPassword, "moodle", dumpPath)
            if(result != null)
                return result

            Thread.sleep(2000)

            // files (moodle)
            BackupLogs.addLine("[3/7] Moodle-Ordner wird kopiert.")
            var exitStatus = cmd("cp -R /var/www/html/moodle $tempPath", sudoPassword = password)
            if(exitStatus != 0)
                return "Exit-Status: $exitStatus"

            Thread.sleep(2000)

            // files (moodledata)
            BackupLogs.addLine("[4/7] Moodledata-Ordner wird kopiert.")
            exitStatus = cmd("cp -R /var/www/html/moodledata $tempPath", sudoPassword = password)
            if(exitStatus != 0)
                return "Exit-Status: $exitStatus"

            Thread.sleep(2000)

            // chmod
            BackupLogs.addLine("[5/7] Rechtevergabe wird konfiguriert.")
            exitStatus = cmd("chmod -R 777 $tempPath", sudoPassword = password)
            if(exitStatus != 0)
                return "Exit-Status: $exitStatus"

            Thread.sleep(2000)

            // zip
            BackupLogs.addLine("[6/7] Zip-Archiv wird erstellt.")
            val time = LocalDateTime.now().format(dateTimeFormat)
            val destination = "$backupPath/moodle/moodle_$time.zip"
            try {
                fileService.zip(tempPath, destination)
            } catch (e: IOException) {
                e.printStackTrace()
                return e.message
            }

            Thread.sleep(1000)

            // clear temporary files
            BackupLogs.addLine("[7/7] Temporäre Dateien werden gelöscht.")
            exitStatus = cmd("rm -rf $tempPath", sudoPassword = password)
            if(exitStatus != 0)
                return "Exit-Status: $exitStatus"
            return null
        }

        return "sudo-Passwort unbekannt"
    }

    private fun moodleRestore(zipFile: String): String? {
        if(windows)
            return "Moodle wird auf Windows nicht unterstützt."

        sudoPassword?.let { password ->
            val backupPath = configFetcher.backupLocation ?: return "Backup-Pfad ist leer."
            val tempPath = "$backupPath/tmp"

            // create temporary folders
            BackupLogs.addLine("[1/9] Temporäre Ordner werden erstellt.")
            val file = File(tempPath)
            if (!file.exists() && !file.mkdirs())
                return "Temporäre Ordner (${file.absolutePath} konnte nicht erstellt werden."

            Thread.sleep(2000)

            // extract zip
            BackupLogs.addLine("[2/9] Zip-Archiv wird extrahiert.")
            try {
                fileService.unzip(zipFile, tempPath)
            } catch (e: IOException) {
                e.printStackTrace()
                return e.message
            }

            Thread.sleep(1000)

            // clear old files (moodle)
            BackupLogs.addLine("[3/9] Alter Moodle-Ordner wird gelöscht.")
            var exitStatus = cmd("rm -rf /var/www/html/moodle", sudoPassword = password)
            if(exitStatus != 0)
                return "Exit-Status: $exitStatus"

            Thread.sleep(1000)

            // clear old files (moodledata)
            BackupLogs.addLine("[4/9] Alter Moodledata-Ordner wird gelöscht.")
            exitStatus = cmd("rm -rf /var/www/html/moodledata", sudoPassword = password)
            if(exitStatus != 0)
                return "Exit-Status: $exitStatus"

            Thread.sleep(1000)

            // copy files (moodle)
            BackupLogs.addLine("[5/9] Moodle-Ordner wird kopiert.")
            exitStatus = cmd("cp -R $tempPath/moodle /var/www/html", sudoPassword = password)
            if(exitStatus != 0)
                return "Exit-Status: $exitStatus"

            Thread.sleep(1000)

            // copy files (moodledata)
            BackupLogs.addLine("[6/9] Moodledata-Ordner wird kopiert.")
            exitStatus = cmd("cp -R $tempPath/moodledata /var/www/html", sudoPassword = password)
            if(exitStatus != 0)
                return "Exit-Status: $exitStatus"

            Thread.sleep(1000)

            // chmod
            BackupLogs.addLine("[7/9] Rechtevergabe wird konfiguriert.")
            exitStatus = cmd("chmod -R 777 /var/www/html/moodle", sudoPassword = password)
            if(exitStatus != 0)
                return "Exit-Status: $exitStatus"

            exitStatus = cmd("chmod -R 777 /var/www/html/moodledata", sudoPassword = password)
            if(exitStatus != 0)
                return "Exit-Status: $exitStatus"

            Thread.sleep(1000)

            // load db
            BackupLogs.addLine("[8/9] Datenbank wird geladen.")
            var dumpPath = "$tempPath/moodle.sql"
            if(windows) {
                dumpPath = dumpPath.replace("/", "\\")
            }

            val result = restoreDump(dbUser, dbPassword, "moodle", dumpPath)
            if(result != null)
                return result
            else
                configFetcher.update()

            Thread.sleep(1000)

            // clear temporary files
            BackupLogs.addLine("[9/9] Temporäre Dateien werden gelöscht.")
            fileService.deleteFolder(File(tempPath), true)
            return null
        }
        return "sudo-Passwort unbekannt"
    }

    fun arBackup(): String? {
        val backupPath = configFetcher.backupLocation ?: return "Backup-Pfad ist leer."
        BackupLogs.addLine("[1/1] Datenbank wird gesichert.")
        val time = LocalDateTime.now().format(dateTimeFormat)
        var destination = "$backupPath/ar/ar_$time.sql"
        if(windows) {
            destination = destination.replace("/", "\\")
        }
        val result = backupDump(dbUser, dbPassword, "ar", destination)
        if(result != null)
            return result

        Thread.sleep(2000)
        return null
    }

    fun arRestore(sqlFile: String): String? {
        BackupLogs.addLine("[1/1] Datenbank wird geladen.")
        var dump = sqlFile
        if(windows) {
            dump = dump.replace("/", "\\")
        }
        val result = restoreDump(dbUser, dbPassword, "ar", dump)
        if(result != null)
            return result

        Thread.sleep(2000)
        return null
    }

    /**
     * creates a dump.sql file
     * @param username db user
     * @param password db password
     * @param database the database which will be dumped
     * @param destination the location where to store the file
     * @return null if it was successful
     */
    private fun backupDump(username: String, password: String, database: String, destination: String): String? {

        try {
            val file = File(destination)
            file.parentFile.mkdirs()
            val exitStatus = if(windows) {
                // on windows, dump file path cannot have spaces
                cmd(""""${dbHelper.getDatabaseLocation()}bin\mysqldump.exe" -u"$username" -p"$password" "$database" > $destination""")
            } else {
                cmd("""mysqldump -u"$username" -p"$password" "$database" > "$destination"""")
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

    /**
     * loads the dump.sql into the database
     * @param username db user
     * @param password db password
     * @param database the database which will be dumped
     * @param source the location where the file currently is
     * @return null if it was successful
     */
    private fun restoreDump(username: String, password: String, database: String, source: String): String? {

        try {
            val exitStatus = if(windows) {
                // on windows, dump file path cannot have spaces
                cmd(""""${dbHelper.getDatabaseLocation()}bin\mysql.exe" -u"$username" -p"$password" "$database" < $source""")
            } else {
                cmd("""mysql -u"$username" -p"$password" "$database" < "$source"""")
            }

            return if (exitStatus == 0) {
                null
            } else {
                "Error-Status: $exitStatus"
            }

        } catch (ex: IOException) {
            return ex.message
        } catch (ex: InterruptedException) {
            return ex.message
        }

    }
}