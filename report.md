# Report imap-timeouts-postfachscanner

- Branch: `fix/imap-timeouts-postfachscanner` (fork derspotter/j-lawyer-org), commit a27e8348b, unchanged since 01.11.
- Status: WITHDRAWN. Please do not post, the branch can be deleted.

## Antwort auf Review 02.11.
1. [P1] Unused code: correct. `MailboxScannerTask.run()` calls `processMailboxViaService()` (line 764), `processMailbox()` is marked "Legacy method - kept for reference, no longer called" (line 1084). The active path connects through `EmailService.imapConnect()`, which has had the IMAP timeouts since e9f331df2. The change fixes nothing, so the branch is withdrawn instead of being reworked. No new commits.
2. [P2] Baseline command, 3. [P2] probe version, 4. [P3] test counts: moot with the withdrawal. For the record, the probe used `com.sun.mail:javax.mail:1.6.2` (the client's version), not the server's `jakarta.mail` 1.6.8, and it only showed library behaviour, not the scanner path.
