# Review of fork/fix/imap-timeouts-postfachscanner (notes/imap-timeouts-postfachscanner), 02.11.2026

Codex gpt-6-astra, read-only, against origin/master 7b3b3dfa8. Fix each point as a new commit on the same branch, or answer it in report.md.

1. **[P1] [MailboxScannerTask.java:1096](j-lawyer-server/j-lawyer-server-war/src/main/java/com/jdimension/jlawyer/timer/MailboxScannerTask.java:1096)** — The entire change is in unused code. `processMailbox()` is explicitly marked “no longer called” at line 1084. `run()` calls `processMailboxViaService()` at line 764, whose IMAP connections already receive these timeouts through `EmailService.imapConnect()` at lines 1854–1857. The patch therefore cannot fix the claimed scanner hang. **Suggested fix:** withdraw this obsolete fix or reproduce and fix a remaining problem in the active path, with a regression test that fails on the baseline. Correct the commit and PR claims accordingly.

2. **[P2] `fork/notes/imap-timeouts-postfachscanner:report.md:26`** — The stated baseline command selects client, EJB, and backup-manager modules. Their dependency closure excludes `j-lawyer-server-war`, yet the table reports matching WAR results and `pr.md:16` claims identical failing cases. That command cannot establish this comparison. **Suggested fix:** provide the actual baseline command and corresponding WAR results, or remove the unsupported comparison.

3. **[P2] `fork/notes/imap-timeouts-postfachscanner:pr.md:13`** — The probe uses JavaMail 1.6.2 and calls it the project version, but the affected WAR declares `com.sun.mail:jakarta.mail`, managed as **1.6.8** in `pom.xml:363`. The standalone probe also exercises manually supplied properties, not the scanner’s connection path, so it cannot detect finding 1. **Suggested fix:** accurately identify the probe’s artifact/version, include its exact reproduction command, and distinguish library behavior from verification of the scanner fix.

4. **[P3] `fork/notes/imap-timeouts-postfachscanner:pr.md:16`** — The test summary contains factual errors. The report lists 1,021 total tests including 16 skipped, meaning 1,005 executed. `FileNameEncodingTest` requires local template files, and `SyncTest.testSync()` requires local backup files and SFTP configuration. Neither uses `mysqldump` or `jlawyerdb`. **Suggested fix:** report skipped tests explicitly and correct the failure prerequisites using the actual failure output.

Read-only inspection only. No files modified or builds/tests rerun. Author and co-author metadata were inspected. Neither the commit message nor PR contains semicolons. No GUI, Remote-interface, schema, or unrelated refactoring changes were found.

VERDICT: NEEDS-CHANGES
