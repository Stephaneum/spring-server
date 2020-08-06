package de.stephaneum.spring.helper

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class DBHelper {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    private var dbLocation: String? = null

    /**
     * @return location of the database on the hard drive
     */
    fun getDatabaseLocation(): String? {
        dbLocation = jdbcTemplate.queryForObject("SELECT @@basedir", String::class.java)
        return dbLocation
    }

    /**
     * @param user the db user
     * @param password the password of db user
     * @return exist status
     */
    fun sql(user: String, password: String, query: String): Int {
        if(windows) {
            if(dbLocation == null)
                getDatabaseLocation()

            dbLocation?.let { location ->
                return cmd(""""${location}bin\mysqldump.exe" -u"$user" -p"$password" -e "$query"""")
            }
            return -1
        } else {
            return cmd("""mysqldump -u"$user" -p"$password" -e "$query"""")
        }
    }

    /**
     * s4 specific
     * @param oldPath the current path
     * @param newPath new path which will be set, NO TRAILING SLASH, ONLY FORWARD SLASH
     */
    fun updateFilePath(oldPath: String, newPath: String) {
        val newPathNormalized = newPath.replace("\\", "/")
        jdbcTemplate.execute("""UPDATE `config` SET `value` = "$newPathNormalized" WHERE `key` = "speicherort"""")
        jdbcTemplate.execute("""UPDATE `config` SET `value` = REPLACE(`value`, "$oldPath", "$newPathNormalized") WHERE `key` = "str_vertretung" """)
        jdbcTemplate.execute("""UPDATE `file` SET `path` = REPLACE(`path`, "$oldPath", "$newPathNormalized")""")
        jdbcTemplate.execute("""UPDATE `slider` SET `path` = REPLACE(path, "$oldPath", "$newPathNormalized")""")
    }

    /**
     * s4 specific
     * @param newPath new path which will be set, NO TRAILING SLASH, ONLY FORWARD SLASH
     */
    fun updateBackupPath(newPath: String) {
        jdbcTemplate.execute("""UPDATE `config` SET `value` = "$newPath" WHERE `key` = "backup_dir"""")
    }
}