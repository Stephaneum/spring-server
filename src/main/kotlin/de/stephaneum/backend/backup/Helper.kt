package de.stephaneum.backend.backup

val REDIRECT_LOGIN = "redirect:/backup/login"
val REDIRECT_ADMIN = "redirect:/backup/admin"

data class Backup(val name: String, val size: String)
data class Section(val title: String, val backups: List<Backup>, val backupURL: String, val uploadURL: String)