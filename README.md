# Stephaneum Spring

## Voraussetzungen

- JSF-Projekt (Authentifizierung läuft noch darüber)
- Java 8
- Tomcat 8.5
- MariaDB 10.1
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
Öffentlich|Startseite|X|
Öffentlich|Beiträge|X|
Öffentlich|Login/Registrierung|X|
Öffentlich|Impressum, Kontakt, etc.|X|
Admin|Konfiguration|X|
Admin|Benutzerdefinierte Seiten|X|N
Admin|Rubriken|X|
Admin|Backup (wird gelöscht)|X|
Admin|Zugangscodes| |X
Admin|Nutzerdaten-Manager|X|
Admin|Logdaten| |X
Verwaltung|Vertretungsplan-Manager| |X
Gemeinschaft|Beitrag-Manager| |X
Gemeinschaft|Klassen-Cloud|X|
Gemeinschaft|Projekt-Cloud|X|
Nutzer|Nutzer-Cloud|X|N
Nutzer|Account-Einstellungen|X|

### Kommunikation JSF nach Spring

Authorisierung über `?key=<JWT>`. Zum Beispiel `https://stephaneum.de/beitrag-manager?key=eyJhbG....`

### Kommunikation Spring nach JSF

Falls Modifikationen an der Datenbank durchgeführt wurde, dann müssen die Änderungen JSF mitgeteilt werden, da JSF vieles im Cache speichert.

Events werden an `https://stephaneum.de/event?event=<JWT>` gesendet.

Im JWT-Token ist das Event einkodiert.

Logout erfolgt in 2 Schritten:
1. `/api/logout` Logout in Spring
2. `/logout.xhtml` Logout in JSF und Weiterleitung zur Startseite