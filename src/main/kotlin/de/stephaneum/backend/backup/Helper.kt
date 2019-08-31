package de.stephaneum.backend.backup

val REDIRECT_LOGIN = "redirect:/backup/login"
val REDIRECT_ADMIN = "redirect:/backup/admin"

val MODULES = listOf("Homepage", "Moodle", "AR")

data class Backup(val name: String, val size: String)
data class Module(val title: String, val backups: List<Backup>, val backupURL: String, val uploadURL: String)