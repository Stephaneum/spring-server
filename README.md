# Stephaneum Spring

## Voraussetzungen

- Java 8+
- MySQL
- Node (wird zum kompilieren der Vue-App benötigt)
- IntelliJ (alternativ über Konsole kompilieren)

## Projekt starten

1. rechts den Gradle-Reiter öffnen
2. Doppelklick: Tasks > application > bootRun

## Export als .jar-Datei

1. Vue App kompilieren
    1. `cd Projekt-Order/src/main/vue` (in Vue-Ordner gelangen)
    2. `npm i` (Bibliotheken herunterladen)
    3. `npm run build` (Kompilieren)
    4. das Ergebnis liegt nun in `Projekt-Ordner/src/main/vue/dist`
2. Spring App kompilieren
    1. `cd Projekt-Order`
        1. `gradlew.bat assemble` (Windows)
        2. `./gradlew assemble` (Linux / Mac OS)
        3. rechts den Gradle-Reiter öffnen > Tasks > build > assemble
    2. die Datei liegt nun in `Projekt-Ordner/build/libs/`

## Migration von JSF nach Spring

Die Migration fand zwischen 25.05.2019 - 17.05.2020 statt und ist nun abgeschlossen.
Das JSF-Projekt wird nicht mehr verwendet.
- Blackboard (Mai 2019) (hier wurde das Spring-Projekt initialisiert)
- Backup System (August 2019)
- Beitrag-Manager (Oktober 2019)
- Cloud (Februar 2020)
- Gruppen (März 2020)
- restliche Seiten (Mai 2020)

Der Server kann nun ohne Tomcat gestartet werden und die Entwicklung neuer Funktionen ist wesentlicher einfacher und schneller.

Die Code-Basis konnte von 35.000 Zeilen (25.000 Java + 10.000 XHTML) auf 17.000 Zeilen (8.000 Kotlin + 9.000 Vue) reduziert werden (Stand Mai 2020), obwohl das Spring-Projekt mehr Funktionen (z.B. Blackboard und Backup) enthält.

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