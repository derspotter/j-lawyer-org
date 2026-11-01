# Report #smtp-timeouts

- Branch: `fix/smtp-timeouts` (fork derspotter/j-lawyer-org)
- Commit: `ddafffc88e46d4428df4c3c2b3f9a532198c2f12`
- Subject: fix(e-mail): Zeitlimits für SMTP-Verbindungen, damit der Versand bei einem nicht antwortenden Mailserver nicht endlos hängt. Refs #1881
- Author: derspotter <jayjag@posteo.de>
- Base: upstream/master `7b3b3dfa8`

## Files (+/-)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/mail/SendAction.java` | 2 | 0 |
| `j-lawyer-server-common/src/main/java/com/jdimension/jlawyer/email/CommonMailUtils.java` | 26 | 0 |
| `j-lawyer-server-common/src/test/java/org/jlawyer/test/email/SmtpTimeoutsTest.java` | 685 | 0 |
| `j-lawyer-server/j-lawyer-server-ejb/src/main/java/com/jdimension/jlawyer/services/EmailService.java` | 2 | 0 |
| `j-lawyer-server/j-lawyer-server-ejb/src/main/java/com/jdimension/jlawyer/services/SystemManagement.java` | 3 | 0 |
| `j-lawyer-server/j-lawyer-server-ejb/src/main/java/org/jlawyer/async/OutgoingMailProcessor.java` | 2 | 0 |
| `j-lawyer-server/j-lawyer-server-war/src/main/java/com/jdimension/jlawyer/timer/SystemMonitorTask.java` | 2 | 0 |
| total | 722 | 0 |

## Build and tests vs. baseline

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink (`/opt/jdk17fx`). Maven 3.9.11.

```
mvn -B -ntp -pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb,j-lawyer-server/j-lawyer-server-war -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

Reactor result: BUILD SUCCESS (all modules compiled and packaged)

Baseline = the same command on upstream/master 7b3b3dfa8 (`-pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb,j-lawyer-backupmgr -am`).

| Module | branch | master |
|---|---|---|
| j-lawyer-client | tests=81 failures=0 errors=0 skipped=0 | tests=81 failures=0 errors=0 skipped=0 |
| j-lawyer-cloud | tests=4 failures=0 errors=0 skipped=0 | tests=4 failures=0 errors=0 skipped=0 |
| j-lawyer-fax | tests=23 failures=0 errors=0 skipped=0 | tests=23 failures=0 errors=0 skipped=0 |
| j-lawyer-server-common | tests=105 failures=17 errors=0 skipped=0 | tests=104 failures=17 errors=0 skipped=0 |
| j-lawyer-server-entities | tests=56 failures=0 errors=0 skipped=0 | tests=56 failures=0 errors=0 skipped=0 |
| j-lawyer-server/j-lawyer-server-ejb | tests=829 failures=1 errors=0 skipped=16 | tests=829 failures=1 errors=0 skipped=16 |
| j-lawyer-server/j-lawyer-server-war | tests=5 failures=4 errors=0 skipped=0 | tests=5 failures=4 errors=0 skipped=0 |

```
TOTAL tests=1103 failures=22 errors=0 skipped=16
failing classes: ['j-lawyer-server-common FtpTest', 'j-lawyer-server-common SftpTest', 'j-lawyer-server-common SmbTest', 'j-lawyer-server/j-lawyer-server-ejb LibreOfficeODFTest', 'j-lawyer-server/j-lawyer-server-war BackupExecutorTest', 'j-lawyer-server/j-lawyer-server-war FileNameEncodingTest', 'j-lawyer-server/j-lawyer-server-war SyncTest']
new failures vs master: none
master failures not present: none
```

## Evidence
- Probe with javax.mail 1.6.2 (the project's version) against a local socket that accepts and never sends the SMTP greeting: without timeout properties `Transport.connect()` was still blocked after 12 s. With `mail.smtp.timeout=2000` it failed after ~2 s with `SocketTimeoutException: Read timed out`.
- IMAP timeouts were added by j-dimension in e9f331df2 (2026-03-20, "timeouts for mailserver connections") in `EmailService` only.
- RFC 5321 section 4.5.3.2 recommends client timeouts of up to 10 minutes (reply after the end of the message data), hence 600 s read timeout instead of the 30 s used for IMAP.

## Call sites (all now call `CommonMailUtils.setSmtpTimeouts()` before custom properties)
- `OutgoingMailProcessor` (server send queue)
- `EmailService.testSmtpConnection()` and the second SMTP session in `EmailService`
- `SystemManagement` (two server notification paths)
- `SystemMonitorTask` (monitoring alert mails, war module)
- `SendAction` (client, direct SMTP path)

## Unverified
- Not run against a real SMTP server. A server that is slow but alive is unaffected as long as each reply comes within 10 minutes.

## Open questions
- No write timeout (`mail.smtp.writetimeout`), it needs an extra thread per connection in JavaMail.
- `Refs #1881` (mail client IPv6 / timeouts issue), it does not close it.
