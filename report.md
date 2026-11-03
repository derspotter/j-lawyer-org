# Report 2592

- Branch: `fix/2592-restore-mariadb-collation` (fork derspotter/j-lawyer-org), base upstream/master 7b3b3dfa8
- Commits (newest first):
  - 25b452efb derspotter <jayjag@posteo.de> | fix(backup): Kollation wird nur noch im SQL-Code ersetzt, nicht in Zeichenketten, Bezeichnern und Kommentaren, und die Sicherung wird ohne Zwischenspeichern ganzer Zeilen verarbeitet. Refs #2592
  - 198715355 derspotter <jayjag@posteo.de> | fix(backup): Kollation nur in den SQL-Anweisungen ersetzen und nur, wenn der Zielserver sie nicht kennt. Refs #2592
  - da04769b6 derspotter <jayjag@posteo.de> | fix(backup): Wiederherstellung in MariaDB ersetzt die MySQL-8-Kollation utf8mb4_0900_ai_ci. Closes #2592

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-backupmgr/src/main/java/org/jlawyer/backupmgr/impl/RestoreExecutor.java` | 33 | 0 |
| `j-lawyer-backupmgr/src/main/java/org/jlawyer/backupmgr/impl/SqlDumpUtils.java` | 1014 | 0 |
| `j-lawyer-backupmgr/src/test/java/org/jlawyer/test/backupmgr/SqlDumpUtilsTest.java` | 892 | 0 |
| total | 1939 | 0 |

## Build and tests (03.11.2026)

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch and master 7b3b3dfa8 built with the identical command in the same container (master in a separate worktree):

```
mvn -B -ntp -pl j-lawyer-backupmgr -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| j-lawyer-backupmgr | 9 / 9 / 0 / 0 | n/a |
| total | 9 / 9 / 0 / 0 | 0 / 0 / 0 / 0 |

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
