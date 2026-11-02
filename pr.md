fix(e-mail): Zeitlimits für SMTP-Verbindungen, damit der Versand bei einem nicht antwortenden Mailserver nicht endlos hängt

## Problem
Nimmt ein Mailserver beim Versand die Verbindung an, antwortet dann aber nicht mehr, wartet der j-lawyer-Server unbegrenzt. Beim Versand einer E-Mail aus dem Client hängt dann der Versand, beim Testen der Postfacheinstellungen der Dialog. Auch Systembenachrichtigungen (z.B. Erinnerungen zu fälligen Belegen) und Warnungen der Serverüberwachung blockieren dann ihren Thread.

## Ursache
Für IMAP setzt `EmailService` seit e9f331df2 Zeitlimits (`mail.imap(s).connectiontimeout` und `timeout`). Für SMTP fehlen sie an allen Stellen. Ohne diese Eigenschaften wartet JavaMail unbegrenzt.

## Fix
Neue Methode `CommonMailUtils.setSmtpTimeouts()` setzt für `smtp` und `smtps` ein Verbindungslimit von 30 Sekunden und ein Leselimit von 10 Minuten. RFC 5321 empfiehlt, auf die Antwort nach dem Nachrichteninhalt bis zu 10 Minuten zu warten, weil Server große Nachrichten vorher prüfen. Ein kürzeres Limit würde den Versand großer Anhänge gefährden. Die Methode wird an allen sechs Stellen auf dem Server aufgerufen, die SMTP-Sitzungen aufbauen: Versand von E-Mails (`EmailService.smtpSendMail()`) und Test der Postfacheinstellungen in `EmailService`, Systembenachrichtigungen in `SystemManagement` (Statusmails und Testmail), Benachrichtigungen über die Warteschlange `OutgoingMailProcessor` und Warnungen der Serverüberwachung (`SystemMonitorTask`). Sie wird jeweils vor den individuellen Einstellungen eines Postfachs angewendet, diese können die Werte also weiterhin überschreiben.

## Verifiziert
- Neuer Test `SmtpTimeoutsTest` (j-lawyer-server-common) für die gesetzten Eigenschaften.
- Ausprobiert mit `com.sun.mail:jakarta.mail` 1.6.8, der JavaMail-Implementierung des Servers, gegen einen lokalen Server, der die Verbindung annimmt und nie antwortet: Ohne Zeitlimits blockiert der Verbindungsaufbau nach 12 Sekunden noch immer, mit `mail.smtp.timeout=2000` endet er nach 2 Sekunden mit `SocketTimeoutException`. Das 10-Minuten-Limit selbst wurde nicht abgewartet.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches und Vergleichsbuild von master 7b3b3dfa8 mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-server/j-lawyer-server-ejb,j-lawyer-server/j-lawyer-server-war -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 1022 gesamt, davon 1006 ausgeführt und 16 übersprungen, 22 fehlgeschlagen. Auf master: 1021 gesamt, 1005 ausgeführt, 16 übersprungen, 22 fehlgeschlagen. Neu ist 1 Test.
- Die fehlgeschlagenen Tests sind genau dieselben wie auf master: FtpTest, SftpTest und SmbTest in j-lawyer-server-common brauchen echte FTP-, SFTP- und SMB-Server mit Zugangsdaten aus Umgebungsvariablen. `LibreOfficeODFTest.setReplaceEmptyLine` in j-lawyer-server-ejb ist ein bekannter Fehler auf master (behoben durch den offenen PR #3703). `BackupExecutorTest` in j-lawyer-server-war braucht lokale Verzeichnisse unter /home/jens/dev/projects und eine lokale MySQL-Datenbank für mysqldump. `FileNameEncodingTest` braucht lokale Vorlagendateien unter /home/jens/dev/projects/j-lawyer-data. `SyncTest.testSync` braucht lokale Sicherungen unter /home/jens/dev/projects/backups und einen SFTP-Zugang.

## Nicht abgedeckt
- Nicht gegen einen echten Mailserver und nicht auf einem laufenden j-lawyer-Server ausprobiert.
- Ein Schreiblimit (`mail.smtp.writetimeout`) ist nicht gesetzt, JavaMail braucht dafür je Verbindung einen zusätzlichen Thread.

Refs #1881

🤖 Generated with [Claude Code](https://claude.com/claude-code)
