# Report #2592

- Branch: `fix/2592-restore-mariadb-collation` (fork derspotter/j-lawyer-org)
- Commit: `da04769b65232ac015b53844319c8c428f5ac5cc`
- Subject: fix(backup): Wiederherstellung in MariaDB ersetzt die MySQL-8-Kollation utf8mb4_0900_ai_ci. Closes #2592
- Author: derspotter <jayjag@posteo.de>
- Base: upstream/master `7b3b3dfa8`

## Files (+/-)

| File | + | - |
|---|---|---|
| `j-lawyer-backupmgr/src/main/java/org/jlawyer/backupmgr/impl/RestoreExecutor.java` | 22 | 0 |
| `j-lawyer-backupmgr/src/main/java/org/jlawyer/backupmgr/impl/SqlDumpUtils.java` | 746 | 0 |
| `j-lawyer-backupmgr/src/test/java/org/jlawyer/test/backupmgr/SqlDumpUtilsTest.java` | 748 | 0 |
| total | 1516 | 0 |

## Build and tests vs. baseline

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink (`/opt/jdk17fx`). Maven 3.9.11.

```
mvn -B -ntp -pl j-lawyer-backupmgr -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

Reactor result: BUILD SUCCESS (all modules compiled and packaged)

Baseline = the same command on upstream/master 7b3b3dfa8 (`-pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb,j-lawyer-backupmgr -am`).

| Module | branch | master |
|---|---|---|
| j-lawyer-backupmgr | tests=4 failures=0 errors=0 skipped=0 | n/a |

```
TOTAL tests=4 failures=0 errors=0 skipped=0
failing classes: []
new failures vs master: none
master failures not present: none
```

## Evidence
- MariaDB 10.11.14 (apt, local), hand-written dump in mysqldump 8.0 style (`COLLATE utf8mb4_0900_ai_ci` on columns and table). Plain `mysql -e "...;source dump;"`: ERROR 1273 Unknown collation, exit code 0.
- E2E via reflection on the real private `restoreDatabase()` (RestoreExecutor constructor with host 127.0.0.1:3306, db jlawyerdb, unencrypted `jlawyerdb-dump.sql.zip`): master jar -> ERROR 1273, table missing, method returns normally. Branch jar -> progress "Kollation ... ersetzt (3 Stellen)", table `cases` with `utf8mb4_unicode_ci`, 2 rows, umlaut bytes C3 BC intact.
- MariaDB product version via Connector/J: `5.5.5-10.11.14-MariaDB-...`, contains "mariadb" (that is what `isMariaDb()` checks).

## Unverified
- MySQL 8 as target (no MySQL server here). `isMariaDb()` returns false there, the dump is not touched.
- Windows path handling: the dump path is built before the existing backslash replacement for the `source` command, `java.io.File` accepts it as is.

## Open questions
- Only `utf8mb4_0900_ai_ci` is mapped, exactly as in j-dimension's issue text. Other `utf8mb4_0900_*` collations would only appear if set explicitly.
- Found while testing: `mysql` exits 0 when statements inside `source` fail, so a broken restore is reported as success. Not changed here (separate topic). Could be addressed with `--abort-source-on-error` (verified in the MariaDB 10.11 client here, support in the MySQL 8 client not checked), needs a decision. Error detection itself is now on its own branch fix/restore-sql-fehler-erkennen (notes/restore-sql-fehler-erkennen), independent of this one.
- New files carry the full AGPL header like the other recent files, hence the large +lines count.
