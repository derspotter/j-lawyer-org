fix(e-mail): Zeitlimits für SMTP-Verbindungen, damit der Versand bei einem nicht antwortenden Mailserver nicht endlos hängt

## Problem
Nimmt ein Mailserver beim Versand die Verbindung an, antwortet dann aber nicht mehr, wartet j-lawyer unbegrenzt. In der serverseitigen Versandwarteschlange (`OutgoingMailProcessor`) bleiben damit alle folgenden E-Mails liegen, beim Testen der Postfacheinstellungen hängt der Dialog.

## Ursache
Für IMAP setzt `EmailService` seit e9f331df2 Zeitlimits (`mail.imap(s).connectiontimeout` und `timeout`). Für SMTP fehlen sie an allen Stellen. Ohne diese Eigenschaften wartet JavaMail unbegrenzt.

## Fix
Neue Methode `CommonMailUtils.setSmtpTimeouts()` setzt für `smtp` und `smtps` ein Verbindungslimit von 30 Sekunden und ein Leselimit von 10 Minuten. RFC 5321 empfiehlt, auf die Antwort nach dem Nachrichteninhalt bis zu 10 Minuten zu warten, weil Server große Nachrichten vorher prüfen. Ein kürzeres Limit würde den Versand großer Anhänge gefährden. Die Methode wird an allen sieben Stellen aufgerufen, die SMTP-Sitzungen aufbauen: Versandwarteschlange, Test der Postfacheinstellungen und Versand in `EmailService`, Systembenachrichtigungen in `SystemManagement` (zwei Stellen), Warnungen der Serverüberwachung und der direkte Versand im Client (`SendAction`). Sie wird jeweils vor den individuellen Einstellungen eines Postfachs angewendet, diese können die Werte also weiterhin überschreiben.

## Verifiziert
- Neuer Test `SmtpTimeoutsTest` (j-lawyer-server-common) für die gesetzten Eigenschaften.
- Ausprobiert mit JavaMail 1.6.2 (Version des Projekts) gegen einen lokalen Server, der die Verbindung annimmt und nie antwortet: Ohne Zeitlimits blockiert der Verbindungsaufbau nach 12 Sekunden noch immer, mit `mail.smtp.timeout` endet er nach Ablauf des Limits mit `SocketTimeoutException`.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build: `mvn -B -ntp -pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb,j-lawyer-server/j-lawyer-server-war -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Alle Module des Reactors bauen. Tests: 1103 ausgeführt (1102 auf master plus 1 neuer), 22 fehlgeschlagen. Fehlgeschlagen sind nur FtpTest, SftpTest und SmbTest in j-lawyer-server-common (sie brauchen echte FTP-, SFTP- und SMB-Server) sowie `LibreOfficeODFTest.setReplaceEmptyLine` in j-lawyer-server-ejb (bekannter Fehler auf master) sowie BackupExecutorTest, FileNameEncodingTest und SyncTest in j-lawyer-server-war (brauchen eine lokale Datenbank jlawyerdb für mysqldump). Das sind dieselben Klassen und Testfälle wie auf master 7b3b3dfa8 mit demselben Setup.

## Nicht abgedeckt
- Ein Schreiblimit (`mail.smtp.writetimeout`) ist nicht gesetzt, JavaMail braucht dafür je Verbindung einen zusätzlichen Thread.
- Nicht gegen einen echten Mailserver getestet.

Refs #1881

🤖 Generated with [Claude Code](https://claude.com/claude-code)
