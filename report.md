# Report 2592

- Branch: `fix/2592-restore-mariadb-collation` (fork derspotter/j-lawyer-org), base upstream/master 7b3b3dfa8
- Commits (newest first):
  - 0234d855d derspotter <jayjag@posteo.de> | fix(backup): Ersetzen der Kollation beachtet DELIMITER, SET-Anweisungen im Rumpf von Triggern und Prozeduren ändern den sql_mode nicht mehr. Refs #2592
  - 6b1a9d097 derspotter <jayjag@posteo.de> | fix(backup): Ersetzen der Kollation beachtet NO_BACKSLASH_ESCAPES in Triggern aus der Sicherung, Tests prüfen den Speicherbedarf bei großen Datensätzen. Refs #2592
  - 25b452efb derspotter <jayjag@posteo.de> | fix(backup): Kollation wird nur noch im SQL-Code ersetzt, nicht in Zeichenketten, Bezeichnern und Kommentaren, und die Sicherung wird ohne Zwischenspeichern ganzer Zeilen verarbeitet. Refs #2592
  - 198715355 derspotter <jayjag@posteo.de> | fix(backup): Kollation nur in den SQL-Anweisungen ersetzen und nur, wenn der Zielserver sie nicht kennt. Refs #2592
  - da04769b6 derspotter <jayjag@posteo.de> | fix(backup): Wiederherstellung in MariaDB ersetzt die MySQL-8-Kollation utf8mb4_0900_ai_ci. Closes #2592

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-backupmgr/src/main/java/org/jlawyer/backupmgr/impl/RestoreExecutor.java` | 33 | 0 |
| `j-lawyer-backupmgr/src/main/java/org/jlawyer/backupmgr/impl/SqlDumpUtils.java` | 1233 | 0 |
| `j-lawyer-backupmgr/src/test/java/org/jlawyer/test/backupmgr/SqlDumpUtilsTest.java` | 1108 | 0 |
| total | 2374 | 0 |

## Build and tests

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch at commit 0234d855d built (started 03.11.2026 12:54 UTC) and master 7b3b3dfa8 built (02.11.2026, in a separate worktree) with the identical command in the same container:

```
mvn -B -ntp -pl j-lawyer-backupmgr -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| j-lawyer-backupmgr | 15 / 15 / 0 / 0 | n/a |
| total | 15 / 15 / 0 / 0 | 0 / 0 / 0 / 0 |

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
