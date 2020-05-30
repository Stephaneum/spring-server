package de.stephaneum.spring.backup

import de.stephaneum.spring.helper.DBHelper
import de.stephaneum.spring.helper.cmd
import de.stephaneum.spring.helper.windows
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException

@Service
class DumpService (
        private val dbHelper: DBHelper
) {

    /**
     * creates a dump.sql file
     * @param username db user
     * @param password db password
     * @param database the database which will be dumped
     * @param destination the location where to store the file
     * @return null if it was successful
     */
    fun dumpToFile(username: String, password: String, database: String, destination: String): String? {

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
    fun loadFromFile(username: String, password: String, database: String, source: String): String? {

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