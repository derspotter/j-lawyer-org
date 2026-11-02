# Report restore-sql-fehler-erkennen

- Branch: `fix/restore-sql-fehler-erkennen` (fork derspotter/j-lawyer-org), base upstream/master 7b3b3dfa8
- Commits (newest first):
  - e256acae0 derspotter <jayjag@posteo.de> | fix(backup): Wiederherstellung meldet Fehler beim Einspielen der Datenbanksicherung, statt Erfolg anzuzeigen. Refs #2592

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-backupmgr/src/main/java/org/jlawyer/backupmgr/impl/RestoreExecutor.java` | 8 | 0 |
| total | 8 | 0 |

## Build and tests (02.11.2026)

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch and master 7b3b3dfa8 built with the identical command in the same container (master in a separate worktree):

```
mvn -B -ntp -pl j-lawyer-backupmgr -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| total | 0 / 0 / 0 / 0 | 0 / 0 / 0 / 0 |

Failing tests: identical to master

- none

## Antwort auf Review 02.11.
1. Build comparison: fixed. Branch and master were rebuilt on 02.11. with the identical command `-pl j-lawyer-backupmgr -am`, which builds only the parent and the backup manager, the comparison covers exactly these. "Alle Module" in the PR now refers to the selected reactor. No code change.

## Evidence (01.11., MariaDB 10.11.14 local, reflection on the real private `restoreDatabase()`)
- master jar + dump with `utf8mb4_0900_ai_ci`: returns normally, 0 tables in jlawyerdb.
- branch jar + same dump: `Datenbankwiederherstellung fehlgeschlagen: ERROR 1273 (HY000) at line 6 in file: '.../jlawyerdb-dump.sql': Unknown collation: 'utf8mb4_0900_ai_ci'`.
- branch jar + clean dump: returns normally, 1 table.
- temporary merge with fix/2592 (not committed): MySQL 8 dump restored completely.
- The code of this branch is unchanged since then, the build was repeated on 02.11.
