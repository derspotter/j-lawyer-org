fix(backup): Wiederherstellung ersetzt die MySQL-8-Kollation utf8mb4_0900_ai_ci, wenn der Zielserver sie nicht kennt

## Problem
Eine Datensicherung von einem System mit MySQL 8 enthält in der Datenbanksicherung die Kollation `utf8mb4_0900_ai_ci`. MariaDB kennt diese Kollation vor Version 11.4.5 nicht. Beim Wiederherstellen in eine solche MariaDB, im Issue 10.11.8, schlägt das Anlegen der Tabellen mit „Unknown collation: 'utf8mb4_0900_ai_ci'“ fehl. Laut Issue ließ sich die Sicherung nach manuellem Ersetzen durch `utf8mb4_unicode_ci` importieren.

## Ursache
`RestoreExecutor.restoreDatabase()` spielt `jlawyerdb-dump.sql` unverändert über den `mysql`-Client ein.

## Fix
- Vor dem Einspielen fragt der Backupmanager den Zielserver per JDBC, ob er die Kollation kennt (`SELECT _utf8mb4'a' COLLATE utf8mb4_0900_ai_ci`). Nur wenn der Server mit „Unknown collation“ (Fehler 1273) antwortet, wird ersetzt. MySQL 8 und MariaDB ab 11.4.5 behalten die Sicherung damit unverändert, ebenso wenn die Prüfung selbst scheitert.
- Ersetzt wird durch `utf8mb4_unicode_ci`, wie im Issue vorgeschlagen, und nur dort, wo der SQL-Code der Sicherung die Kollation als Kollation verwendet: nach `COLLATE` und in Kollationsvariablen wie `collation_connection`.
- Die neue Hilfsklasse `SqlDumpUtils` liest die Sicherung als SQL. Zeichenketten ('…' und "…", mit Backslash-Maskierung und verdoppelten Anführungszeichen), Bezeichner in Backticks und Kommentare (`--`, `#`, `/* */`) bleiben unverändert. Gespeicherte Texte, Standardwerte, Tabellen- und Spaltenkommentare und Texte in Ansichten, die den Namen der Kollation enthalten, werden also nicht geändert. Ausführbare Kommentare wie `/*!50003 … */`, in die mysqldump SQL-Anweisungen schreibt, werden wie SQL behandelt. `INSERT`- und `REPLACE`-Anweisungen bleiben als Ganzes unverändert. Änderungen des `sql_mode` (setzen, in einer Variablen sichern und wiederherstellen, wie mysqldump es um Trigger und Prozeduren herum schreibt) werden mitverfolgt. Solange `NO_BACKSLASH_ESCAPES` gilt, ist ein Backslash in einer Zeichenkette ein normales Zeichen.
- Die Datei wird Zeichen für Zeichen durchlaufen, ohne ganze Zeilen zwischenzuspeichern. Der Speicherbedarf hängt also nicht von der Größe einzelner Datensätze ab. Jedes Byte wird unverändert zurückgeschrieben, auch Binärdaten und Zeilenenden. Die Datei wird nur ersetzt, wenn die Kollation vorkommt. Endet die Sicherung mitten in einer Zeichenkette oder einem Kommentar, ist sie kein gültiges SQL und bleibt unverändert.

## Verifiziert
- Test `SqlDumpUtilsTest` (j-lawyer-backupmgr) mit 9 Fällen: Ersetzung in `CREATE TABLE`, in einer Ansicht und in `collation_connection`. Unverändert bleiben Daten in `INSERT` und `REPLACE`, Standardwerte, Kommentare von Tabelle und Spalte, Texte in einer Ansicht, Bezeichner, Kommentare und Binärdaten mit dem Namen der Kollation (alle 256 Bytewerte, maskiert wie von mysqldump). Außerdem ein Trigger, der mit `NO_BACKSLASH_ESCAPES` angelegt wurde und die Zeichenkette `'\'` enthält, mit Kollationen davor und danach, die Kollation über jede Position einer 64k-Blockgrenze hinweg, Lesen in Stücken von 1 bis 7 Zeichen, eine Zeile mit 16 MB, eine unvollständige Sicherung und eine Datei ohne Fundstelle. Zum Speicherbedarf: Eine erzeugte Zeile mit 64 MB darf beim Lesen höchstens 256 KB vor der Ausgabe liegen, und eine Sicherung mit einer Zeile von 48 MB wird in einer eigenen JVM mit 32 MB Heap verarbeitet.
- Mit dem Stand vom 02.11. ersetzt dieselbe Sicherung mit Tabelle, Ansicht und Kollationsvariable 16 statt 5 Stellen. Mit 32 MB Heap bricht er bei einer Zeile mit 48 MB mit `OutOfMemoryError` ab, der neue Stand verarbeitet sie. Mit dem Stand vom Vormittag des 03.11. wird im Trigger die Kollation in der Zeichenkette ersetzt.
- Ende-zu-Ende mit MariaDB 10.11.14 (Ubuntu 24.04): Eine Sicherung im Stil von MySQL 8 mit Tabelle, Daten, Binärwert und Ansicht scheitert unverändert mit Fehler 1273. Nach der Ersetzung lässt sie sich fehlerfrei einspielen, und Standardwerte, Kommentare, der Text der Ansicht, die Daten und der Binärwert sind byte-genau erhalten. Ebenso eine Sicherung mit einem Trigger mit `NO_BACKSLASH_ESCAPES`: Der Trigger wird mit seinem `sql_mode` und unverändertem Text angelegt und setzt beim Einfügen den erwarteten Wert. Siehe report.md.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches (Stand 6b1a9d097, gestartet 03.11.2026 09:41 UTC) und Vergleichsbuild von master 7b3b3dfa8 (02.11.2026) mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-backupmgr -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 12 gesamt, davon 12 ausgeführt und 0 übersprungen, 0 fehlgeschlagen. Auf master: 0 gesamt, 0 ausgeführt, 0 übersprungen, 0 fehlgeschlagen. Neu sind 12 Tests.
- Keine fehlgeschlagenen Tests, wie auf master.

## Nicht abgedeckt
- Wiederherstellen in MySQL 8 oder MariaDB ab 11.4.5 nicht getestet, dort greift die Ersetzung nicht. Mit MariaDB 10.11 ist geprüft, dass eine bekannte Kollation als bekannt erkannt wird.
- Wiederherstellungen über die Oberfläche des Backupmanagers nicht ausprobiert.
- Dass `mysql` bei Fehlern in `source` mit Exit-Code 0 endet, behandelt der Branch `fix/restore-sql-fehler-erkennen`.
- Während der Ersetzung wird kurzzeitig zusätzlicher Platz in Größe der Datenbanksicherung benötigt.

Closes #2592

🤖 Generated with [Claude Code](https://claude.com/claude-code)
