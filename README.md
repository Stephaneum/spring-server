# Stephaneum Spring

## Voraussetzungen

- JSF-Projekt (Authentifizierung läuft noch darüber)
- Java 8
- Tomcat 8.5+
- MariaDB oder MySQL
- IntelliJ CE / UE (empfohlen)

## Projekt starten

1. rechts den Gradle-Reiter öffnen
2. Doppelklick: Tasks > application > bootRun

## Export als .war-Datei

1. rechts den Gradle-Reiter öffnen
2. Doppelklick: Tasks > build > assemble
3. die Datei liegt nun in <Projekt-Ordner>/build/libs/

## Migration von JSF nach Spring

Stück für Stück werden Komponent von JSF nach Spring migriert.
Zuerst wird der interne Bereich inklusive Login / Registrierung übernommen.
Danach folgt der öffentliche Bereich.

Blackboard und Backup sind bereits in Spring implementiert.
Hier werden nur Funktionen aufgeführt, die ursprünglich in JSF implementiert wurden:

**X** - zur Zeit implementiert und aktiv

**N** - nächstes Ziel

Bereich | Seite(n) | JSF | Spring
---|---|---|---
Öffentlich|Startseite|X|N
Öffentlich|Beiträge|X|N
Öffentlich|Login/Registrierung|X|N
Öffentlich|Impressum, Kontakt, etc.|X|N
Admin|Konfiguration| |X
Admin|Benutzerdefinierte Seiten| |X
Admin|Rubriken| |D
Admin|Zugangscodes| |X
Admin|Nutzerdaten-Manager| |X
Admin|Logdaten| |X
Verwaltung|Vertretungsplan-Manager| |X
Gemeinschaft|Beitrag-Manager| |X
Gemeinschaft|Klassen-Cloud| |X
Gemeinschaft|Projekt-Cloud| |X
Nutzer|Nutzer-Cloud| |X
Nutzer|Account-Einstellungen| |X

#### Kommunikation JSF nach Spring

Authorisierung über `?key=<JWT>`. Zum Beispiel `https://stephaneum.de/beitrag-manager?key=eyJhbG....`

#### Kommunikation Spring nach JSF

Falls Modifikationen an der Datenbank durchgeführt wurde, dann müssen die Änderungen JSF mitgeteilt werden, da JSF vieles im Cache speichert.

Events werden an `https://stephaneum.de/event?event=<JWT>` gesendet.

Im JWT-Token ist das Event einkodiert.

Logout erfolgt in 2 Schritten:
1. `/api/logout` Logout in Spring
2. `/logout.xhtml` Logout in JSF und Weiterleitung zur Startseite

## Routen (GET)

Hier werden Adressen (Routen) aufgeführt, wo der Server mit einer HTML-Datei antwortet.

### Homepage
Route|Info
---|---
`/login`|Login (wird momentan nicht genutzt)
`/beitrag-manager`|Beitrag-Manager
`/cloud`|Nutzer-Cloud
`/admin-static`|Verwaltung der benutzerdefinierten Seiten
`/admin-codes`|Verwaltung der Zugangscodes
`/admin-logs`|Logdaten
`/vertretungsplan-manager`|Verwaltung des Vertretungsplans

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