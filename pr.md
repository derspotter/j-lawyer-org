fix(e-mail): kein doppeltes Leerzeichen bei leeren Platzhaltern in Textvorlagen

## Problem
In E-Mail-Vorlagen im Format Text (text/plain) bleibt bei einem leeren Platzhalter ein doppeltes Leerzeichen stehen, z.B. „Sehr geehrter Herr  Müller,“, wenn der Mandant keinen Titel hat (#2888).

## Ursache
`CommonTemplatesUtil.replacePlaceHolders()` ersetzt `{{KEY}}` durch den getrimmten Wert und lässt die Leerzeichen davor und danach stehen. Bei einem leeren Wert stehen dann zwei Leerzeichen hintereinander. In HTML zeigt die Darstellung sie als eines, in Text-E-Mails bleiben beide sichtbar.

## Fix
- Neue Methode `CommonTemplatesUtil.replacePlaceHoldersInText()`: Ein Platzhalter ohne Wert (leer, nur Leerzeichen oder nicht belegt), der zwischen zwei Leerzeichen steht, wird mit einem der beiden entfernt. Danach ersetzt sie wie `replacePlaceHolders()`, mit dem wörtlichen Einsetzen aus #3750.
- `SendEmailFrame` nutzt sie nur für den Text einer Textvorlage. `replacePlaceHolders()` selbst und damit HTML-Vorlagen, Betreff und Empfänger, Dokumentvorlagen und Dateinamen bleiben unverändert.
- Leerzeichen, die in der Vorlage selbst stehen, bleiben erhalten, ebenso ein Leerzeichen am Zeilenanfang oder -ende neben einem leeren Platzhalter.

## Verifiziert
- Zwei neue Fälle in `ReplacePlaceHoldersTest` (j-lawyer-server-common): leerer, nicht belegter und nur aus Leerzeichen bestehender Titel in „Sehr geehrter Herr {{MANDANT_TITEL}} {{MANDANT_NAME}},“, zwei leere Platzhalter hintereinander, ein Titel mit Wert, und dass doppelte Leerzeichen der Vorlage, Platzhalter am Zeilenanfang, am Zeilenende, nach einem Zeilenumbruch und in Klammern unverändert bleiben. Der vorhandene Fall `testRegularValues` hält fest, dass `replacePlaceHolders()` für HTML und Dokumente weiter „a  b“ liefert. Diese Methode hat der Textzweig bisher genutzt.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches (Stand 0a7ae6a4f, gestartet 04.11.2026 07:53 UTC) und Vergleichsbuild von master 05f6b4350 (04.11.2026) mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 316 gesamt, davon 316 ausgeführt und 0 übersprungen, 17 fehlgeschlagen. Auf master: 314 gesamt, 314 ausgeführt, 0 übersprungen, 17 fehlgeschlagen. Neu sind 2 Tests.
- Die fehlgeschlagenen Tests sind genau dieselben wie auf master: FtpTest und SftpTest in j-lawyer-server-common brauchen einen echten FTP- bzw. SFTP-Server mit Zugangsdaten aus Umgebungsvariablen. SmbTest braucht die im Test fest eingetragene SMB-Freigabe smb://j-lawyer-box/j-lawyer-share/.

## Nicht abgedeckt
- Im laufenden Client nicht ausprobiert.
- beA-Nachrichten (`SendBeaMessageFrame`) nutzen ebenfalls Textvorlagen und sind hier nicht geändert, um den PR auf E-Mails zu beschränken. Die REST-Schnittstelle `TemplatesEndpointV6` setzt Vorlagentexte weiter mit `replacePlaceHolders()` ein.

Closes #2888

🤖 Generated with [Claude Code](https://claude.com/claude-code)
