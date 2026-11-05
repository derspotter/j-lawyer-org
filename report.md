# Report restore-sql-fehler-erkennen

- Branch: `fix/restore-sql-fehler-erkennen` (fork derspotter/j-lawyer-org), base upstream/master 7b3b3dfa8
- Commits (newest first):
  - f7aa51e4b derspotter <jayjag@posteo.de> | fix(backup): Nach einem Fehler beim Einspielen der Datenbanksicherung kann die Wiederherstellung abgebrochen oder fortgesetzt werden. Fortsetzen spielt die Sicherung bis zum Ende ein und überspringt Anweisungen mit Fehlern, am Ende werden alle Fehler mit dem Pfad der ausgepackten Sicherung aufgelistet. Die Oberfläche fragt per Dialog, die Konsole fragt nach oder setzt mit --ignore-db-errors ohne Rückfrage fort. Refs #2592
  - e256acae0 derspotter <jayjag@posteo.de> | fix(backup): Wiederherstellung meldet Fehler beim Einspielen der Datenbanksicherung, statt Erfolg anzuzeigen. Refs #2592

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-backupmgr/src/main/java/org/jlawyer/backupmgr/BackupManagerLauncher.java` | 42 | 0 |
| `j-lawyer-backupmgr/src/main/java/org/jlawyer/backupmgr/controller/BackupMgrController.java` | 57 | 1 |
| `j-lawyer-backupmgr/src/main/java/org/jlawyer/backupmgr/impl/DatabaseImport.java` | 840 | 0 |
| `j-lawyer-backupmgr/src/main/java/org/jlawyer/backupmgr/impl/RestoreExecutor.java` | 31 | 29 |
| `j-lawyer-backupmgr/src/test/java/org/jlawyer/backupmgr/BackupManagerLauncherTest.java` | 694 | 0 |
| `j-lawyer-backupmgr/src/test/java/org/jlawyer/backupmgr/impl/DatabaseImportTest.java` | 869 | 0 |
| total | 2533 | 30 |

## Build and tests

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch at commit f7aa51e4b built (started 05.11.2026 08:31 UTC) and master d34cd0531 built (05.11.2026, in a separate worktree) with the identical command in the same container:

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
1. Build comparison: fixed. Branch and master were rebuilt on 02.11. with the identical command `-pl j-lawyer-backupmgr -am`, which builds only the parent and the backup manager, the comparison covers exactly these. "Alle Module" in the PR now refers to the selected reactor. No code change.

## Evidence for e256acae0 (01.11., MariaDB 10.11.14 local, reflection on the real private `restoreDatabase()`)
- master jar + dump with `utf8mb4_0900_ai_ci`: returns normally, 0 tables in jlawyerdb.
- branch jar + same dump: `Datenbankwiederherstellung fehlgeschlagen: ERROR 1273 (HY000) at line 6 in file: '.../jlawyerdb-dump.sql': Unknown collation: 'utf8mb4_0900_ai_ci'`.
- branch jar + clean dump: returns normally, 1 table.
- temporary merge with fix/2592 (not committed): MySQL 8 dump restored completely.
- Superseded by the extension below, kept as history.

## Antwort auf Jens 04.11.
Spec: Jens' PR comment of 04.11. 12:36 UTC (abort or continue on the first ERROR, GUI dialog, console prompt or `--ignore-db-errors`, continue must really import to the end, collect all errors, final message "mit Fehlern abgeschlossen", name the path of the unpacked dump). Implemented in f7aa51e4b on top of e256acae0, no rebase, no force-push.

### How the mysql client handles an error in a sourced dump (measured 05.11.)
Command as in `restoreDatabase()`: `mysql ... -e` with the statements `drop database if exists ft`, `create database ft`, `use ft`, `source dump.sql` and `commit`, dump with 3 good inserts around a `CREATE TABLE ... COLLATE=utf8mb4_bogus_ci` and an insert into that table.

| client -> server | options | exit | stops at first error | rows imported |
|---|---|---|---|---|
| MariaDB 10.11.14 -> MariaDB 10.11.14 | none | 0 | no, both errors reported | 1,2,3 |
| MariaDB 10.11.14 -> MySQL 8.0.46 | none | 0 | no | 1,2,3 |
| MySQL 8.0.46 -> MySQL 8.0.46 | none | 1 | yes | 1 |
| MySQL 8.0.46 -> MariaDB 10.11.14 | none | 1 | yes | 1 |
| MySQL 8.0.46 -> MySQL 8.0.46 | `--force -e ...` | 1 | yes | 1 |
| MySQL 8.0.46 -> MySQL 8.0.46 | `-e ... --force` | 0 | no, both errors reported | 1,2,3 |
| MySQL 8.0.46 -> MariaDB 10.11.14 | `-e ... --force` | 0 | no | 1,2,3 |
| MariaDB 10.11.14 -> MariaDB 10.11.14 | `-e ... --force` | 0 | no | 1,2,3 |

So the answer to "macht der Client mit -e nach dem ersten Fehler weiter": the MariaDB client does, the MySQL client does not, and `--force` only helps the MySQL client when it comes after `-e` (its `-e` handling resets `ignore_errors` for the first `-e`). Both clients write `ERROR <code> (<state>) at line <n> in file: '<path>': <message>` for statements of the sourced file. With `--force` both exit with 0 even after errors. This also corrects the earlier PR text ("der Exit-Code bleibt 0"), which is only true for the MariaDB client: e256acae0 with the MySQL client reported "Datenbankwiederherstellung fehlgeschlagen!" without the error, because the exit code check came first (E2E run 11 below).

### Design
- New class `DatabaseImport` (impl package) holds the decision, `RestoreExecutor.restoreDatabase()` only calls it with the existing command line. `importDump()`:
  1. first run without `--force` (or with it when `--ignore-db-errors` is set)
  2. if the first error line names the dump file (`in file: '...'`) and a handler is set and it answers "continue": if the client exited non-zero (it stopped, MySQL client), a second run with `--force` appended at the end. If it exited with 0 (MariaDB client, already at the end of the dump), no second run.
  3. any other first error (login refused, drop database failed, no handler, answer "abort") throws "Datenbankwiederherstellung fehlgeschlagen: <error>" plus the dump path, as before plus the path.
  4. a non-zero exit of the last run throws (with `--force` the client only stops on errors it can not skip, e.g. a lost connection), reporting the last error line.
  5. otherwise returns all error lines of the last run, `RestoreExecutor.getDatabaseErrors()` exposes them.
- Second run and double application: every run executes `drop database if exists X`, `create database X` and `use X` before `source`, so the second run starts from an empty database and no statement of the first run is applied twice. The dump of the server backup is a plain `mysqldump -h -P -u -p -r <file> <database>` (`IterativeBackupExecutor`): no `--databases`, so no `CREATE DATABASE`/`USE`, and with default options it only holds objects of that database (tables, views, triggers), which the drop removes. Even for a dump made with `--databases`, mysqldump writes `DROP TABLE IF EXISTS` before each `CREATE TABLE`. Cost: the dump is imported twice up to the first error with the MySQL client. E2E run 3: 2 rows in `cases` after the second run (primary key, a second insert would have failed with 1062 and been listed, it was not).
- Process handling: `ProcessBuilder(...).redirectErrorStream(true)` instead of reading stdout to the end and then stderr. The MariaDB client echoes each failing statement in full to stderr, with many failing or large statements the old order could block the client on a full stderr pipe while the backup manager waits for stdout. `DatabaseImportTest#testRunClient` writes about 220 KB to stderr before anything else and checks the run completes.
- Error lines: `^ERROR \d+`, so `mysql: [Warning] ...` and the MariaDB statement echo do not count.
- Dump path: taken from the client's error line (`in file: '<path>'`), the file the client really read. On master that is `<backup dir>/jlawyerdb-dump.sql` (unpacked by `restoreFromTo(backupDirectory, backupDirectory)`), and it stays there after the restore as before. Taking it from the error line instead of building it again keeps `restoreDatabase()` free of `backup` references, so the merge with #3767 (which removes that variable) compiles.
- Messages (built and tested in `DatabaseImport`): question with the error, what abort and continue mean, and the dump path. Final message "Wiederherstellung mit Fehlern abgeschlossen. Beim Einspielen der Datenbanksicherung sind N Fehler aufgetreten, die betroffenen Anweisungen wurden übersprungen:" followed by every error line and the dump path.
- GUI (`BackupMgrController`): `continueAfterDatabaseError()` shows a WARNING `Alert` with "Abbrechen" (cancel/close) and "Fehler ignorieren und fortsetzen" on the FX thread via `FutureTask` and blocks the restore thread until answered, closing aborts. At the end, the label says "Wiederherstellung mit Fehlern abgeschlossen." and a second `Alert` shows the full message in a read-only `TextArea`. No FXML change.
- Console (`BackupManagerLauncher`): `--ignore-db-errors` (also `-ignore-db-errors`, like `-console`/`-force`, case-insensitive) sets `RestoreExecutor.setIgnoreDatabaseErrors(true)`. Otherwise `continueAfterDatabaseError()` prints the message, a hint to the parameter and "Fehler ignorieren und fortsetzen J/N", reads with the same `Scanner` as the existing confirmation. End of input (unattended run without the parameter) aborts as before. `-force` still only skips the first confirmation. Exit codes: 0 success, 1 failed (unchanged), 2 completed with errors (new, so unattended runs can tell). The GUI ignores the parameter and always asks.
- Decision points I took without asking (easy to change): exit code 2, `-ignore-db-errors` accepted as well, "continue" is the default button of the dialog (Enter), and errors outside the dump are not offered for continuing.

### Merge with #3767 (fix/2592-restore-mariadb-collation, 63669b13f)
- `git merge-tree --write-tree origin/fix/2592-restore-mariadb-collation f7aa51e4b`: clean. With upstream/master d34cd0531: clean. #3767 changes `restore()`, the first half of `restoreDatabase()` and adds a method at the end of the class, this branch adds fields and accessors elsewhere and replaces only the process part of `restoreDatabase()`.
- Merged state built in a separate worktree (temporary merge commit, not pushed) with the build command below: BUILD SUCCESS, 35 tests, 0 failures (`SqlDumpUtilsTest` 18, `RestorePreparationTest` 3, `DatabaseImportTest` 12, `BackupManagerLauncherTest` 2).
- Open point, needs a decision by Jens: #3767 unpacks the dump into `<backup dir>/jlawyerdb-restore-<n>/` and deletes that directory in the `finally` of `restore()`. In the merged state the messages name the right file (E2E run 10: `.../backup/jlawyerdb-restore-4904756035126990099/jlawyerdb-dump.sql`), but it is gone when the restore ends, so point 4 of the request does not hold any more. Options:
  - (a) keep the staging directory when the database import reported errors (continued or aborted after a dump error) and delete it otherwise. The unencrypted dump then stays on disk until the user deletes it, which is still less than master today (it always stays in the backup directory).
  - (b) delete it as #3767 does and name the archive instead, the user unpacks it again to correct it.
  I would do (a), it is a few lines in `restore()` of whichever PR is merged second. Not done here because it only exists together with #3767.

### E2E (05.11., console, `java -jar j-lawyer-backupmgr.jar -console`, answers on stdin, fresh backup and data directory per run)
Backup: `.metadata/encoding.UTF-8`, the required directories, `archivefiles/case1/case1.zip` with one document, `jlawyerdb-dump.sql.zip` with `cases` (2 rows), `broken` (`COLLATE=utf8mb4_bogus_ci`, plus one insert), `contacts` (3 rows). Clients: MariaDB 10.11.14 from Ubuntu, MySQL 8.0.46 client from the Ubuntu packages put first in `PATH`.

| run | jar | client -> server | answer | exit | result |
|---|---|---|---|---|---|
| 1 | master d34cd0531 | MySQL 8 -> MySQL 8 | - | 1 | "Datenbankwiederherstellung fehlgeschlagen!", no error text, `contacts` missing |
| 5 | master d34cd0531 | MariaDB -> MariaDB | - | 0 | "Backup erfolgreich wiederhergestellt." despite 2 errors |
| 11 | e256acae0 | MySQL 8 -> MySQL 8 | - | 1 | "Datenbankwiederherstellung fehlgeschlagen!", no error text, `contacts` missing |
| 12 | e256acae0 | MariaDB -> MariaDB | - | 1 | "fehlgeschlagen: ERROR 1273 ...", DB complete except `broken` |
| 2 | branch | MySQL 8 -> MySQL 8 | n | 1 | question with error and path, then "fehlgeschlagen: ERROR 1273 ..." with path, `contacts` missing |
| 3 | branch | MySQL 8 -> MySQL 8 | j | 2 | second run with `--force`, "mit Fehlern abgeschlossen", 2 errors (1273, 1146) and path, `cases` 2 rows, `contacts` 3 rows |
| 4 | branch | MySQL 8 -> MySQL 8 | `--ignore-db-errors` | 2 | no question, one run, same result as 3 |
| 6 | branch | MariaDB -> MariaDB | j | 2 | no second run, same list and DB as 3 |
| 7 | branch | MariaDB -> MariaDB | n | 1 | "fehlgeschlagen: ERROR 1273 ..." with path |
| 8 | branch | MariaDB -> MariaDB | no answer (end of input) | 1 | "- Abbruch", failed as before |
| 9 | branch | MySQL 8 -> MySQL 8 | clean dump | 0 | "Backup erfolgreich wiederhergestellt." |
| 10 | merge with #3767 | MySQL 8 -> MySQL 8 | j | 2 | as 3, path is the staging directory, which no longer exists afterwards |

In every run the document from `archivefiles` was restored before the database step.

### Tests
- `DatabaseImportTest` (12) and `BackupManagerLauncherTest` (2) test new code: on e256acae0 they do not compile, there is no meaningful red run. The behaviour change against e256acae0 and master is shown by the E2E runs 1, 5, 11 and 12 with the same dump.
- `testRunClient` needs `/bin/sh` (skipped via `Assume` otherwise).
- The baseline is a fresh build of upstream/master d34cd0531. j-lawyer-backupmgr is unchanged between the branch base 7b3b3dfa8 and d34cd0531, the root pom only changed dependency versions the backup manager does not use (commons-net, httpclient). Master runs 0 tests in this module (the only existing test class has its `@Test` commented out).

### Not tested
- GUI dialogs (no display here), Windows and macOS paths, encrypted backups, MySQL 5.7 clients.
