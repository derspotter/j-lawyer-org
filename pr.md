ZURÜCKGEZOGEN: fix(e-mail): Zeitlimits für IMAP-Verbindungen des Postfach-Scanners auf dem Server

Dieser Branch wird zurückgezogen und soll nicht als PR gestellt werden.

Die Änderung liegt in `MailboxScannerTask.processMailbox()`. Diese Methode ist als „Legacy method - kept for reference, no longer called“ markiert. Der Scanner ruft `processMailboxViaService()` auf, dessen IMAP-Verbindungen über `EmailService` laufen, und dort sind die Zeitlimits seit e9f331df2 bereits gesetzt. Der beschriebene Hänger des Scanners besteht im aktiven Pfad also nicht.

Refs #1881

🤖 Generated with [Claude Code](https://claude.com/claude-code)
