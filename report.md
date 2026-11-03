# Report smtp-timeouts

- Branch: `fix/smtp-timeouts` (fork derspotter/j-lawyer-org), base upstream/master 7b3b3dfa8
- Commits (newest first):
  - 4216d4264 derspotter <jayjag@posteo.de> | fix(e-mail): wirkungslose Zeile im auskommentierten SMTP-Code des Clients wieder entfernt. Refs #1881
  - da8e9283a derspotter <jayjag@posteo.de> | fix(e-mail): Zeitlimits für SMTP-Verbindungen, damit der Versand bei einem nicht antwortenden Mailserver nicht endlos hängt. Refs #1881

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-server-common/src/main/java/com/jdimension/jlawyer/email/CommonMailUtils.java` | 25 | 0 |
| `j-lawyer-server-common/src/test/java/org/jlawyer/test/email/SmtpTimeoutsTest.java` | 685 | 0 |
| `j-lawyer-server/j-lawyer-server-ejb/src/main/java/com/jdimension/jlawyer/services/EmailService.java` | 2 | 0 |
| `j-lawyer-server/j-lawyer-server-ejb/src/main/java/com/jdimension/jlawyer/services/SystemManagement.java` | 3 | 0 |
| `j-lawyer-server/j-lawyer-server-ejb/src/main/java/org/jlawyer/async/OutgoingMailProcessor.java` | 2 | 0 |
| `j-lawyer-server/j-lawyer-server-war/src/main/java/com/jdimension/jlawyer/timer/SystemMonitorTask.java` | 2 | 0 |
| total | 719 | 0 |

## Build and tests (02.11.2026)

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch and master 7b3b3dfa8 built with the identical command in the same container (master in a separate worktree):

```
mvn -B -ntp -pl j-lawyer-server/j-lawyer-server-ejb,j-lawyer-server/j-lawyer-server-war -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| j-lawyer-cloud | 4 / 4 / 0 / 0 | 4 / 4 / 0 / 0 |
| j-lawyer-fax | 23 / 23 / 0 / 0 | 23 / 23 / 0 / 0 |
| j-lawyer-server-common | 105 / 105 / 0 / 17 | 104 / 104 / 0 / 17 |
| j-lawyer-server-entities | 56 / 56 / 0 / 0 | 56 / 56 / 0 / 0 |
| j-lawyer-server/j-lawyer-server-ejb | 829 / 813 / 16 / 1 | 829 / 813 / 16 / 1 |
| j-lawyer-server/j-lawyer-server-war | 5 / 5 / 0 / 4 | 5 / 5 / 0 / 4 |
| total | 1022 / 1006 / 16 / 22 | 1021 / 1005 / 16 / 22 |

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
1. Baseline command: fixed. Branch and master were rebuilt on 02.11. with the identical command `-pl j-lawyer-server/j-lawyer-server-ejb,j-lawyer-server/j-lawyer-server-war -am`. The client is no longer changed by this branch (see 2), so it is not part of the selection.
2. SendAction call site in commented-out code: agreed, the line sat inside the block comment "Legacy SMTP send path - removed". Removed together with the unused import in 4216d4264, the branch no longer changes the client. The PR now names the six active server call sites. While checking this I also corrected the description of `OutgoingMailProcessor`: it is the JMS bean for system notification mails, not the send queue for user mails (those go synchronously through `EmailService.smtpSendMail()`).
3. Counts and failure causes: fixed, total / executed / skipped / failed and the WAR test prerequisites taken from their sources.
4. Library version: agreed. The probe was repeated with `com.sun.mail:jakarta.mail:1.6.8` (server) and shows the same behaviour: blocked after 12 s without timeouts, `SocketTimeoutException` after 2 s with `mail.smtp.timeout=2000`. How the probe works and its output are below (the Java source itself is not quoted because the notes must not contain semicolons).

## Probe
`java -cp jakarta.mail-1.6.8.jar SmtpProbe.java` (single-file source launch, JDK 17). The probe opens a local `ServerSocket` that accepts connections and never sends the SMTP greeting. It then calls `Transport.connect()` for protocol `smtp` against that port twice in a separate thread and waits at most 12 s each time. The first run uses only `mail.smtp.host` and `mail.smtp.port`, the second adds `mail.smtp.connectiontimeout=30000` and `mail.smtp.timeout=2000`. Output:
- `javax.mail implementation: file:.../com/sun/mail/jakarta.mail/1.6.8/jakarta.mail-1.6.8.jar`
- `without timeouts: still blocked after 12005 ms`
- `with timeouts: failed after 2006 ms: java.net.SocketTimeoutException: Read timed out`

## Merge mit master 03.11.

Merge-Commit: fdfb55f62 (Eltern 4216d4264 und upstream/master ac2aa7a62), kein Rebase, kein Force-Push.

Konflikt nur in j-lawyer-server/j-lawyer-server-war/src/main/java/com/jdimension/jlawyer/timer/SystemMonitorTask.java, und dort nur im Import-Block: Dieser Branch fügt import com.jdimension.jlawyer.email.CommonMailUtils hinzu (für CommonMailUtils.setSmtpTimeouts in sendMail), 70b84085f (Berechtigungen für Server-Einstellungen) an derselben Stelle import com.jdimension.jlawyer.server.services.settings.ServerSettingsKeys. Beide Importe übernommen. Die übrigen Änderungen von 70b84085f in der Datei (Schlüssel als Konstanten aus ServerSettingsKeys) und der Aufruf von setSmtpTimeouts überschneiden sich nicht und sind unverändert im Ergebnis. SystemManagement.java wurde von git ohne Konflikt zusammengeführt. Gegenüber upstream/master ändert der Merge genau die 6 Dateien dieses Branches mit denselben 719 Zeilen wie vorher gegenüber 7b3b3dfa8, das Verhalten ist unverändert, pr.md bleibt wie es ist.

Build (Stand fdfb55f62, gestartet 03.11.2026 20:47 UTC, Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9, Maven 3.9.11):

```
mvn -B -ntp -pl j-lawyer-server/j-lawyer-server-war -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

Ergebnis: BUILD SUCCESS, alle Module des Reactors bauen (mit -am auch j-lawyer-server-ejb).

| Modul | Tests | fehlgeschlagen | übersprungen |
|---|---|---|---|
| j-lawyer-cloud | 4 | 0 | 0 |
| j-lawyer-fax | 23 | 0 | 0 |
| j-lawyer-server-common | 115 | 17 | 0 |
| j-lawyer-server-entities | 56 | 0 | 0 |
| j-lawyer-server/j-lawyer-server-ejb | 841 | 1 | 16 |
| j-lawyer-server/j-lawyer-server-war | 5 | 4 | 0 |
| gesamt | 1044 | 22 | 16 |

Mehr Tests als im Build vom 02.11. (1022), die zusätzlichen kommen aus den neuen Commits von master. Die 22 fehlgeschlagenen Tests sind genau dieselben wie im Build vom 02.11. und auf master 7b3b3dfa8 (FtpTest, SftpTest und SmbTest ohne Server, LibreOfficeODFTest#setReplaceEmptyLine, BackupExecutorTest, FileNameEncodingTest und SyncTest), siehe Build and tests oben. master ac2aa7a62 selbst wurde nicht gebaut.
