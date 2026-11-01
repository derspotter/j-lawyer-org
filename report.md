# Report #imap-timeouts-postfachscanner

- Branch: `fix/imap-timeouts-postfachscanner` (fork derspotter/j-lawyer-org)
- Commit: `a27e8348b1dc6c28a88433c0c49db5a25e63e8ab`
- Subject: fix(e-mail): Zeitlimits für IMAP-Verbindungen des Postfach-Scanners auf dem Server. Refs #1881
- Author: derspotter <jayjag@posteo.de>
- Base: upstream/master `7b3b3dfa8`

## Files (+/-)

| File | + | - |
|---|---|---|
| `j-lawyer-server/j-lawyer-server-war/src/main/java/com/jdimension/jlawyer/timer/MailboxScannerTask.java` | 5 | 0 |
| total | 5 | 0 |

## Build and tests vs. baseline

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink (`/opt/jdk17fx`). Maven 3.9.11.

```
mvn -B -ntp -pl j-lawyer-server/j-lawyer-server-war -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

Reactor result: BUILD SUCCESS (all modules compiled and packaged)

Baseline = the same command on upstream/master 7b3b3dfa8 (`-pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb,j-lawyer-backupmgr -am`).

| Module | branch | master |
|---|---|---|
| j-lawyer-cloud | tests=4 failures=0 errors=0 skipped=0 | tests=4 failures=0 errors=0 skipped=0 |
| j-lawyer-fax | tests=23 failures=0 errors=0 skipped=0 | tests=23 failures=0 errors=0 skipped=0 |
| j-lawyer-server-common | tests=104 failures=17 errors=0 skipped=0 | tests=104 failures=17 errors=0 skipped=0 |
| j-lawyer-server-entities | tests=56 failures=0 errors=0 skipped=0 | tests=56 failures=0 errors=0 skipped=0 |
| j-lawyer-server/j-lawyer-server-ejb | tests=829 failures=1 errors=0 skipped=16 | tests=829 failures=1 errors=0 skipped=16 |
| j-lawyer-server/j-lawyer-server-war | tests=5 failures=4 errors=0 skipped=0 | tests=5 failures=4 errors=0 skipped=0 |

```
TOTAL tests=1021 failures=22 errors=0 skipped=16
failing classes: ['j-lawyer-server-common FtpTest', 'j-lawyer-server-common SftpTest', 'j-lawyer-server-common SmbTest', 'j-lawyer-server/j-lawyer-server-ejb LibreOfficeODFTest', 'j-lawyer-server/j-lawyer-server-war BackupExecutorTest', 'j-lawyer-server/j-lawyer-server-war FileNameEncodingTest', 'j-lawyer-server/j-lawyer-server-war SyncTest']
new failures vs master: none
master failures not present: none
```

## Evidence
- Probe with javax.mail 1.6.2 against a local socket that never sends the IMAP greeting: without timeout properties `Store.connect()` was still blocked after 10 s. With `mail.imap.timeout=3000` it failed after ~3 s with a `MessagingException` (read timeout).
- `TransientTimer` schedules `MailboxScannerTask` on its own `java.util.Timer` (every 15 min). A `java.util.Timer` runs tasks on one thread, so a hanging run prevents all further runs.
- Values copied from `EmailService` (e9f331df2): connect 15 s, read 30 s.

## Unverified
- Not run against a real IMAP server.

## Open questions
- `SystemManagement.testReceiveMail()` also lacks IMAP timeouts but writes its properties into `System.getProperties()` (JVM wide) and uses `Session.getDefaultInstance()`. Left out on purpose, changing that needs a closer look.
- `Refs #1881`.
