fix(backup): Wiederherstellung in MariaDB ersetzt die MySQL-8-Kollation utf8mb4_0900_ai_ci

## Problem
Eine Datensicherung von einem System mit MySQL 8 enthält in der Datenbanksicherung die Kollation `utf8mb4_0900_ai_ci`. MariaDB kennt diese Kollation nicht (bis 11.4.4). Beim Wiederherstellen in eine MariaDB, im Issue 10.11.8, schlägt das Anlegen der Tabellen mit „Unknown collation: 'utf8mb4_0900_ai_ci'“ fehl. Laut Issue ließ sich die Sicherung nach manuellem Ersetzen durch `utf8mb4_unicode_ci` importieren.

## Ursache
`RestoreExecutor.restoreDatabase()` spielt `jlawyerdb-dump.sql` unverändert über den `mysql`-Client ein. Dazu kommt: Fehler innerhalb von `source` ändern den Exit-Code von `mysql` nicht. Die Wiederherstellung meldet deshalb keinen Fehler, obwohl Tabellen fehlen.

## Fix
Vor dem Einspielen fragt der Backupmanager per JDBC die Version des Zielservers ab. Ist es eine MariaDB, wird in der Datenbanksicherung `utf8mb4_0900_ai_ci` durch `utf8mb4_unicode_ci` ersetzt, wie im Issue vorgeschlagen (`sed -i 's/utf8mb4_0900_ai_ci/utf8mb4_unicode_ci/g' jlawyerdb-dump.sql`). Die neue Hilfsklasse `SqlDumpUtils` liest die Datei dabei in Blöcken und schreibt jedes Byte unverändert zurück. Die Datei wird nur ersetzt, wenn die Kollation vorkommt. Bei MySQL als Ziel bleibt die Sicherung unverändert, weil `utf8mb4_unicode_ci` andere Vergleichsregeln hat (z.B. für Leerzeichen am Ende) und das bei eindeutigen Indizes zu Konflikten führen könnte.

## Verifiziert
- Neuer Test `SqlDumpUtilsTest`: Ersetzung, Fundstellen über die Blockgrenze hinweg an jeder Position, Binärdaten und Umlaute bleiben byte-genau erhalten, Datei ohne Fundstelle bleibt unangetastet.
- Ende-zu-Ende mit MariaDB 10.11.14 (Ubuntu 24.04) und einer Sicherung im Format von mysqldump 8.0 mit `COLLATE=utf8mb4_0900_ai_ci`, Aufruf von `RestoreExecutor.restoreDatabase()` mit dem gebauten Backupmanager: Mit master meldet MariaDB „Unknown collation“, die Tabelle fehlt, die Methode endet trotzdem ohne Fehler. Mit diesem Branch werden 3 Stellen ersetzt, die Tabelle wird mit `utf8mb4_unicode_ci` angelegt, Daten samt Umlauten sind vollständig.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build: `mvn -B -ntp -pl j-lawyer-backupmgr -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Alle Module bauen, keine fehlgeschlagenen Tests. Auf master hat j-lawyer-backupmgr keine aktiven Tests (0 ausgeführt), auf diesem Branch 4. Fehlgeschlagene Testklassen damit identisch mit master (keine).

## Nicht abgedeckt
- Dass `mysql` bei Fehlern in `source` mit Exit-Code 0 endet und die Wiederherstellung dann als erfolgreich gilt, bleibt unverändert. Das wäre ein eigener PR.
- Wiederherstellung von MySQL 8 nach MySQL 8 nicht getestet, dort greift die Ersetzung nicht.
- Während der Ersetzung wird kurzzeitig zusätzlicher Platz in Größe der Datenbanksicherung benötigt.

Closes #2592

🤖 Generated with [Claude Code](https://claude.com/claude-code)
