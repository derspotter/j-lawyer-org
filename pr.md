fix(backup): Wiederherstellung meldet Fehler beim Einspielen der Datenbanksicherung, statt Erfolg anzuzeigen

## Problem
Schlagen beim Wiederherstellen einer Datensicherung Anweisungen aus der Datenbanksicherung fehl, meldet der Backupmanager trotzdem eine erfolgreiche Wiederherstellung. Die Datenbank ist dann unvollständig, z.B. fehlen Tabellen. Aufgefallen bei #2592: Beim Einspielen einer MySQL-8-Sicherung in MariaDB fehlen alle Tabellen, die Wiederherstellung gilt aber als erfolgreich.

## Ursache
`RestoreExecutor.restoreDatabase()` spielt die Sicherung mit `mysql -e "...source jlawyerdb-dump.sql..."` ein und prüft nur den Exit-Code des Clients. Fehler in Anweisungen der per `source` eingelesenen Datei gibt der `mysql`-Client nur auf stderr aus, der Exit-Code bleibt 0.

## Fix
Die stderr-Ausgabe von `mysql` wird schon zeilenweise gelesen. Beginnt eine Zeile mit `ERROR`, wird die erste solche Zeile gemerkt, und die Wiederherstellung schlägt danach mit „Datenbankwiederherstellung fehlgeschlagen: ERROR 1273 (HY000) at line 6 …“ fehl. Kommandozeile und Oberfläche zeigen diese Meldung wie andere Fehler der Wiederherstellung an. Warnungen des Clients (`mysql: [Warning] …`) lösen das nicht aus.

## Verifiziert
- Ende-zu-Ende mit MariaDB 10.11.14 (Ubuntu 24.04) und `RestoreExecutor.restoreDatabase()` aus dem gebauten Backupmanager:
  - Sicherung mit fehlschlagenden Anweisungen (MySQL-8-Kollation): Mit master endet die Methode ohne Fehler, in der Datenbank sind 0 Tabellen. Mit diesem Branch kommt die Meldung „Datenbankwiederherstellung fehlgeschlagen: ERROR 1273 (HY000) at line 6 in file: '…/jlawyerdb-dump.sql': Unknown collation: 'utf8mb4_0900_ai_ci'“.
  - Fehlerfreie Sicherung: endet ohne Fehler, die Tabelle ist wiederhergestellt.
  - Zusammen mit dem PR zu #2592 wird die MySQL-8-Sicherung in MariaDB vollständig und ohne Fehlermeldung wiederhergestellt.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches und Vergleichsbuild von master 7b3b3dfa8 mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-backupmgr -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 0 gesamt, davon 0 ausgeführt und 0 übersprungen, 0 fehlgeschlagen. Auf master: 0 gesamt, 0 ausgeführt, 0 übersprungen, 0 fehlgeschlagen.
- Keine fehlgeschlagenen Tests, wie auf master.
- Kein Unit-Test: Die Prüfung steckt im Prozessaufruf des `mysql`-Clients.

## Nicht abgedeckt
- Wiederherstellung über die Oberfläche des Backupmanagers nicht ausprobiert, MySQL 8 als Zielserver nicht getestet.
- Bei einem Fehler läuft das Einspielen weiterhin bis zum Ende der Sicherung durch, erst danach wird der Fehler gemeldet. Die Datenbank bleibt unvollständig. Ein Abbruch beim ersten Fehler wäre mit `--abort-source-on-error` möglich, das der MariaDB-Client kennt. Ob der MySQL-Client es kennt, ist nicht geprüft.

Refs #2592

🤖 Generated with [Claude Code](https://claude.com/claude-code)
