package de.stephaneum.spring.backup

const val REDIRECT_LOGIN = "redirect:/backup/login"
const val REDIRECT_ADMIN = "redirect:/backup/admin"

enum class ModuleType(val display: String, val code: String) {
    HOMEPAGE("Homepage", "homepage"),
    MOODLE("Moodle", "moodle"),
    AR("AR", "ar")
}
val MODULES = listOf(ModuleType.HOMEPAGE, ModuleType.MOODLE, ModuleType.AR)

data class Backup(val name: String, val size: String)
data class Module(val title: String, val code: String, val backups: List<Backup>, val passwordNeeded: Boolean)
data class LogData(val logs: String, val running: Boolean, val error: Boolean)

object Request {
    data class Password(val password: String?)
}

object Response {
    data class AdminData(val modules: List<Module>, val backupLocation: String, val totalSize: String, val nextBackup: String)
}