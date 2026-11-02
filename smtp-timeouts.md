# Review of fork/fix/smtp-timeouts (notes/smtp-timeouts), 02.11.2026

Codex gpt-6-astra, read-only, against origin/master 7b3b3dfa8. Fix each point as a new commit on the same branch, or answer it in report.md.

1. **`fork/notes/smtp-timeouts:report.md:32` — Baseline command contradicts the comparison.** It selects `j-lawyer-backupmgr`, whereas the branch command selects `j-lawyer-server-war`. The baseline selection does not build the WAR, yet the table reports baseline WAR tests and claims identical failures. Record the actual baseline command and supporting results, or rerun the same module selection before claiming equivalence.

2. **`fork/notes/smtp-timeouts:pr.md:10` and `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/mail/SendAction.java:858` — The claimed seventh call site is commented out.** The addition lies inside the legacy block beginning at line 805. Active client sending delegates to `EmailService`. Remove the inert addition and unused import, and describe six active server call sites. Correct `report.md:61` accordingly.

3. **`fork/notes/smtp-timeouts:pr.md:17` — Test counts and failure explanations are inaccurate.** The report’s 1103 tests include 16 skipped tests, so 1087 actually ran. Also, `FileNameEncodingTest` requires local filesystem fixtures, and `SyncTest.testSync` requires local backups and SFTP access. Neither uses `mysqldump`. Report skipped tests explicitly and distinguish these prerequisites from `BackupExecutorTest`’s database requirements.

4. **`fork/notes/smtp-timeouts:pr.md:14` and `report.md:52` — The probe’s library version is mischaracterized.** JavaMail 1.6.2 is declared by the client, but server-common and server modules declare `com.sun.mail:jakarta.mail:1.6.8` through `pom.xml:363`. Label the probe specifically as testing 1.6.2. Verify the server provider separately or explicitly mark it untested.

The new unit test checks all four default properties. I found no additional introduced runtime regression, override-order problem, or `.form`, Remote-interface, migration, or unrelated-refactoring violation. Commit attribution is present, and the commit message and PR contain no semicolons. No files were modified and no builds or tests were rerun.

VERDICT: NEEDS-CHANGES
