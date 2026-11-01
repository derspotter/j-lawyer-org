fix(finanzen): Girocode enthält auch bei Beträgen ab rund 131.000 EUR den exakten Rechnungsbetrag

## Problem
Der Girocode (QR-Code für die Überweisung) kann bei hohen Rechnungsbeträgen um einen Cent vom Rechnungsbetrag abweichen, z.B. 150.000,02 statt 150.000,01. Die Banking-App übernimmt dann den falschen Betrag.

## Ursache
`InvoiceDialog.cmdGiroCodeActionPerformed()` liest den angezeigten Gesamtbetrag über `floatValue()` ein. Ein `float` hat nur etwa 7 signifikante Stellen. Ab 131.072 (2^17) lassen sich Cent-Beträge nicht mehr genau darstellen und werden bei der Formatierung für den Girocode teils falsch gerundet.

## Fix
Der Betrag wird exakt als `BigDecimal` eingelesen, mit einer Kopie des Anzeigeformats und `setParseBigDecimal(true)`.

## Verifiziert
- Alle Beträge von 100.000,00 bis 300.000,00 im Abstand von 0,07 durchgerechnet (Anzeigeformat `#,##0.00`, Girocode-Format `0.00` wie im Server): Bisher ergeben 1.041.827 von 2.857.143 Beträgen einen falschen Cent, der erste ist 131.072,37 (Girocode 131072.38). Mit dem neuen Einlesen sind alle exakt.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Alle Module des Reactors bauen. Tests: 268 ausgeführt, 17 fehlgeschlagen. Fehlgeschlagen sind nur FtpTest, SftpTest und SmbTest in j-lawyer-server-common (sie brauchen echte FTP-, SFTP- und SMB-Server). Das sind dieselben Klassen und Testfälle wie auf master 7b3b3dfa8 mit demselben Setup.
- Kein Unit-Test: Das Einlesen steckt im Swing-Dialog.

## Nicht abgedeckt
- Dasselbe Muster gibt es beim Budget-Limit der Zeiterfassung (`TimesheetDialog`). Dort wird auf 2 Nachkommastellen gespeichert und Limits über 131.000 EUR sind selten, daher nicht Teil dieses PRs.

Ohne eigenes Issue, gefunden beim Prüfen von #2747.

🤖 Generated with [Claude Code](https://claude.com/claude-code)
