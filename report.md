# Report #1779

- Branch: `fix/1779-ram-ueberwachung-ohne-cache` (fork derspotter/j-lawyer-org)
- Commit: `6d95e4071b6cbc1bec0056884dc0d72216ab8002`
- Subject: fix(server): RAM-Überwachung zählt den Seitencache unter Linux nicht mehr als belegt. Closes #1779
- Author: derspotter <jayjag@posteo.de>
- Base: upstream/master `7b3b3dfa8`

## Files (+/-)

| File | + | - |
|---|---|---|
| `j-lawyer-server-common/src/main/java/com/jdimension/jlawyer/server/utils/SystemMemoryUtils.java` | 742 | 0 |
| `j-lawyer-server-common/src/test/java/org/jlawyer/test/system/SystemMemoryUtilsTest.java` | 712 | 0 |
| `j-lawyer-server/j-lawyer-server-ejb/src/main/java/com/jdimension/jlawyer/services/SystemManagement.java` | 2 | 1 |
| `j-lawyer-server/j-lawyer-server-war/src/main/java/com/jdimension/jlawyer/timer/SystemMonitorTask.java` | 2 | 1 |
| total | 1458 | 2 |

## Build and tests vs. baseline

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink (`/opt/jdk17fx`). Maven 3.9.11.

```
mvn -B -ntp -pl j-lawyer-server/j-lawyer-server-ejb,j-lawyer-server/j-lawyer-server-war -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

Reactor result: BUILD SUCCESS (all modules compiled and packaged)

Baseline = the same command on upstream/master 7b3b3dfa8 (`-pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb,j-lawyer-backupmgr -am`).

| Module | branch | master |
|---|---|---|
| j-lawyer-cloud | tests=4 failures=0 errors=0 skipped=0 | tests=4 failures=0 errors=0 skipped=0 |
| j-lawyer-fax | tests=23 failures=0 errors=0 skipped=0 | tests=23 failures=0 errors=0 skipped=0 |
| j-lawyer-server-common | tests=108 failures=17 errors=0 skipped=0 | tests=104 failures=17 errors=0 skipped=0 |
| j-lawyer-server-entities | tests=56 failures=0 errors=0 skipped=0 | tests=56 failures=0 errors=0 skipped=0 |
| j-lawyer-server/j-lawyer-server-ejb | tests=829 failures=1 errors=0 skipped=16 | tests=829 failures=1 errors=0 skipped=16 |
| j-lawyer-server/j-lawyer-server-war | tests=5 failures=4 errors=0 skipped=0 | tests=5 failures=4 errors=0 skipped=0 |

```
TOTAL tests=1025 failures=22 errors=0 skipped=16
failing classes: ['j-lawyer-server-common FtpTest', 'j-lawyer-server-common SftpTest', 'j-lawyer-server-common SmbTest', 'j-lawyer-server/j-lawyer-server-ejb LibreOfficeODFTest', 'j-lawyer-server/j-lawyer-server-war BackupExecutorTest', 'j-lawyer-server/j-lawyer-server-war FileNameEncodingTest', 'j-lawyer-server/j-lawyer-server-war SyncTest']
new failures vs master: none
master failures not present: none
```

## Evidence
- Sanity run in this sandbox (container with cgroup memory limit) using the compiled `SystemMemoryUtils`: JVM total 13677 MB, JVM free 0 MB, new available 0 MB (fallback, because /proc/meminfo shows the host: MemTotal 16090 MB, MemAvailable 11370 MB). So the guard works as designed, and also shows that the limited-container case is NOT fixed by this PR.
- JDK 17 on Linux without container limit: `getFreePhysicalMemorySize()` comes from `sysinfo().freeram`, which excludes buffers/page cache. `MemAvailable` is the kernel's own estimate including reclaimable cache (since Linux 3.14).

## Unverified
- No run on a Linux host without memory limit (the reporter's setup). Covered by the unit test with a realistic /proc/meminfo sample.
- Windows/macOS: no /proc/meminfo, unchanged behaviour by construction.

## Open questions
- Limited containers would need cgroup v1/v2 parsing (memory.max, memory.current, memory.stat inactive_file), left out to keep it small.
- j-dimension's comment (2022) says the RAM check can be disabled individually, and calls the cause the "buffered" RAM. This PR keeps the check, it just stops counting the cache.
