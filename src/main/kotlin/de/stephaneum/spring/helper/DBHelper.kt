package de.stephaneum.spring.helper

import org.springframework.core.io.FileSystemResource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.init.ScriptUtils
import org.springframework.stereotype.Service
import javax.sql.DataSource

@Service
class DBHelper(
    private val jdbcTemplate: JdbcTemplate,
    private val dataSource: DataSource,
) {
    private var dbLocation: String? = null

    /**
     * @return location of the database on the hard drive
     */
    fun getDatabaseLocation(): String? {
        dbLocation = jdbcTemplate.queryForObject("SELECT @@basedir", String::class.java)
        return dbLocation
    }

    /**
     * @param filePath path to sql script
     * @return exist status
     */
    fun executeSqlScript(filePath: String): String? {
        return try {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0;")
            ScriptUtils.executeSqlScript(dataSource.connection, FileSystemResource(filePath))
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1;")
            null
        } catch (e: Exception) {
            e.message
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