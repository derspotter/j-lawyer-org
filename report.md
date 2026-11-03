# Report 2592

- Branch: `fix/2592-restore-mariadb-collation` (fork derspotter/j-lawyer-org), base upstream/master 7b3b3dfa8
- Commits (newest first):
  - ca95debb9 derspotter <jayjag@posteo.de> | fix(backup): Kollation wird nur in Sicherungen von mysqldump mit Standardoptionen ersetzt. sql_mode nur mit Modi, die Zeichenketten nicht anders lesen, NO_BACKSLASH_ESCAPES nur direkt um einen Trigger, ein Trigger endet erst nach einer Zeile mit doppeltem Semikolon, Tabellendaten werden Zeile für Zeile geprüft, eine Zeile mit einem Qualifier am Ende macht das Ergebnis unsicher, und die Datenbanksicherung wird nur aus ihrem eigenen Archiv ausgepackt. Refs #2592
  - 82d2b1497 derspotter <jayjag@posteo.de> | fix(backup): Kollation wird nur noch in den Zeilen ersetzt, die mysqldump schreibt (SET-Anweisungen, COLLATE in CREATE TABLE und Views), andere SQL mit sql_mode, Kollationsvariablen oder der Kollation an anderer Stelle bricht die Wiederherstellung vor dem Löschen ab. Die Datenbanksicherung wird in ein neues Verzeichnis ausgepackt, eine alte Sicherung kann nicht mehr verwendet werden, und die Prüfung vor dem Löschen beachtet Groß- und Kleinschreibung nicht. Refs #2592
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
| `j-lawyer-backupmgr/src/main/java/org/jlawyer/backupmgr/impl/RestoreExecutor.java` | 102 | 12 |
| `j-lawyer-backupmgr/src/main/java/org/jlawyer/backupmgr/impl/SqlDumpUtils.java` | 1248 | 0 |
| `j-lawyer-backupmgr/src/test/java/org/jlawyer/test/backupmgr/RestorePreparationTest.java` | 757 | 0 |
| `j-lawyer-backupmgr/src/test/java/org/jlawyer/test/backupmgr/SqlDumpUtilsTest.java` | 1031 | 0 |
| total | 3138 | 12 |

## Build and tests

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch at commit ca95debb9 built (started 03.11.2026 20:42 UTC) and master 7b3b3dfa8 built (02.11.2026, in a separate worktree) with the identical command in the same container:

```
mvn -B -ntp -pl j-lawyer-backupmgr -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| j-lawyer-backupmgr | 14 / 14 / 0 / 0 | n/a |
| total | 14 / 14 / 0 / 0 | 0 / 0 / 0 / 0 |

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

## Antwort auf Codex 03.11. (Runde 3)

Neuer Commit: 82d2b1497.

Build (Stand 82d2b1497, gestartet 03.11.2026 20:08 UTC, Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9, Maven 3.9.11):

```
mvn -B -ntp -pl j-lawyer-backupmgr -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

Ergebnis: BUILD SUCCESS. 8 Tests gesamt, 8 ausgeführt, 0 übersprungen, 0 fehlgeschlagen. Keine fehlgeschlagenen Tests, wie auf master 7b3b3dfa8. Mit dem vorherigen Stand 7088e5caa schlagen 3 der 8 Tests fehl (testOtherSqlIsNotReplaced, testContainsTextIgnoresCase, testDumpOfAnEarlierRestoreIsNotUsed).

Umbau: Der zeichenweise SQL-Lexer (DELIMITER-Verfolgung, sql_mode-Verfolgung mit gesicherten Modi, Versionen ausführbarer Kommentare, Erkennung von Rümpfen) ist entfernt. Stattdessen eine Liste der Zeilen, die mysqldump schreibt. INSERT INTO bis zur Zeile mit Semikolon am Ende bleibt unverändert (eine Anweisung je Zeile bei MySQL, ein Datensatz je Zeile bei MariaDB, das Docker-Image von j-lawyer installiert mariadb-client). SET-Anweisungen aus einfachen Zuweisungen am Anfang einer Anweisung ersetzen den Wert von collation_-Variablen und dürfen sql_mode nur auf ein Literal ohne NO_BACKSLASH_ESCAPES und ANSI_QUOTES setzen oder aus einer Variablen mit sql_mode im Namen wiederherstellen. Trigger-Rümpfe zwischen den DELIMITER-Befehlen bleiben unverändert. In allen anderen Zeilen wird nur nach COLLATE ersetzt, Zeichenketten, Bezeichner und Kommentare werden je Zeile erkannt. Jede Fundstelle des Namens, die so nicht ersetzt wird, jede andere Zeile mit sql_mode oder collation_ und jede Zeichenkette oder jeder Kommentar über das Zeilenende hinaus macht das Ergebnis unsicher. Das öffentliche replaceCollation für eine Datei an Ort und Stelle ist entfernt, die Wiederherstellung hat es nie verwendet.

Die Punkte aus Runde 2:

- P1 gesicherte Modi in versionsabhängigen Kommentaren: Wiederherstellen aus @saved ist nicht erlaubt (der Name enthält kein sql_mode), SET sql_mode='NO_BACKSLASH_ESCAPES' ebenso nicht. Das Beispiel ist unsicher (vorher 4 Ersetzungen, jetzt -1).
- P1 ANSI_QUOTES: aneinandergereihte Literale, `sql_mode` in Backticks und SET/**/sql_mode passen nicht auf die SET-Form und erwähnen sql_mode, alle drei sind unsicher (vorher 3, 2 und 2 Ersetzungen).
- P1 Großbuchstaben: containsText vergleicht ohne Beachtung der Groß- und Kleinschreibung wie das Ersetzen (neuer Test testContainsTextIgnoresCase). Das Beispiel mit ANSI_QUOTES und UTF8MB4_0900_AI_CI ist unsicher und wird gefunden.
- P1 alte Datenbanksicherung: Ausgepackt wird in ein neues Verzeichnis jlawyerdb-restore-… im Sicherungsverzeichnis (rwx------, mit umask 022 geprüft), danach wird es gelöscht. Eine alte jlawyerdb-dump.sql wird nie verwendet, sie bleibt aber auch unangetastet. Neuer Test testDumpOfAnEarlierRestoreIsNotUsed (mit 7088e5caa wird das Datenverzeichnis gelöscht). Der vorhandene Test für ein nicht auspackbares Archiv nutzt jetzt ein beschädigtes Archiv, die Datei wird teilweise geschrieben, bevor der CRC-Fehler kommt.
- P2 collation_connection als Spalte: Ersetzt wird nur in der SET-Form am Anfang einer Anweisung. Ansicht, UPDATE, t. mit 21 Leerzeichen und eine SET-Zeile nach einem UPDATE ohne Semikolon sind unsicher (vorher je 3 Ersetzungen).
- P3 Antworten: Die Antworten unten nennen die Bedingungen für den Abbruch (Zielserver meldet die Kollation als unbekannt und die Sicherung enthält ihren Namen).
- P3 Javadoc: Die Methode mit den Rechten gibt es nicht mehr. Die Beschreibung erwähnt keine Versionen von MariaDB mehr, das Ersetzen hängt nicht davon ab, welche ausführbaren Kommentare ein Server ausführt. Es gilt nur, dass eine Zeichenkette darin das Kommentarende nicht enthalten darf.

Unsichere Fälle im Test (24): Mit 7088e5caa ergeben 22 davon Ersetzungen, nur ANSI_QUOTES mit der Kollation in Großbuchstaben und die am Ende abgeschnittene Sicherung waren dort schon unsicher. Echte Fehler dort waren die Gegenbeispiele aus Runde 2, t.collate als Spaltenname mit Alias und eine Zeichenkette mit */ in einem Kommentar für MySQL 8. Die übrigen wurden dort richtig behandelt (Name in Zeichenkette, Kommentar, Bezeichner, REPLACE und Trigger-Rumpf übersprungen, COLLATE nach einer Zeichenkette über zwei Zeilen ersetzt) oder enthalten nur einen Wechsel des sql_mode ohne spätere Zeichenkette. Sie gelten jetzt als unsicher, weil mysqldump sie nicht so schreibt.

Ende-zu-Ende (MariaDB 10.11.14, RestoreExecutor.restore() mit den Klassen dieses Stands): Grundlage ist die j-lawyer-Datenbank der lokalen Testinstallation (101 Tabellen, Flyway 3.6.0.9) in einer Kopie e2esrc mit zusätzlicher Ansicht v_e2e (COLLATE in der Abfrage), Trigger trg_e2e und einem Datensatz mit dem Namen der Kollation in Groß- und Kleinschreibung, SET collation_connection, einem DELIMITER-Befehl, Backslash und Anführungszeichen. Mit mysqldump von MariaDB gesichert und in zwei Formate gebracht: MySQL 8 (Kopf von MySQL, eine Zeile je INSERT, ohne die Zeile für den Sandbox-Modus) und MariaDB-Client (unverändertes Format), jeweils utf8mb4_0900_ai_ci an 11 Tabellen, 2 Spalten, in der Ansicht und in beiden SET collation_connection. Ziel ist die Datenbank e2erestore.

```
mysql8        Kollation ... ersetzt (16 Stellen), Daten identisch mit e2esrc (1886 Datensätze), Ansicht 1, Trigger 1, Spalten mit 0900: 0
mariaclient   Kollation ... ersetzt (16 Stellen), Daten identisch mit e2esrc (1886 Datensätze), Ansicht 1, Trigger 1, Spalten mit 0900: 0
unbekannt     Abbruch: Der Datenbankserver kennt die Kollation utf8mb4_0900_ai_ci nicht ... nicht sicher ersetzt werden
alte Sicherung  Abbruch: Die Datenbanksicherung konnte nicht vollständig ausgepackt werden
```

Unbekannt ist die mysql8-Sicherung mit zusätzlicher Zeile SET sql_mode='ANSI_QUOTES' nach dem Kopf. Alte Sicherung ist ein Archiv nur mit hinweis.txt und daneben die mysql8-Sicherung als jlawyerdb-dump.sql. In beiden Abbruchfällen enthält das Datenverzeichnis weiter archivefiles/case1/brief.pdf mit dem Inhalt, e2erestore hat weiter 103 Tabellen samt Markierungstabelle e2e_marker mit dem Wert 42, und im Sicherungsverzeichnis liegt kein jlawyerdb-restore-… mehr. Daten verglichen mit mysqldump --skip-comments --no-create-info --skip-triggers von Quelle und Ziel.

numstat gegen master 7b3b3dfa8:

```
                                  vorher 7088e5caa   nachher 82d2b1497
RestoreExecutor.java              +89 -12            +86 -12
SqlDumpUtils.java (neu)           +1438              +1122
RestorePreparationTest.java (neu) +718               +748
SqlDumpUtilsTest.java (neu)       +1260              +987
gesamt                            +3505 -12          +2943 -12
ohne Lizenzkopf (662 Zeilen je neuer Datei)
Code                              865                546
Tests                             654                411
zusammen                          1519               957
```

Diff von 7088e5caa auf 82d2b1497: 491 Zeilen hinzu, 1053 entfernt.

pr.md ist angepasst: Fix beschreibt die Liste der Zeilen von mysqldump, das Verzeichnis zum Auspacken und die Gründe für ein unsicheres Ergebnis. Verifiziert nennt die 8 Tests, die 3 Fehlschläge mit 7088e5caa und den Ende-zu-Ende-Test mit beiden Formaten. Unter Nicht abgedeckt entfällt der Punkt zu replaceCollation an Ort und Stelle. Neu sind dort die nachgebauten Sicherungen statt einer echten von MySQL 8, der Abbruch bei Trigger-Rümpfen mit dem Namen der Kollation und bei Sicherungen, die nicht von mysqldump stammen, und das Verzeichnis, das bei einem harten Abbruch zurückbleibt.

Antworten je Thread:

- PRRT_kwDOCUQTLc6zRFYL: Behoben in 8932d6b1b, 7088e5caa und 82d2b1497: Die Datenbanksicherung wird vor dem Leeren des Datenverzeichnisses in ein neu angelegtes Verzeichnis ausgepackt, eine jlawyerdb-dump.sql von einer früheren Wiederherstellung wird nie verwendet. Die Wiederherstellung bricht ab, bevor etwas gelöscht wird, wenn beim Auspacken eine Datei nicht geschrieben werden kann oder das Archiv keine Datenbanksicherung enthält, wenn das Schreiben der angepassten Kopie scheitert, und wenn der Zielserver die Kollation als unbekannt meldet, die Sicherung ihren Namen in beliebiger Schreibweise enthält und sie nicht sicher angepasst werden kann. Kennt der Zielserver die Kollation oder scheitert diese Prüfung, wird die Sicherung wie bisher unverändert eingespielt. Die Sicherung selbst bleibt unverändert, das Verzeichnis zum Auspacken wird danach gelöscht.
- PRRT_kwDOCUQTLc6zRFZF: Behoben in 8932d6b1b und 82d2b1497: Die angepasste Kopie wird mit den Rechten rw------- in einem Verzeichnis mit rwx------ angelegt, wo das Dateisystem POSIX-Rechte kennt. Das Ersetzen an Ort und Stelle gibt es nicht mehr, die Wiederherstellung hat es nie verwendet.
- PRRT_kwDOCUQTLc6zRFZl: Behoben in 82d2b1497: Der Wert einer collation_-Variable wird nur noch in SET-Anweisungen ersetzt, die am Anfang einer Anweisung stehen und nur aus einfachen Zuweisungen bestehen, wie mysqldump sie schreibt. Erwähnt eine andere Zeile collation_ oder steht der Name der Kollation an einer anderen Stelle als nach COLLATE (z.B. collation_connection als Spalte in einer Ansicht oder einem UPDATE, auch nach t. mit beliebig vielen Leerzeichen), wird nichts ersetzt. Meldet der Zielserver die Kollation als unbekannt, bricht die Wiederherstellung dann vor dem Löschen ab.
- PRRT_kwDOCUQTLc6zRFaL: Behoben in 7088e5caa und 82d2b1497: sql_mode wird nur in SET-Anweisungen wie denen von mysqldump akzeptiert, mit genau einem Literal ohne ANSI_QUOTES und NO_BACKSLASH_ESCAPES. Jede andere Zeile, die sql_mode erwähnt (aneinandergereihte Literale, sql_mode in Backticks, SET direkt nach einem Kommentar), macht das Ergebnis unsicher und es wird nichts ersetzt. Meldet der Zielserver die Kollation als unbekannt und enthält die Sicherung ihren Namen, bricht die Wiederherstellung dann vor dem Löschen ab. Kennt der Zielserver die Kollation, wird die Sicherung unverändert eingespielt wie ohne diesen PR.
- PRRT_kwDOCUQTLc6zRFbv: Behoben in 82d2b1497, ohne Versionen nachzubilden: Ein Literal für sql_mode darf weder NO_BACKSLASH_ESCAPES noch ANSI_QUOTES enthalten, wiederhergestellt werden darf sql_mode nur aus einer Variablen, deren Name sql_mode enthält (wie @OLD_SQL_MODE und @saved_sql_mode bei mysqldump), und jede andere Zeile mit sql_mode macht das Ergebnis unsicher. Damit hängt das Lesen von Zeichenketten nicht mehr davon ab, welche versionsabhängigen Kommentare der Zielserver ausführt. Für ausführbare Kommentare gilt nur noch, dass eine Zeichenkette darin das Kommentarende nicht enthalten darf. Das Beispiel aus dem Review ist unsicher. Meldet der Zielserver die Kollation als unbekannt und enthält die Sicherung ihren Namen, bricht die Wiederherstellung vor dem Löschen ab.
- PRRT_kwDOCUQTLc6zRFbR: Ergänzung zu 82d2b1497: Ein Wert in Anführungszeichen wird weiterhin nicht umgeschrieben. Statt erst beim Einspielen mit Fehler 1273 zu scheitern, gilt das Ergebnis jetzt als unsicher. Meldet der Zielserver die Kollation als unbekannt, bricht die Wiederherstellung vor dem Löschen ab.
- PRRT_kwDOCUQTLc6zRFcO (nicht angefragt, die gepostete Antwort stimmt sonst nicht mehr ganz): Ergänzung zu 82d2b1497: Ein Kommentar /* … */ zwischen COLLATE und dem Namen zählt weiter wie Leerraum. Stehen COLLATE und der Name in verschiedenen Zeilen (z.B. mit -- oder # dazwischen), wird nichts ersetzt, mysqldump schreibt das nicht. Meldet der Zielserver die Kollation als unbekannt, bricht die Wiederherstellung dann vor dem Löschen ab.

## Antwort auf Codex 03.11. (Runde 4)

Neuer Commit: ca95debb9 (auf 82d2b1497).

Build (Stand ca95debb9, gestartet 03.11.2026 20:42 UTC, Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9, Maven 3.9.11):

```
mvn -B -ntp -pl j-lawyer-backupmgr -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

Ergebnis: BUILD SUCCESS. 14 Tests gesamt, 14 ausgeführt, 0 übersprungen, 0 fehlgeschlagen. Keine fehlgeschlagenen Tests, wie auf master 7b3b3dfa8. Mit 82d2b1497 schlagen die 6 neuen Tests fehl, einer je Regel: testOnlySqlModesThatKeepQuoting (Regel 1), testTriggerWithNoBackslashEscapes (Regel 2), testTriggerEndsOnlyAfterItsLastLine (Regel 3), testTableDataIsChecked (Regel 4), testDumpFromAnotherArchiveIsNotUsed in RestorePreparationTest (Regel 5), testQualifierAtTheEndOfALine (Regel 6).

Umfang ab jetzt (steht auch in pr.md unter Nicht abgedeckt): Unterstützt werden Sicherungen, die mysqldump von MySQL 8 oder MariaDB mit Standardoptionen schreibt, so wie IterativeBackupExecutor es aufruft. Alles andere wird nicht angepasst. Kennt der Zielserver die Kollation nicht und enthält die Sicherung ihren Namen, bricht die Wiederherstellung dann vor dem Löschen ab. Text in Zeichenketten und Daten wird nie umgeschrieben.

Die Punkte aus Runde 3:

- P1 DELIMITER in einer Zeichenkette eines Triggers (Regel 3): Ein Trigger beginnt nur mit der Zeile DELIMITER und doppeltem Semikolon am Anfang einer Anweisung und endet nur an einer Zeile, die genau DELIMITER mit einfachem Semikolon ist und auf eine Zeile mit doppeltem Semikolon am Ende folgt. Kommt der Name der Kollation darin vor, ist das Ergebnis unsicher. Andere DELIMITER-Zeilen sind unsicher, ebenso Befehle des mysql-Clients wie \d (nur \- aus der ersten Zeile von MariaDB ist erlaubt). Das Beispiel aus dem Review ist unsicher.
- P1 Tabellendaten (Regel 4): Eine INSERT-Zeile endet mit einer schließenden Klammer und einem Komma (es folgt ein Datensatz) oder Semikolon (die Anweisung endet), die erste Zeile auch mit VALUES (MariaDB). Jede andere Zeile, ein Zeilenende in einer Zeichenkette und eine Sicherung, die mitten in der Anweisung endet, sind unsicher. Die Zeilen werden beim Durchreichen auf Zeichenketten geprüft, der Name der Kollation außerhalb einer Zeichenkette und Kommentare sind unsicher. Beide Beispiele aus dem Review sind unsicher.
- P1 Sammelmodi (Regel 1): Ein Literal für sql_mode darf nur Modi aus einer festen Liste enthalten, die Zeichenketten, Bezeichner und Kommentare nicht anders lesen lassen (laut Dokumentation von MySQL 8.0 und MariaDB): ALLOW_INVALID_DATES, ERROR_FOR_DIVISION_BY_ZERO, HIGH_NOT_PRECEDENCE, IGNORE_SPACE, NO_AUTO_CREATE_USER, NO_AUTO_VALUE_ON_ZERO, NO_DIR_IN_CREATE, NO_ENGINE_SUBSTITUTION, NO_UNSIGNED_SUBTRACTION, NO_ZERO_DATE, NO_ZERO_IN_DATE, ONLY_FULL_GROUP_BY, PAD_CHAR_TO_FULL_LENGTH, PIPES_AS_CONCAT, REAL_AS_FLOAT, STRICT_ALL_TABLES, STRICT_TRANS_TABLES, TIME_TRUNCATE_FRACTIONAL, TRADITIONAL. ANSI, ANSI_QUOTES, DB2, MAXDB, MSSQL, ORACLE, POSTGRESQL und jeder unbekannte Modus sind unsicher. Das Beispiel mit ANSI ist unsicher (mit 82d2b1497 eine Ersetzung in der Zeichenkette).
- P1 Archiv (Regel 5): Ausgepackt wird nur jlawyerdb-dump.sql.zip oder jlawyerdb-dump-encrypted.sql.zip (genau eines muss vorhanden sein, sonst Abbruch), mit dem Passwort wie bisher über unzipWithPassword, ohne über unzip. Andere Archive im Sicherungsverzeichnis werden für die Datenbank nicht mehr ausgepackt. Sie haben dort auch vorher nichts beigetragen, IterativeBackupExecutor legt im Sicherungsverzeichnis nur dieses eine Archiv ab und löscht alte. Das Beispiel mit older.zip bricht ab.
- P2 Trigger mit NO_BACKSLASH_ESCAPES (Regel 2): NO_BACKSLASH_ESCAPES ist nur in der Zeile erlaubt, mit der mysqldump den Modus eines Triggers setzt, und nur, wenn direkt danach DELIMITER mit doppeltem Semikolon folgt und direkt nach dem Ende des Triggers der gesicherte Modus wiederhergestellt wird. Der Rumpf wird nie geändert. Die beiden echten Sicherungen unten enthalten einen solchen Trigger. Mit 82d2b1497 sind beide unsicher (-1), jetzt werden sie mit 24 Ersetzungen eingespielt.
- P2 Qualifier über das Zeilenende (Regel 6): Endet eine Zeile außerhalb von Zeichenketten und Kommentaren mit einem Punkt, ist das Ergebnis unsicher. Das Beispiel aus dem Review ist unsicher.
- P3 Antworten und pr.md (Regel 7): Die Antwort zu PRRT_kwDOCUQTLc6zRFZl nennt jetzt beide Bedingungen. In pr.md sind Fix (die Zeilen von Runde 3 zu den angenommenen Zeilen, zur Unsicherheit und zum Abbruch) und Nicht abgedeckt (früher Zeile 36) an die Regeln angepasst, der Umfang steht unter Nicht abgedeckt.

Ende-zu-Ende mit echten Sicherungen: MySQL 8.0.46 aus den Ubuntu-Paketen mysql-server-core-8.0 und mysql-client-core-8.0 (mit apt-get download geladen, mit dpkg -x ausgepackt, eigenes Datenverzeichnis, Port 3307, nicht installiert). Darin die j-lawyer-Datenbank der lokalen Testinstallation (101 Tabellen, Flyway 3.6.0.9, aus MariaDB übernommen), die 11 Tabellen mit utf8mb4 auf utf8mb4_0900_ai_ci umgestellt (document_name_tpls und assistant_prompts mit CONVERT TO, also auch alle Textspalten), dazu die Ansicht v_e2e mit COLLATE utf8mb4_0900_ai_ci, der Trigger trg_plain, der Trigger trg_nbe unter NO_BACKSLASH_ESCAPES mit der Zeichenkette aus einem Backslash und ein Datensatz mit dem Namen der Kollation in Groß- und Kleinschreibung, SET collation_connection, zwei DELIMITER-Zeilen, Backslash und Anführungszeichen. Ansicht und Trigger über eine Verbindung mit utf8mb4 angelegt, die Sicherungen setzen dort also collation_connection auf utf8mb4_0900_ai_ci. Gesichert mit mysqldump von MySQL 8.0.46 und mit mysqldump von MariaDB 10.11.14, beide nur mit -h, -P, -u und -r wie in IterativeBackupExecutor. Eingespielt mit RestoreExecutor.restore() in MariaDB 10.11.14, Datenbank e2erestore.

```
MySQL-8-mysqldump       24 Ersetzungen, 39 INSERT-Anweisungen identisch mit der Quelle, 102 Tabellen und Ansichten, trg_nbe mit NO_BACKSLASH_ESCAPES, trg_plain, keine Tabelle und Spalte mit 0900
MariaDB-mysqldump       24 Ersetzungen, 39 INSERT-Anweisungen identisch mit der Quelle, 102 Tabellen und Ansichten, trg_nbe mit NO_BACKSLASH_ESCAPES, trg_plain, keine Tabelle und Spalte mit 0900
trg_nbe nach dem Einspielen   INSERT mit schema_syntax abc ergibt abc und einen Backslash (Länge 4)
ANSI                    Abbruch (Kollation nicht sicher ersetzbar)
Trigger mit ANSI_QUOTES Abbruch (Kollation nicht sicher ersetzbar)
INSERT mit COLLATE      Abbruch (Kollation nicht sicher ersetzbar)
DELIMITER im Trigger    Abbruch (Kollation nicht sicher ersetzbar)
Qualifier am Zeilenende Abbruch (Kollation nicht sicher ersetzbar)
nur in older.zip        Abbruch (Datenbanksicherung nicht vollständig ausgepackt)
alte jlawyerdb-dump.sql Abbruch (Datenbanksicherung nicht vollständig ausgepackt)
```

Die Abbruchfälle sind die Sicherung von MySQL 8 mit je einer Änderung: eine Zeile SET sql_mode='ANSI' nach dem Kopf, NO_BACKSLASH_ESCAPES im Modus von trg_nbe durch ANSI_QUOTES ersetzt, eine INSERT-Zeile mit 'x' COLLATE utf8mb4_0900_ai_ci nach der ersten INSERT-Zeile, der Trigger aus Runde 3 mit DELIMITER in einer Zeichenkette, die Ansicht aus Runde 3 mit t. am Zeilenende. Dazu die unveränderte Sicherung nur in older.zip neben einem jlawyerdb-dump.sql.zip mit hinweis.txt, und als jlawyerdb-dump.sql neben einem solchen Archiv. In allen Abbruchfällen enthält das Datenverzeichnis weiter archivefiles/case1/brief.pdf mit dem Inhalt, e2erestore hat weiter 103 Tabellen samt Markierungstabelle e2e_marker mit dem Wert 42, und im Sicherungsverzeichnis liegt kein jlawyerdb-restore-… mehr. Daten verglichen mit mysqldump --skip-comments --no-create-info --skip-triggers --compact von Quelle (MySQL 8) und Ziel (MariaDB), INSERT-Anweisungen auf eine Zeile gebracht und _binary entfernt.

numstat gegen master 7b3b3dfa8:

```
                                  vorher 82d2b1497   nachher ca95debb9
RestoreExecutor.java              +86 -12            +102 -12
SqlDumpUtils.java (neu)           +1122              +1248
RestorePreparationTest.java (neu) +748               +757
SqlDumpUtilsTest.java (neu)       +987               +1031
gesamt                            +2943 -12          +3138 -12
ohne Lizenzkopf (662 Zeilen je neuer Datei)
Code                              546                688
Tests                             411                464
zusammen                          957                1152
```

Diff von 82d2b1497 auf ca95debb9: 277 Zeilen hinzu, 82 entfernt. Der Zuwachs kommt von der Prüfung der Tabellendaten beim Durchreichen, den Regeln für Trigger, der Liste der Modi und der Auswahl des Archivs. Im Gegenzug ist der Test zum Lesen großer Zeilen kürzer (er prüft nur noch den Vorlauf und die Länge, der Inhalt wird von den anderen Tests geprüft) und ein doppelter unsicherer Fall entfällt.

pr.md ist angepasst: Fix beschreibt das eine Archiv, die Regeln für Tabellendaten, sql_mode, Trigger und die übrigen Zeilen und alle Gründe für ein unsicheres Ergebnis. Verifiziert nennt die 14 Tests, die 6 Fehlschläge mit 82d2b1497 und den Ende-zu-Ende-Test mit echten Sicherungen von MySQL 8.0.46 und MariaDB-mysqldump. Unter Nicht abgedeckt stehen der Umfang, Trigger, die nicht angepasst werden, MySQL nur in 8.0.46 und nicht ausprobierte verschlüsselte Sicherungen. Der Punkt zu nachgebauten Sicherungen entfällt.

Endgültige Antworten je Thread:

- PRRT_kwDOCUQTLc6zRFYL: Behoben in 8932d6b1b, 7088e5caa, 82d2b1497 und ca95debb9: Die Datenbanksicherung wird nur aus dem Archiv der Datenbanksicherung (jlawyerdb-dump.sql.zip oder jlawyerdb-dump-encrypted.sql.zip, genau eines) und vor dem Leeren des Datenverzeichnisses in ein neu angelegtes Verzeichnis ausgepackt. Eine jlawyerdb-dump.sql aus einem anderen Archiv oder von einer früheren Wiederherstellung wird nie verwendet. Die Wiederherstellung bricht ab, bevor etwas gelöscht wird, wenn keines oder beide Archive vorhanden sind, wenn beim Auspacken eine Datei nicht geschrieben werden kann oder das Archiv keine Datenbanksicherung enthält, wenn das Schreiben der angepassten Kopie scheitert, und wenn der Zielserver die Kollation als unbekannt meldet, die Sicherung ihren Namen in beliebiger Schreibweise enthält und sie nicht sicher angepasst werden kann. Kennt der Zielserver die Kollation oder scheitert diese Prüfung, wird die Sicherung wie bisher unverändert eingespielt. Die Sicherung selbst bleibt unverändert, das Verzeichnis zum Auspacken wird danach gelöscht.
- PRRT_kwDOCUQTLc6zRFZF: Behoben in 8932d6b1b und 82d2b1497: Die angepasste Kopie wird mit den Rechten rw------- in einem Verzeichnis mit rwx------ angelegt, wo das Dateisystem POSIX-Rechte kennt. Das Ersetzen an Ort und Stelle gibt es nicht mehr, die Wiederherstellung hat es nie verwendet.
- PRRT_kwDOCUQTLc6zRFZl: Behoben in 82d2b1497 und ca95debb9: Der Wert einer collation_-Variable wird nur in SET-Anweisungen ersetzt, die am Anfang einer Anweisung stehen und nur aus einfachen Zuweisungen bestehen, wie mysqldump sie schreibt. Erwähnt eine andere Zeile collation_, steht der Name der Kollation an einer anderen Stelle als nach COLLATE oder endet eine Zeile mit einem Qualifier (t. vor einer Spalte in der nächsten Zeile), wird nichts ersetzt. Die Wiederherstellung bricht dann vor dem Löschen ab, wenn der Zielserver die Kollation als unbekannt meldet und die Sicherung ihren Namen enthält. Sonst wird die Sicherung unverändert eingespielt.
- PRRT_kwDOCUQTLc6zRFaL: Behoben in 7088e5caa, 82d2b1497 und ca95debb9: sql_mode wird nur in SET-Anweisungen wie denen von mysqldump akzeptiert, mit genau einem Literal aus einer festen Liste von Modi, die Zeichenketten, Bezeichner und Kommentare nicht anders lesen lassen. ANSI_QUOTES und die Sammelmodi, die es enthalten (ANSI, DB2, MAXDB, MSSQL, ORACLE, POSTGRESQL), stehen nicht in der Liste, ebenso jeder unbekannte Modus. Jede andere Zeile, die sql_mode erwähnt, macht das Ergebnis ebenfalls unsicher und es wird nichts ersetzt. Meldet der Zielserver die Kollation als unbekannt und enthält die Sicherung ihren Namen, bricht die Wiederherstellung dann vor dem Löschen ab. Kennt der Zielserver die Kollation, wird die Sicherung unverändert eingespielt wie ohne diesen PR.
- PRRT_kwDOCUQTLc6zRFbv: Behoben in 82d2b1497 und ca95debb9, ohne Versionen nachzubilden: sql_mode darf nur auf Modi aus dieser Liste gesetzt und nur aus einer Variablen wiederhergestellt werden, deren Name sql_mode enthält (wie @OLD_SQL_MODE und @saved_sql_mode bei mysqldump), jede andere Zeile mit sql_mode macht das Ergebnis unsicher. NO_BACKSLASH_ESCAPES ist nur als Modus eines Triggers erlaubt, genau so, wie mysqldump ihn schreibt (direkt vor dem Trigger gesetzt und direkt danach wiederhergestellt), und der Rumpf eines Triggers wird nie geändert. Damit hängt das Lesen von Zeichenketten außerhalb von Triggern nicht davon ab, welche versionsabhängigen Kommentare der Zielserver ausführt. Für ausführbare Kommentare gilt nur noch, dass eine Zeichenkette darin das Kommentarende nicht enthalten darf. Das Beispiel aus dem Review ist unsicher. Meldet der Zielserver die Kollation als unbekannt und enthält die Sicherung ihren Namen, bricht die Wiederherstellung dann vor dem Löschen ab.
- PRRT_kwDOCUQTLc6zRFbR und PRRT_kwDOCUQTLc6zRFcO: Die Ergänzungen aus Runde 3 gelten unverändert, kein neuer Text nötig.
