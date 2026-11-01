# Report #restore-sql-fehler-erkennen

- Branch: `fix/restore-sql-fehler-erkennen` (fork derspotter/j-lawyer-org)
- Commit: `e256acae0e8396916a51d0e916ad750c314b40e4`
- Subject: fix(backup): Wiederherstellung meldet Fehler beim Einspielen der Datenbanksicherung, statt Erfolg anzuzeigen. Refs #2592
- Author: derspotter <jayjag@posteo.de>
- Base: upstream/master `7b3b3dfa8`

## Files (+/-)

| File | + | - |
|---|---|---|
| `j-lawyer-backupmgr/src/main/java/org/jlawyer/backupmgr/impl/RestoreExecutor.java` | 8 | 0 |
| total | 8 | 0 |

## Build and tests vs. baseline

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink (`/opt/jdk17fx`). Maven 3.9.11.

```
mvn -B -ntp -pl j-lawyer-backupmgr -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

Reactor result: BUILD SUCCESS (all modules compiled and packaged)

Baseline = the same command on upstream/master 7b3b3dfa8 (`-pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb,j-lawyer-backupmgr -am`).

| Module | branch | master |
|---|---|---|

```
TOTAL tests=0 failures=0 errors=0 skipped=0
failing classes: []
new failures vs master: none
master failures not present: none
```

## Evidence
- MariaDB 10.11.14 local, reflection on the real private `restoreDatabase()`, unencrypted `jlawyerdb-dump.sql.zip`:
  - master jar + dump with `utf8mb4_0900_ai_ci`: returns normally, `information_schema` shows 0 tables in jlawyerdb.
  - branch jar + same dump: `Exception: Datenbankwiederherstellung fehlgeschlagen: ERROR 1273 (HY000) at line 6 in file: '.../jlawyerdb-dump.sql': Unknown collation: 'utf8mb4_0900_ai_ci'`.
  - branch jar + clean dump (collation replaced by hand): returns normally, 1 table.
  - temporary merge of this branch with fix/2592 (not committed): MySQL 8 dump restored, table `cases` with `utf8mb4_unicode_ci`, no exception.
- Both callers surface the message: `BackupManagerLauncher` (CLI) prints "Wiederherstellung fehlgeschlagen: ..." and exits 1, the JavaFX controller sets it into `lblProgress`.

## Unverified
- MySQL 8 server/client as target. MySQL prints errors as `ERROR <code> (<state>) at line <n> in file: ...` as well, but not run here.

## Open questions
- No issue number of its own (found while fixing #2592), hence `Refs #2592`. Independent of fix/2592, `git merge-tree` clean.
- Detection is based on stderr lines starting with `ERROR`. A data line starting with "ERROR" can only appear in the echoed text of a statement that failed anyway.
