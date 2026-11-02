# Report 1779

- Branch: `fix/1779-ram-ueberwachung-ohne-cache` (fork derspotter/j-lawyer-org), base upstream/master 7b3b3dfa8
- Commits (newest first):
  - 8937c09fe derspotter <jayjag@posteo.de> | fix(server): RAM-Überwachung fällt auch ohne Lesezugriff auf /proc/meminfo auf den JVM-Wert zurück, Tests für den Rückfall. Refs #1779
  - 6d95e4071 derspotter <jayjag@posteo.de> | fix(server): RAM-Überwachung zählt den Seitencache unter Linux nicht mehr als belegt. Closes #1779

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-server-common/src/main/java/com/jdimension/jlawyer/server/utils/SystemMemoryUtils.java` | 753 | 0 |
| `j-lawyer-server-common/src/test/java/org/jlawyer/test/system/SystemMemoryUtilsTest.java` | 763 | 0 |
| `j-lawyer-server/j-lawyer-server-ejb/src/main/java/com/jdimension/jlawyer/services/SystemManagement.java` | 2 | 1 |
| `j-lawyer-server/j-lawyer-server-war/src/main/java/com/jdimension/jlawyer/timer/SystemMonitorTask.java` | 2 | 1 |
| total | 1520 | 2 |

## Build and tests (02.11.2026)

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch and master 7b3b3dfa8 built with the identical command in the same container (master in a separate worktree):

```
mvn -B -ntp -pl j-lawyer-server/j-lawyer-server-ejb,j-lawyer-server/j-lawyer-server-war -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| j-lawyer-cloud | 4 / 4 / 0 / 0 | 4 / 4 / 0 / 0 |
| j-lawyer-fax | 23 / 23 / 0 / 0 | 23 / 23 / 0 / 0 |
| j-lawyer-server-common | 112 / 112 / 0 / 17 | 104 / 104 / 0 / 17 |
| j-lawyer-server-entities | 56 / 56 / 0 / 0 | 56 / 56 / 0 / 0 |
| j-lawyer-server/j-lawyer-server-ejb | 829 / 813 / 16 / 1 | 829 / 813 / 16 / 1 |
| j-lawyer-server/j-lawyer-server-war | 5 / 5 / 0 / 4 | 5 / 5 / 0 / 4 |
| total | 1029 / 1013 / 16 / 22 | 1021 / 1005 / 16 / 22 |

Failing tests: identical to master

- j-lawyer-server-common FtpTest#testCopy
- j-lawyer-server-common FtpTest#testCreateDeleteDirectory
- j-lawyer-server-common FtpTest#testDelete
- j-lawyer-server-common FtpTest#testIsDirectory
- j-lawyer-server-common FtpTest#testIsFile
- j-lawyer-server-common FtpTest#testTraverse
- j-lawyer-server-common SftpTest#testCopy
- j-lawyer-server-common SftpTest#testCreateDeleteDirectory
- j-lawyer-server-common SftpTest#testDelete
- j-lawyer-server-common SftpTest#testIsDirectory
- j-lawyer-server-common SftpTest#testIsFile
- j-lawyer-server-common SftpTest#testTraverse
- j-lawyer-server-common SmbTest#testCopy
- j-lawyer-server-common SmbTest#testCreateDeleteDirectory
- j-lawyer-server-common SmbTest#testDelete
- j-lawyer-server-common SmbTest#testIsFile
- j-lawyer-server-common SmbTest#testTraverse
- j-lawyer-server/j-lawyer-server-ejb LibreOfficeODFTest#setReplaceEmptyLine
- j-lawyer-server/j-lawyer-server-war BackupExecutorTest#testBackupEncrypted
- j-lawyer-server/j-lawyer-server-war BackupExecutorTest#testBackupUnEncrypted
- j-lawyer-server/j-lawyer-server-war FileNameEncodingTest#testZip4JFileNameEncoding
- j-lawyer-server/j-lawyer-server-war SyncTest#testSync

## Antwort auf Review 02.11.
1. [P2] Access errors bypass the fallback: agreed, fixed in 8937c09fe. `Files.isReadable()` and reading are inside the `try`, any exception (incl. `SecurityException`) falls back to the JVM value. A SecurityManager test was not added (deprecated in Java 17 and fragile under surefire), instead an unreadable path (a directory) is tested through the new overload `getAvailablePhysicalMemory(osBean, Path)`.
2. [P2] Baseline evidence: fixed. Branch and master were rebuilt on 02.11. with the identical command `-pl j-lawyer-server/j-lawyer-server-ejb,j-lawyer-server/j-lawyer-server-war -am`, which builds the WAR module on both.
3. [P2] Counts and failure causes: fixed, total / executed / skipped / failed, and the prerequisites of the WAR tests are taken from their sources (`BackupExecutorTest`: local directories and MySQL for mysqldump, `FileNameEncodingTest`: local template files, `SyncTest.testSync`: local backups and SFTP).
4. [P3] Fallback coverage: agreed, added four tests that call `getAvailablePhysicalMemory()` itself with a proxy `OperatingSystemMXBean` (MemAvailable used, file missing, file unreadable, container with memory limit).
