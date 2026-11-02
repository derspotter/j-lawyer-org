# Report 2592

- Branch: `fix/2592-restore-mariadb-collation` (fork derspotter/j-lawyer-org), base upstream/master 7b3b3dfa8
- Commits (newest first):
  - 198715355 derspotter <jayjag@posteo.de> | fix(backup): Kollation nur in den SQL-Anweisungen ersetzen und nur, wenn der Zielserver sie nicht kennt. Refs #2592
  - da04769b6 derspotter <jayjag@posteo.de> | fix(backup): Wiederherstellung in MariaDB ersetzt die MySQL-8-Kollation utf8mb4_0900_ai_ci. Closes #2592

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-backupmgr/src/main/java/org/jlawyer/backupmgr/impl/RestoreExecutor.java` | 33 | 0 |
| `j-lawyer-backupmgr/src/main/java/org/jlawyer/backupmgr/impl/SqlDumpUtils.java` | 771 | 0 |
| `j-lawyer-backupmgr/src/test/java/org/jlawyer/test/backupmgr/SqlDumpUtilsTest.java` | 766 | 0 |
| total | 1570 | 0 |

## Build and tests (02.11.2026)

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch and master 7b3b3dfa8 built with the identical command in the same container (master in a separate worktree):

```
mvn -B -ntp -pl j-lawyer-backupmgr -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| j-lawyer-backupmgr | 5 / 5 / 0 / 0 | n/a |
| total | 5 / 5 / 0 / 0 | 0 / 0 / 0 / 0 |

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
