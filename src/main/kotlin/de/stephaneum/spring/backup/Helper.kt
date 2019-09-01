package de.stephaneum.spring.backup

val REDIRECT_LOGIN = "redirect:/backup/login"
val REDIRECT_ADMIN = "redirect:/backup/admin"
val REDIRECT_LOGS = "redirect:/backup/logs"

enum class ModuleType(val display: String, val code: String) {
    HOMEPAGE("Homepage", "homepage"),
    MOODLE("Moodle", "moodle"),
    AR("AR", "ar")
}
val MODULES = listOf(ModuleType.HOMEPAGE, ModuleType.MOODLE, ModuleType.AR)

data class Backup(val name: String, val size: String)
data class Module(val title: String, val backups: List<Backup>, val backupURL: String, val uploadURL: String)
data class Log(val log: String, val running: Boolean)