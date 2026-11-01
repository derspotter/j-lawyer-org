fix(e-mail): Zeitlimits für IMAP-Verbindungen des Postfach-Scanners auf dem Server

## Problem
Der Postfach-Scanner auf dem Server prüft alle 15 Minuten die eingerichteten Postfächer und ordnet eingehende E-Mails Akten zu. Nimmt ein IMAP-Server die Verbindung an, antwortet dann aber nicht mehr, bleibt der Scanner für immer hängen. Weil er auf einem eigenen `java.util.Timer` läuft, findet bis zum nächsten Serverneustart auch kein weiterer Durchlauf mehr statt, für kein Postfach.

## Ursache
`MailboxScannerTask.processMailbox()` baut die IMAP-Sitzung ohne Zeitlimits auf. In `EmailService` hat j-dimension mit e9f331df2 Zeitlimits für IMAP-Verbindungen eingeführt (`mail.imap(s).connectiontimeout` 15 s, `mail.imap(s).timeout` 30 s), im Scanner fehlen sie. Ohne diese Eigenschaften wartet JavaMail unbegrenzt.

## Fix
Der Scanner setzt dieselben Zeitlimits wie `EmailService`, vor den individuellen Einstellungen des Postfachs, die die Werte also weiterhin überschreiben können.

## Verifiziert
- Ausprobiert mit JavaMail 1.6.2 (Version des Projekts) gegen einen lokalen Server, der die Verbindung annimmt und nie antwortet: Ohne Zeitlimits blockiert der Verbindungsaufbau nach 10 Sekunden noch immer, mit `mail.imap.timeout` endet er nach Ablauf des Limits mit einer `MessagingException`.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build: `mvn -B -ntp -pl j-lawyer-server/j-lawyer-server-war -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Alle Module des Reactors bauen. Tests: 1021 ausgeführt, 22 fehlgeschlagen. Fehlgeschlagen sind nur FtpTest, SftpTest und SmbTest in j-lawyer-server-common (sie brauchen echte FTP-, SFTP- und SMB-Server) sowie `LibreOfficeODFTest.setReplaceEmptyLine` in j-lawyer-server-ejb (bekannter Fehler auf master) sowie BackupExecutorTest, FileNameEncodingTest und SyncTest in j-lawyer-server-war (brauchen eine lokale Datenbank jlawyerdb für mysqldump). Das sind dieselben Klassen und Testfälle wie auf master 7b3b3dfa8 mit demselben Setup.
- Kein Unit-Test: Die Sitzung wird mitten im TimerTask zwischen JNDI-Lookups aufgebaut.

## Nicht abgedeckt
- `SystemManagement.testReceiveMail()` (Test des Empfangs in den Servereinstellungen) baut ebenfalls eine IMAP-Sitzung ohne Zeitlimits auf. Dort werden die Eigenschaften aber in `System.getProperties()` gesetzt, also für die ganze JVM. Das sollte zusammen mit dieser Besonderheit angesehen werden und ist nicht Teil dieses PRs.
- Nicht gegen einen echten Mailserver getestet.

Refs #1881

🤖 Generated with [Claude Code](https://claude.com/claude-code)
