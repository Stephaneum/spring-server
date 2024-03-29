# Stephaneum Spring

## Voraussetzungen

- Java 8+
- MySQL 5
- Node 12 bis 16 (wird zum Kompilieren der Vue-App benötigt)
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

Die Migration fand zwischen 25.05.2019 - 17.05.2020 statt.

Das JSF-Projekt wird nicht mehr verwendet.

## Routen (GET)

Hier werden Adressen (Routen) aufgeführt, wo der Server mit einer HTML-Datei antwortet, oder mit bestimmten Dateien.

### Homepage
| Route                  | Info                        | Bereich                 |
|------------------------|-----------------------------|-------------------------|
| `/`                    | Homepage                    | öffentlich              |
| `/m/:id/:page`         | Homepage (bestimmtes Menü)  | öffentlich              |
| `/beitrag/:id`         | Beitrag                     | öffentlich              |
| `/login`               | Login                       | öffentlich              |
| `/statistiken`         | Statistiken                 | öffentlich              |
| `/termine`             | Termine                     | öffentlich              |
| `/geschichte`          | Geschichte                  | öffentlich              |
| `/eu-sa`               | Europa Förderung            | öffentlich              |
| `/kontakt`             | Kontakt                     | öffentlich              |
| `/impressum`           | Impressum                   | öffentlich              |
| `/sitemap`             | Sitemap                     | öffentlich              |
| `/s/*`                 | statische Seiten            | öffentlich              |
| `/home`                | Homepage (eingeloggt)       | intern                  |
| `/user-manager`        | Nutzerverwaltung            | intern                  |
| `/config-manager`      | Konfiguration               | intern                  |
| `/static-manager`      | Seiten                      | intern                  |
| `/code-manager`        | Zugangscodes                | intern                  |
| `/logs`                | Logdaten                    | intern                  |
| `/plan-manager`        | Vertretungsplan             | intern                  |
| `/menu-manager`        | Menü                        | intern                  |
| `/post-manager`        | Beiträge                    | intern                  |
| `/groups`              | Gruppen                     | intern                  |
| `/groups/:id`          | eine bestimmte Gruppe       | intern                  |
| `/cloud`               | Cloud                       | intern                  |
| `/account`             | Accounteinstellungen        | intern                  |
| `/vertretungsplan.pdf` | Vertretungsplan             | Dateien                 |
| `/files/public/:file`  | öffentliche Datei           | Dateien                 |
| `/files/slider/:id`    | Diashow                     | Dateien                 |
| `/files/images/:file`  | Bild in Beiträgen           | Dateien                 |
| `/files/internal/:id`  | Dateien aus der Cloud       | Dateien                 |
| `/init`                | Erste Einrichtung           | nicht direkt erreichbar |
| `/status/restoring`    | Infoseite: Backup laden     | nicht direkt erreichbar |
| `/status/backup`       | Infoseite: Backup erstellen | nicht direkt erreichbar |

### Blackboard
| Route               | Info                                       |
|---------------------|--------------------------------------------|
| `/blackboard`       | Diese Seite wird vom Blackboard aufgerufen |
| `/blackboard/admin` | Administration des Blackboards             |
| `/blackboard/login` | Blackboard-Login                           |

### Backup
| Route           | Info                                                    |
|-----------------|---------------------------------------------------------|
| `/backup`       | Weiterleitung nach `/backup/login` bzw. `/backup/admin` |
| `/backup/admin` | Administration der Backups                              |
| `/backup/login` | Backup-Login                                            |
| `/backup/logs`  | Konsolenausgaben (der letzten Aktion)                   |