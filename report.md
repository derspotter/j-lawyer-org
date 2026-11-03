# Report 2592

- Branch: `fix/2592-restore-mariadb-collation` (fork derspotter/j-lawyer-org), base upstream/master 7b3b3dfa8
- Commits (newest first):
  - 7088e5caa derspotter <jayjag@posteo.de> | fix(backup): Kollation wird nicht ersetzt, wenn sql_mode in einem versionsabhängigen Teil einer Anweisung, ANSI_QUOTES oder Kollationsvariablen in Triggern oder Prozeduren das Ergebnis unsicher machen, dann bricht die Wiederherstellung vor dem Löschen ab, ebenso bei Fehlern beim Auspacken der Datenbanksicherung. Refs #2592
  - 8932d6b1b derspotter <jayjag@posteo.de> | fix(backup): Wiederherstellung bereitet die angepasste Datenbanksicherung vor dem Löschen des Datenverzeichnisses vor und lässt die Sicherung unverändert, nur der Eigentümer kann die Kopie lesen, Kollation nur nach COLLATE und in Kollationsvariablen, offene ausführbare Kommentare und sql_mode in versionsabhängigen Kommentaren verhindern das Ersetzen. Refs #2592
  - 0234d855d derspotter <jayjag@posteo.de> | fix(backup): Ersetzen der Kollation beachtet DELIMITER, SET-Anweisungen im Rumpf von Triggern und Prozeduren ändern den sql_mode nicht mehr. Refs #2592
  - 6b1a9d097 derspotter <jayjag@posteo.de> | fix(backup): Ersetzen der Kollation beachtet NO_BACKSLASH_ESCAPES in Triggern aus der Sicherung, Tests prüfen den Speicherbedarf bei großen Datensätzen. Refs #2592
  - 25b452efb derspotter <jayjag@posteo.de> | fix(backup): Kollation wird nur noch im SQL-Code ersetzt, nicht in Zeichenketten, Bezeichnern und Kommentaren, und die Sicherung wird ohne Zwischenspeichern ganzer Zeilen verarbeitet. Refs #2592
  - 198715355 derspotter <jayjag@posteo.de> | fix(backup): Kollation nur in den SQL-Anweisungen ersetzen und nur, wenn der Zielserver sie nicht kennt. Refs #2592
  - da04769b6 derspotter <jayjag@posteo.de> | fix(backup): Wiederherstellung in MariaDB ersetzt die MySQL-8-Kollation utf8mb4_0900_ai_ci. Closes #2592

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-backupmgr/src/main/java/org/jlawyer/backupmgr/impl/RestoreExecutor.java` | 89 | 12 |
| `j-lawyer-backupmgr/src/main/java/org/jlawyer/backupmgr/impl/SqlDumpUtils.java` | 1438 | 0 |
| `j-lawyer-backupmgr/src/test/java/org/jlawyer/test/backupmgr/RestorePreparationTest.java` | 718 | 0 |
| `j-lawyer-backupmgr/src/test/java/org/jlawyer/test/backupmgr/SqlDumpUtilsTest.java` | 1260 | 0 |
| total | 3505 | 12 |

## Build and tests

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch at commit 7088e5caa built (started 03.11.2026 19:20 UTC) and master 7b3b3dfa8 built (02.11.2026, in a separate worktree) with the identical command in the same container:

```
mvn -B -ntp -pl j-lawyer-backupmgr -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| j-lawyer-backupmgr | 27 / 27 / 0 / 0 | n/a |
| total | 27 / 27 / 0 / 0 | 0 / 0 / 0 / 0 |

Failing tests: identical to master

- none

## Antwort auf Review 02.11.
1. [P1] Data changed: agreed, fixed in 198715355. Replacement only after `COLLATE` (with optional `=`) and in `...collation... =` assignments, and never in lines starting with `INSERT ` or `REPLACE ` (mysqldump writes one line per statement, escapes newlines inside values). Test `testDataIsNotChanged` covers a note containing the collation name and a `COLLATE ...` text inside an INSERT and a REPLACE.
2. [P2] Compatible targets converted: agreed, fixed. `isMariaDb()` is replaced by `isCollationUnknown()`, which runs `SELECT _utf8mb4'a' COLLATE utf8mb4_0900_ai_ci` and only reports "unknown" for error 1273. Any other failure leaves the dump unchanged (as before). Checked against MariaDB 10.11.14 (unknown) and with `utf8mb4_unicode_ci` as a known collation, see Evidence. MySQL 8 / MariaDB >= 11.4.5 not available here.
3. [P3] Boundary test: agreed, fixed. The filler now accounts for the `COLLATE=` prefix and the test asserts that the block boundary lies "before" characters into the collation name, for every position 1 to 17.
4. [P2] Build evidence: fixed. Branch and master built with the identical command `-pl j-lawyer-backupmgr -am` (parent and backup manager only), stated as such.

## Evidence (02.11., MariaDB 10.11.14 local, built j-lawyer-backupmgr.jar of this branch, reflection on the private methods)
- `isCollationUnknown("utf8mb4_0900_ai_ci")` = true, `isCollationUnknown("utf8mb4_unicode_ci")` = false.
- `restoreDatabase()` with a mysqldump-8.0-style dump (two columns and the table with `utf8mb4_0900_ai_ci`, plus an INSERT whose value contains "Kollation utf8mb4_0900_ai_ci, siehe COLLATE utf8mb4_0900_ai_ci"): progress "... ersetzt (3 Stellen)", returned normally, table `case_notes` with `utf8mb4_unicode_ci`, the stored note is unchanged including both mentions of the collation and the umlaut.
- Red check of the data test: the 01.11. implementation (plain replacement in the whole file) would change both mentions inside the INSERT.

## Antwort auf Review 03.11.
1. [P2] Quoted SQL values: agreed, fixed in 25b452efb. The regex over lines is replaced by a streaming scanner over the SQL. It tracks single and double quoted strings (backslash escapes and doubled quotes), backtick identifiers, line comments (`-- ` and `#`) and block comments. Executable comments (`/*!50003 ... */` and MariaDB's `/*M!...*/`) are treated as SQL, because mysqldump writes `SET collation_connection` and view definitions into them. The collation is only replaced when the whole word matches and the SQL code right before it (quoted text and comments act as a separator) ends with `COLLATE`, `COLLATE=` or `...collation... =`. INSERT and REPLACE statements (recognized by their first word, up to the end of the statement) stay unchanged as a whole as an additional guard. New regression tests cover defaults, a column comment, a table comment, a view literal next to a real `COLLATE` in the same view, a string with `\'` and with `''`, a backtick identifier, all three comment styles and a binary value containing the matching text. The pr.md wording is corrected.
2. [P2] Unbounded memory: agreed, fixed. Nothing is buffered per line or statement any more. The scanner keeps a fixed input and output buffer (64k characters each), the current word (at most 64 characters, longer words are written through) and the last 512 characters of SQL code. `testLargeRowIsStreamed` processes a 16 MB row, and the reviewer's comparison was repeated (see Evidence).
3. The old `testBinaryContentIsPreserved` wrote the bytes 0x27 and 0x5C unescaped into a string literal. mysqldump never writes that (it escapes with `mysql_real_escape_string`) and MySQL could not parse it, so the test is changed to escape the 256 byte values like mysqldump, and now also puts the collation text into the binary value.
4. [P3, from the README] SMB test prerequisites: not applicable for this branch, its build has no failing tests.

## Evidence 03.11.
- New tests: plain javac + JUnit 4.12, 9 of 9 pass. With `SqlDumpUtils` from 198715355 (the two tests that need the new `Reader`/`Writer` method left out) `testQuotedValuesAndCommentsInStatementsAreNotChanged` fails with `expected:<5> but was:<16>`.
- Memory: a 40 MiB single-row INSERT plus a CREATE TABLE, `java -Xmx64m`: 198715355 ends with `OutOfMemoryError: Java heap space`, 25b452efb replaces 1 occurrence in 237 ms.
- MariaDB 10.11.14 (local, Ubuntu 24.04 package): a MySQL-8-style dump with a table (two columns and the table with `utf8mb4_0900_ai_ci`, a default `'COLLATE utf8mb4_0900_ai_ci'`, a default `'it\'s COLLATE=utf8mb4_0900_ai_ci'`, a column comment `collation_connection = utf8mb4_0900_ai_ci`, a table comment `siehe COLLATE utf8mb4_0900_ai_ci`), a row with the collation in a text and in a binary value (all 256 byte values, escaped like mysqldump) and a view selecting the literal `'COLLATE utf8mb4_0900_ai_ci'` next to a real `COLLATE`. Unprocessed: `ERROR 1273 (HY000) at line 5: Unknown collation: 'utf8mb4_0900_ai_ci'`. Processed: 4 replacements, `mysql` exit 0, column and table collation `utf8mb4_unicode_ci`, both defaults, both comments, the view literal and the text unchanged, `HEX(bin)` identical to the original bytes.

## Antwort auf Review 03.11. (2)
1. [P2] SQL mode in trigger dumps: agreed, fixed in 6b1a9d097. The scanner follows the session's sql_mode: in every SET statement (also inside executable comments, the version number of `/*!50003` is skipped) it evaluates assignments to `sql_mode` (a string literal sets or clears NO_BACKSLASH_ESCAPES, `@var` restores a value saved before, `@@sql_mode` keeps it, GLOBAL is ignored) and saves the current mode for `SET @var = @@sql_mode`, which is what mysqldump writes around triggers and routines (`@saved_sql_mode`) and at the start and end of a dump (`@OLD_SQL_MODE`). While NO_BACKSLASH_ESCAPES is set, a backslash in a string literal is an ordinary character. `testTriggerWithNoBackslashEscapes` uses a mysqldump-style trigger block with `SET NEW.a = CONCAT('\', 'COLLATE utf8mb4_0900_ai_ci')` under NO_BACKSLASH_ESCAPES, a table with collations before it and a table with `DEFAULT 'it\'s'` after the mode is restored: 5 replacements, the trigger line unchanged, also with reads of 1 to 3 characters. With 25b452efb the collation inside the trigger's string is replaced. On MariaDB 10.11.14 the unprocessed block fails with error 1273, the processed one loads, the trigger keeps sql_mode `NO_BACKSLASH_ESCAPES,NO_ENGINE_SUBSTITUTION` and its text, and an insert fills the column with backslash plus the text.
2. [P3] Large-row test did not detect buffering: agreed, two tests added. `testReadsOnlyALittleAhead` feeds a generated 64 MiB row through the `Reader`/`Writer` variant, checks every output character and asserts that the input is never more than 256 KiB ahead of the output. `testLargeRowInLittleMemory` writes a dump with a 48 MiB row and runs the file variant in a separate JVM with `-Xmx32m` (same `java.home` and class path as the test). With `SqlDumpUtils` from 198715355 the same main ends with `OutOfMemoryError: Java heap space`, with 25b452efb and 6b1a9d097 it replaces 2 occurrences. The first test can not run against 198715355 at all, that version has no `Reader`/`Writer` variant.

## Evidence 03.11. (2)
- `SqlDumpUtilsTest`: plain javac + JUnit 4.12, 12 of 12 pass in 3.5 s. With `SqlDumpUtils` from 25b452efb: `testTriggerWithNoBackslashEscapes` fails (the collation inside the trigger's string literal is replaced), the other 11 pass.
- Low-memory run, `java -Xmx32m` with a 48 MiB row: 198715355 `OutOfMemoryError: Java heap space`, 25b452efb `replaced 2`, 6b1a9d097 `replaced 2`.

## Antwort auf Review 03.11. (3)
1. [P2] Semicolons inside stored-program bodies: agreed, reproduced and fixed. The scanner now ends statements at the client's delimiter like the mysql client: a statement whose first word is `DELIMITER` takes the first token of the rest of its line as the new delimiter (mysqldump switches to two semicolons before a trigger or routine and back to one after it, `DELIMITER $$` works the same way). So the body of a trigger or procedure is one statement whose first word is CREATE, and the SET statements in it are never evaluated, as the server does not execute them when creating the program. In addition, an assignment target directly after a dot (`NEW.sql_mode`, `t.sql_mode`) is not taken for the system variable. New regressions: `testSetInProcedureBodyDoesNotChangeTheMode` (the reviewer's procedure under `sql_mode=''`, with two literals and with one), `testCompoundTriggerAssigningAColumnNamedSqlMode` (BEGIN ... END trigger with `SET NEW.sql_mode` before literals with `\'`) and `testOtherDelimiter` (`DELIMITER $$`). All three check the result for reads of 1, 8, 64, 512, 4096 and 32768 characters. With 6b1a9d097 they fail with 4 instead of 3 replacements (a literal rewritten) and with -1 (dump taken for incomplete). On MariaDB 10.11.14 a processed dump with such a procedure and trigger loads, both bodies keep their literals, the trigger fills the column and the procedure returns both strings.
2. [P3] Test count in pr.md: fixed, it now says 15 (the class has 15 tests after the three new ones).

## Evidence 03.11. (3)
- `SqlDumpUtilsTest`: plain javac + JUnit 4.12, 15 of 15 pass. With `SqlDumpUtils` from 6b1a9d097: the three new tests fail (`expected:<3> but was:<4>` twice, `expected:<3> but was:<-1>` once), the other 12 pass.
- MariaDB 10.11.14: processed dump (3 replacements: column and table of `t`, table of `u`) loads with exit 0. `information_schema.routines` and `information_schema.triggers` show the bodies with `'it''s COLLATE utf8mb4_0900_ai_ci'` (the server normalises the escaped quote), inserting into `t` gives `it's COLLATE utf8mb4_0900_ai_ciit's COLLATE utf8mb4_0900_ai_ci`, `CALL p()` returns both strings.

## Antwort auf Copilot-Review 03.11.

Neue Commits: 8932d6b1b.

Build (Stand 8932d6b1b, gestartet 03.11.2026 18:44 UTC, Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9, Maven 3.9.11):

```
mvn -B -ntp -pl j-lawyer-backupmgr -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

Ergebnis: BUILD SUCCESS. 21 Tests gesamt, 21 ausgeführt, 0 übersprungen, 0 fehlgeschlagen. Die fehlgeschlagenen Tests sind dieselben wie auf master 7b3b3dfa8 (siehe Build and tests), die neuen Tests bestehen.

Ende-zu-Ende mit MariaDB 10.11.14 und RestoreExecutor.restore() aus diesem Stand: Mit einer MySQL-8-Sicherung werden 2 Stellen ersetzt, die Tabelle hat danach utf8mb4_unicode_ci, Standardwert und Daten mit dem Namen der Kollation sind unverändert, jlawyerdb-dump.sql im Sicherungsverzeichnis ist bytegleich mit dem Original und die Kopie ist danach gelöscht. Blockiert ein Verzeichnis den Namen der Kopie, bricht restore() mit DirectoryNotEmptyException ab, die Datei im Datenverzeichnis ist noch da und die Datenbank wurde nicht angelegt. Welche Formen die Sicherung enthält: IterativeBackupExecutor ruft mysqldump ohne Optionen auf (Tabellen, Ansichten, Trigger, keine Prozeduren), das Schema von j-lawyer hat keine Trigger und Prozeduren. pr.md ist angepasst.

Antworten je Thread:

- PRRT_kwDOCUQTLc6zRFYL: Behoben in 8932d6b1b: Die Datenbanksicherung wird jetzt vor dem Leeren des Datenverzeichnisses ausgepackt und geprüft. Nur wenn der Server die Kollation nicht kennt und die Sicherung sie verwendet (Zählung ohne Schreiben), wird eine angepasste Kopie geschrieben. Scheitert das, bricht die Wiederherstellung ab, bevor etwas gelöscht wird. Mit MariaDB 10.11 durchgespielt: Datenverzeichnis und Datenbank bleiben dann unverändert. Die Sicherung selbst wird nicht mehr verändert, die Kopie wird nach dem Einspielen gelöscht.
- PRRT_kwDOCUQTLc6zRFZF: Behoben in 8932d6b1b: Kopie und temporäre Datei werden mit den Rechten rw------- angelegt, wo das Dateisystem POSIX-Rechte kennt. Ersetzt replaceCollation eine Datei, übernimmt die neue Datei die Rechte der alten. Tests für beides, der Test für die Rechte schlägt mit dem vorherigen Stand fehl.
- PRRT_kwDOCUQTLc6zRFZl: Behoben in 8932d6b1b: Die zweite Variante passt nur noch auf collation_connection, collation_server und collation_database, auch mit @@, SESSION oder GLOBAL. Benutzervariablen, Spalten wie NEW.collation und lokale Variablen bleiben unverändert. Neuer Test dazu.
- PRRT_kwDOCUQTLc6zRFaL: Abgelehnt: Die Sicherung von j-lawyer erstellt mysqldump ohne Optionen, es schreibt Bezeichner in Backticks. Das Schema von j-lawyer enthält keine Trigger und keine Prozeduren, in deren Text ANSI_QUOTES gelten könnte. Ein solcher Fall endet sicher: Die Sicherung gilt als unvollständig, es wird nichts ersetzt, und die Wiederherstellung meldet wie ohne diesen PR den Fehler 1273 des Servers.
- PRRT_kwDOCUQTLc6zRFa2: Behoben in 8932d6b1b: Ist am Ende der Datei ein ausführbarer Kommentar noch offen, gilt die Sicherung als unvollständig und es wird nichts ersetzt. Neuer Test dazu.
- PRRT_kwDOCUQTLc6zRFbR: Abgelehnt: mysqldump schreibt die Kollation in SET collation_connection ohne Anführungszeichen. Ein Wert in Anführungszeichen bleibt bewusst unverändert, die Wiederherstellung meldet dann den Fehler 1273 des Servers, statt eine Zeichenkette umzuschreiben.
- PRRT_kwDOCUQTLc6zRFbv: Behoben in 8932d6b1b, sicher statt nachgebildet: MariaDB 10.11.14 führt /*!50503 und /*M!100100 aus und ignoriert /*!50700 und /*!80000 (geprüft: SELECT 1 /*!50503 +10 */ /*!50700 +100 */ /*!80000 +1000 */ /*M!100100 +10000 */ ergibt 10011). Ändert eine Anweisung in einem Kommentar, den der Zielserver vielleicht ignoriert (ab 50700 oder /*M!), den sql_mode, wird nichts ersetzt. mysqldump schreibt sql_mode nur in /*!40101 und /*!50003, die alle in Frage kommenden Server ausführen. Neuer Test, er schlägt mit dem vorherigen Stand fehl.
- PRRT_kwDOCUQTLc6zRFcO: Behoben in 8932d6b1b: Kommentare (/* */, -- und #) zwischen COLLATE und dem Namen zählen wie Leerraum, ihr Inhalt bleibt geschützt. Neuer Test dazu.

## Antwort auf Codex 03.11. (Runde 2)

Neuer Commit: 7088e5caa.

Build (Stand 7088e5caa, gestartet 03.11.2026 19:20 UTC, Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9, Maven 3.9.11):

```
mvn -B -ntp -pl j-lawyer-backupmgr -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

Ergebnis: BUILD SUCCESS. 27 Tests gesamt, 27 ausgeführt, 0 übersprungen, 0 fehlgeschlagen. Die fehlgeschlagenen Tests sind dieselben wie auf master 7b3b3dfa8 (siehe Build and tests), die neuen Tests bestehen und schlagen mit dem vorherigen Stand fehl.

Punkte: Unsicherheit gilt jetzt je Anweisung, nicht nach ihrem ersten Wort. ANSI_QUOTES, nicht verstandene sql_mode-Werte (Ausdrücke, nicht gesicherte Variablen) und Zuweisungen der Kollation an Kollationsvariablen in Rümpfen von Triggern und Prozeduren machen das Ergebnis unsicher, dann wird nichts ersetzt. RestoreExecutor bricht vor dem Löschen ab, wenn beim Auspacken eine Datei nicht geschrieben werden konnte oder die Datenbanksicherung fehlt, und wenn der Server die Kollation nicht kennt, die Sicherung sie enthält und nicht sicher angepasst werden kann. Ein Fehler beim Löschen der Kopie überdeckt weder einen Fehler der Wiederherstellung noch macht er eine erfolgreiche zum Fehler. Neue Tests: 5 in SqlDumpUtilsTest (vier davon schlagen mit 8932d6b1b fehl, der fünfte prüft die neue Suche in der Datei) und RestorePreparationTest (mit 8932d6b1b wird das Datenverzeichnis gelöscht). Ende-zu-Ende mit MariaDB 10.11.14 und RestoreExecutor.restore(): eine MySQL-8-Sicherung wird mit 2 Ersetzungen eingespielt, eine Sicherung mit ANSI_QUOTES bricht mit der Meldung ab, Datenverzeichnis und vorhandene Datenbank bleiben unverändert. Hinweis: Ein erster lokaler Stand dieses Commits hatte die vorhandene, auskommentierte RestoreExecutorTest.java überschrieben. Das wurde vor dem Push korrigiert, der neue Test liegt in RestorePreparationTest.java, RestoreExecutorTest.java ist unverändert.

pr.md ist angepasst (siehe die Punkte oben).

Antworten je Thread:

- PRRT_kwDOCUQTLc6zRFYL: Behoben in 8932d6b1b und 7088e5caa: Die Datenbanksicherung wird vor dem Leeren des Datenverzeichnisses ausgepackt und geprüft. Schlägt das Auspacken fehl, scheitert das Schreiben der angepassten Kopie, oder kennt der Server die Kollation nicht und die Sicherung enthält sie, kann aber nicht sicher angepasst werden, bricht die Wiederherstellung ab, bevor etwas gelöscht wird. Die Sicherung selbst bleibt unverändert. Die Kopie wird nach dem Einspielen gelöscht, ein Fehler beim Löschen wird gemeldet und ändert das Ergebnis der Wiederherstellung nicht.
- PRRT_kwDOCUQTLc6zRFZF: Behoben in 8932d6b1b: Die Kopie für die Wiederherstellung wird mit den Rechten rw------- angelegt, wo das Dateisystem POSIX-Rechte kennt. Ersetzt replaceCollation eine Datei an Ort und Stelle, bekommt die neue Datei die POSIX-Rechtebits der alten, Eigentümer, Gruppe und ACLs sind die einer neu angelegten Datei. Die Wiederherstellung ersetzt die Sicherung nicht mehr.
- PRRT_kwDOCUQTLc6zRFZl: Behoben in 8932d6b1b und 7088e5caa: Ersetzt wird nach COLLATE und in Zuweisungen an collation_connection, collation_server und collation_database, nicht nach einem Punkt (auch mit Leerzeichen dazwischen, z.B. NEW. collation_connection). Im Rumpf eines Triggers oder einer Prozedur kann eine gleichnamige lokale Variable gemeint sein. Dort wird eine solche Zuweisung nicht ersetzt, und die Wiederherstellung bricht vor dem Löschen ab.
- PRRT_kwDOCUQTLc6zRFaL: Behoben in 7088e5caa statt abgelehnt: Setzt die Sicherung einen sql_mode mit ANSI_QUOTES (auch über ANSI oder Sammelmodi wie ORACLE), wird nichts ersetzt und die Wiederherstellung bricht vor dem Löschen mit einer Meldung ab. Doppelte Anführungszeichen als Bezeichner werden nicht nachgebildet.
- PRRT_kwDOCUQTLc6zRFbv: Behoben in 8932d6b1b und 7088e5caa, sicher statt nachgebildet: Ändert eine Anweisung den sql_mode und enthält sie irgendwo einen ausführbaren Kommentar, den der Zielserver vielleicht ignoriert (ab /*!50700 oder /*M!), wird nichts ersetzt und die Wiederherstellung bricht vor dem Löschen ab. Das gilt auch, wenn nur die Zuweisung im Kommentar steht. MariaDB 10.11.14 ignoriert /*!50700 und /*!80000 (geprüft mit SELECT).
