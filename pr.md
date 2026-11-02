fix(backup): Wiederherstellung ersetzt die MySQL-8-Kollation utf8mb4_0900_ai_ci, wenn der Zielserver sie nicht kennt

## Problem
Eine Datensicherung von einem System mit MySQL 8 enthält in der Datenbanksicherung die Kollation `utf8mb4_0900_ai_ci`. MariaDB kennt diese Kollation vor Version 11.4.5 nicht. Beim Wiederherstellen in eine solche MariaDB, im Issue 10.11.8, schlägt das Anlegen der Tabellen mit „Unknown collation: 'utf8mb4_0900_ai_ci'“ fehl. Laut Issue ließ sich die Sicherung nach manuellem Ersetzen durch `utf8mb4_unicode_ci` importieren.

## Ursache
`RestoreExecutor.restoreDatabase()` spielt `jlawyerdb-dump.sql` unverändert über den `mysql`-Client ein.

## Fix
- Vor dem Einspielen fragt der Backupmanager den Zielserver per JDBC, ob er die Kollation kennt (`SELECT _utf8mb4'a' COLLATE utf8mb4_0900_ai_ci`). Nur wenn der Server mit „Unknown collation“ (Fehler 1273) antwortet, wird ersetzt. MySQL 8 und MariaDB ab 11.4.5 behalten die Sicherung damit unverändert, ebenso wenn die Prüfung selbst scheitert.
- Ersetzt wird durch `utf8mb4_unicode_ci`, wie im Issue vorgeschlagen, und nur dort, wo die Anweisungen der Sicherung die Kollation als Kollation verwenden: nach `COLLATE` und in Kollationsvariablen wie `collation_connection`. Zeilen mit Daten (`INSERT` und `REPLACE`, in einer mysqldump-Datei je eine Zeile) bleiben unverändert, auch wenn ein gespeicherter Text den Namen der Kollation enthält.
- Die neue Hilfsklasse `SqlDumpUtils` liest die Datei in Blöcken und schreibt jedes Byte unverändert zurück, auch Binärdaten und Zeilenenden. Die Datei wird nur ersetzt, wenn die Kollation vorkommt.

## Verifiziert
- Test `SqlDumpUtilsTest` (j-lawyer-backupmgr) mit 5 Fällen: Ersetzung in `CREATE TABLE` und `collation_connection`, Daten in `INSERT` und `REPLACE` bleiben unverändert, die Kollation über jede Position einer Blockgrenze hinweg, Binärdaten, Umlaute und Zeilenenden bleiben byte-genau erhalten, Datei ohne Fundstelle bleibt unangetastet.
- Ende-zu-Ende mit MariaDB 10.11.14 (Ubuntu 24.04) und `RestoreExecutor.restoreDatabase()` aus dem gebauten Backupmanager, siehe report.md.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches und Vergleichsbuild von master 7b3b3dfa8 mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-backupmgr -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 5 gesamt, davon 5 ausgeführt und 0 übersprungen, 0 fehlgeschlagen. Auf master: 0 gesamt, 0 ausgeführt, 0 übersprungen, 0 fehlgeschlagen. Neu sind 5 Tests.
- Keine fehlgeschlagenen Tests, wie auf master.

## Nicht abgedeckt
- Wiederherstellen in MySQL 8 oder MariaDB ab 11.4.5 nicht getestet, dort greift die Ersetzung nicht. Mit MariaDB 10.11 ist geprüft, dass eine bekannte Kollation als bekannt erkannt wird.
- Wiederherstellungen über die Oberfläche des Backupmanagers nicht ausprobiert.
- Dass `mysql` bei Fehlern in `source` mit Exit-Code 0 endet, behandelt der Branch `fix/restore-sql-fehler-erkennen`.
- Während der Ersetzung wird kurzzeitig zusätzlicher Platz in Größe der Datenbanksicherung benötigt.

Closes #2592

🤖 Generated with [Claude Code](https://claude.com/claude-code)
