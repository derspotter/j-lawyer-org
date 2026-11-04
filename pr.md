fix(e-mail): normale Leerzeile statt Absatz zwischen Vorlagentext und Signatur

## Problem
Wird beim Schreiben einer HTML-E-Mail eine Vorlage ausgewählt, steht zwischen dem Text der Vorlage und der Signatur seit #3263 eine Leerzeile im Format „Absatz“, mit Absatzabstand. Wer durchgehend im Format „normal (div)“ schreibt, bekommt dadurch einen größeren Abstand als gewollt (#3402). In Antworten sind es zwei Leerzeilen in einem Absatz.

## Ursache
`SendEmailFrame.cmbTemplatesActionPerformed()` verbindet Vorlagentext, Text vor der Signatur und Signatur jeweils mit einem einzelnen `<br/>`: bei einer neuen E-Mail mit HTML-Vorlage, bei einer Antwort mit HTML-Vorlage und bei einer Antwort auf eine HTML-E-Mail mit Textvorlage. Der HTML-Editor (SunEditor) setzt ein `<br/>` zwischen Blockelementen in einen eigenen Absatz `<p><br></p>`, und `.sun-editor-editable p` hat einen Absatzabstand. In Antworten ist der Text vor der Signatur leer, dann landen beide `<br/>` in einem Absatz.

## Fix
- Die Teile werden mit einer Leerzeile im Format „normal (div)“ verbunden (`<div><br></div>`). Das ist die eine Zeile Abstand, die 0a2e62097 für #3263 vorgesehen hat.
- Ist der Text vor der Signatur leer (in Antworten der Normalfall), steht nur eine Leerzeile zwischen Vorlagentext und Signatur, nicht zwei. So macht es 0a2e62097 schon für Text-E-Mails.
- Die Verbindung steht in der kleinen statischen Methode `withSignatureHtml()`, die alle drei Stellen nutzen. Textvorlagen in Text-E-Mails und das Einsetzen der Signatur ohne Vorlage (`setBody()`, dort schon ohne `<br/>`) sind unverändert.

## Verifiziert
- Neuer Test `SignatureSpacingTest` (j-lawyer-client) mit 4 Fällen: neue E-Mail, Antwort mit leerem und mit vorhandenem Text vor der Signatur, kein `<br/>` zwischen den Blöcken.
- Mit dem SunEditor aus den Ressourcen des Clients (`suneditor-template.html` mit `suneditor.min.js`) in Chromium über Playwright geprüft, eine Textzeile ist dort 20 px hoch. Neue E-Mail: bisher wird das `<br/>` zu `<p><br></p>` mit 30 px Abstand zwischen Text und Signatur, jetzt bleibt `<div><br></div>` stehen und der Abstand ist 20 px. Antwort: bisher `<p><br><br></p>` mit 49 px, jetzt `<div><br></div>` mit 20 px. Im Client läuft der Editor in JavaFX WebView statt Chromium, die Umwandlung macht aber das JavaScript des Editors.
- Zusammenführen mit den offenen PRs #3718, #3721 und #3724 (alle im E-Mail-Bereich) ist ohne Konflikt möglich, jeweils einzeln mit `git merge-tree` geprüft. #3718 baut den Text beim Wechsel des Postfachs über die Vorlagenauswahl neu auf und nutzt damit ebenfalls die neue Verbindung.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches (Stand 531129f1a, gestartet 04.11.2026 07:51 UTC) und Vergleichsbuild von master 05f6b4350 (04.11.2026) mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 318 gesamt, davon 318 ausgeführt und 0 übersprungen, 17 fehlgeschlagen. Auf master: 314 gesamt, 314 ausgeführt, 0 übersprungen, 17 fehlgeschlagen. Neu sind 4 Tests.
- Die fehlgeschlagenen Tests sind genau dieselben wie auf master: FtpTest und SftpTest in j-lawyer-server-common brauchen einen echten FTP- bzw. SFTP-Server mit Zugangsdaten aus Umgebungsvariablen. SmbTest braucht die im Test fest eingetragene SMB-Freigabe smb://j-lawyer-box/j-lawyer-share/.

## Nicht abgedeckt
- Im laufenden Client nicht ausprobiert.
- Den Abstand ganz wegzulassen (die erste Bitte im Issue) ist eine Gestaltungsfrage. Hier bleibt die eine Leerzeile aus #3263, nur im Format „normal (div)“.

Closes #3402

🤖 Generated with [Claude Code](https://claude.com/claude-code)
