package de.stephaneum.spring.backup

val REDIRECT_LOGIN = "redirect:/backup/login"
val REDIRECT_ADMIN = "redirect:/backup/admin"

val MODULE_HOMEPAGE = "Homepage"
val MODULE_MOODLE = "Moodle"
val MOODLE_AR = "AR"
val MODULES = listOf(MODULE_HOMEPAGE, MODULE_MOODLE, MOODLE_AR)

data class Backup(val name: String, val size: String)
data class Module(val title: String, val backups: List<Backup>, val backupURL: String, val uploadURL: String)