fix(finanzen): Girocode enthält auch bei Beträgen ab rund 131.000 EUR den exakten Rechnungsbetrag

## Problem
Der Girocode (QR-Code für die Überweisung) kann bei hohen Rechnungsbeträgen um einen Cent vom Rechnungsbetrag abweichen, z.B. 150.000,02 statt 150.000,01. Die Banking-App übernimmt dann den falschen Betrag.

## Ursache
`InvoiceDialog.cmdGiroCodeActionPerformed()` liest den angezeigten Gesamtbetrag über `floatValue()` ein. Ein `float` hat nur etwa 7 signifikante Stellen. Ab 131.072 (2^17) lassen sich Cent-Beträge nicht mehr genau darstellen und werden bei der Formatierung für den Girocode teils falsch gerundet.

## Fix
Neue Hilfsmethode `AmountUtils.parseExact()`, die den Betrag mit einer Kopie des Anzeigeformats exakt als `BigDecimal` einliest (`setParseBigDecimal(true)`). Der Girocode-Dialog nutzt sie.

## Verifiziert
- Neuer Test `AmountUtilsTest` (j-lawyer-client): 150.000,01 und 131.072,37 werden exakt eingelesen, alle Beträge von 131.000,00 bis 132.000,00 (Abstand 0,13) werden nach Formatieren exakt zurückgelesen, ungültige Eingaben werfen eine `ParseException`, das übergebene Format bleibt unverändert.
- Zusätzlich alle Beträge von 100.000,00 bis 300.000,00 im Abstand von 0,07 außerhalb des Repositorys durchgerechnet: Bisher ergeben 1.041.827 von 2.857.143 Beträgen einen falschen Cent, der erste ist 131.072,37. Mit dem neuen Einlesen sind alle exakt.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches und Vergleichsbuild von master 7b3b3dfa8 mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 272 gesamt, davon 272 ausgeführt und 0 übersprungen, 17 fehlgeschlagen. Auf master: 268 gesamt, 268 ausgeführt, 0 übersprungen, 17 fehlgeschlagen. Neu sind 4 Tests.
- Die fehlgeschlagenen Tests sind genau dieselben wie auf master: FtpTest und SftpTest in j-lawyer-server-common brauchen einen echten FTP- bzw. SFTP-Server mit Zugangsdaten aus Umgebungsvariablen. SmbTest braucht die im Test fest eingetragene SMB-Freigabe smb://j-lawyer-box/j-lawyer-share/.

## Nicht abgedeckt
- Nicht in einem laufenden Client ausprobiert, der Girocode-Dialog selbst hat keinen automatischen Test.
- Dasselbe Muster gibt es beim Budget-Limit der Zeiterfassung (`TimesheetDialog`). Dort wird auf 2 Nachkommastellen gespeichert und Limits über 131.000 EUR sind selten, daher nicht Teil dieses PRs.

Ohne eigenes Issue, gefunden beim Prüfen von #2747.

🤖 Generated with [Claude Code](https://claude.com/claude-code)
