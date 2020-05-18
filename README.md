# Stephaneum Spring

## Voraussetzungen

- Java 8+
- MariaDB oder MySQL
- Node (wird zum kompilieren der Vue-App benötigt)
- IntelliJ CE / UE (alternativ über Konsole kompilieren)

## Projekt starten

1. rechts den Gradle-Reiter öffnen
2. Doppelklick: Tasks > application > bootRun

## Export als .jar-Datei

1. rechts den Gradle-Reiter öffnen
2. Doppelklick: Tasks > build > assemble
3. die Datei liegt nun in <Projekt-Ordner>/build/libs/

## Migration von JSF nach Spring

Die Migration fand zwischen 25.05.2019 - 17.05.2020 statt und ist nun abgeschlossen.
Das JSF-Projekt wird nicht mehr verwendet.
- Blackboard (Mai 2019)
- Backup System (August 2019)
- Beitrag-Manager (Oktober 2019)
- Cloud (Februar 2020)
- Gruppen (März 2020)
- restliche Seiten (Mai 2020)

## Routen (GET)

Hier werden Adressen (Routen) aufgeführt, wo der Server mit einer HTML-Datei antwortet, oder mit bestimmten Dateien.

### Homepage
Route|Info|Bereich
---|---|---
`/`|Homepage|öffentlich
`/m/:id`|Homepage (bestimmtes Menü)|
`/beitrag/:id`|Beitrag|
`/login`|Login|
`/statistiken`|Statistiken|
`/termine`|Termine|
`/geschichte`|Geschichte|
`/eu-sa`|Europa Förderung|
`/kontakt`|Kontakt|
`/impressum`|Impressum|
`/sitemap`|Sitemap|
`/s/*`|statische Seiten|
`/home`|Homepage (eingeloggt)|intern
`/user-manager`|Nutzerverwaltung|
`/config-manager`|Konfiguration|
`/static-manager`|Seiten|
`/code-manager`|Zugangscodes|
`/logs`|Logdaten|
`/plan-manager`|Vertretungsplan|
`/menu-manager`|Menü|
`/post-manager`|Beiträge|
`/groups`|Gruppen|
`/groups/:id`|eine bestimmte Gruppe|
`/cloud`|Cloud|
`/account`|Accounteinstellungen|
`/vertretungsplan.pdf`|Vertretungsplan|Dateien
`/files/public/:file`|öffentliche Datei|
`/files/slider/:id`|Diashow|
`/files/images/:file`|Bild in Beiträgen|
`/files/internal/:id`|Dateien aus der Cloud|


### Blackboard
Route|Info
---|---
`/blackboard`|Diese Seite wird vom Blackboard aufgerufen
`/blackboard/admin`|Administration des Blackboards
`/blackboard/login`|Blackboard-Login

### Backup
Route|Info
---|---
`/backup`|Weiterleitung nach `/backup/login` bzw. `/backup/admin`
`/backup/admin`|Administration der Backups
`/backup/login`|Backup-Login
`/backup/logs`|Konsolenausgaben (der letzten Aktion)