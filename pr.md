fix(finanzen): Bruttosumme eines Belegs ohne ausgewiesene USt entspricht der Nettosumme

## Problem
Ist bei einem Beleg „USt ausweisen“ nicht angehakt, zeigt der Belegdialog als Gesamtbetrag die Nettosumme. Gespeichert wird als Bruttosumme aber die Summe inklusive der Steuersätze der Positionen, z.B. 172,50 statt 150,00. Diese gespeicherte Bruttosumme nutzen unter anderem die Zuordnung von Kontoauszügen (Abgleich mit dem Zahlbetrag), die offenen Rechnungen auf dem Desktop, die Auswertungen (Bruttobetrag), die Rechnungssumme beim Kontakt, die Erinnerungsmails zu fälligen Belegen und der vorgeschlagene Betrag beim grünen Haken. Dort erscheint ein solcher Beleg mit Umsatzsteuer, die gar nicht berechnet wird.

## Ursache
`ArchiveFileService.updateInvoiceTotal()` berechnet die Bruttosumme immer aus den Steuersätzen der Positionen und berücksichtigt `Invoice.smallBusiness` nicht. `smallBusiness=true` bedeutet „USt ausweisen“ (siehe #3170). Der Belegdialog (`InvoiceDialog.updateTotals()`) und die Belegübersicht (`InvoiceEntryPanel`) behandeln Belege ohne USt schon jetzt mit der Nettosumme.

## Fix
- Ohne ausgewiesene USt ist die Bruttosumme gleich der Nettosumme, wie im Belegdialog.
- Nach einem Wechsel des Nummernkreises in `updateInvoiceType()` werden die Summen neu berechnet, weil der Nummernkreis die Einstellung „USt ausweisen“ setzt.

## Verifiziert
- Neuer Test `InvoiceTotalTest` (j-lawyer-server-ejb): Mit USt 150,00 netto / 172,50 brutto wie bisher, ohne USt 150,00 / 150,00. Ohne die Änderung schlägt der Test fehl („expected 150.00 but was 172.50“).
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build: `mvn -B -ntp -pl j-lawyer-server/j-lawyer-server-ejb -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Alle Module des Reactors bauen. Tests: 1018 ausgeführt (1016 auf master plus 2 neue), 18 fehlgeschlagen. Fehlgeschlagen sind nur FtpTest, SftpTest und SmbTest in j-lawyer-server-common (sie brauchen echte FTP-, SFTP- und SMB-Server) sowie `LibreOfficeODFTest.setReplaceEmptyLine` in j-lawyer-server-ejb (bekannter Fehler auf master). Das sind dieselben Klassen und Testfälle wie auf master 7b3b3dfa8 mit demselben Setup.

## Nicht abgedeckt
- Bereits gespeicherte Belege behalten ihre Bruttosumme, bis sie das nächste Mal gespeichert werden. Eine Datenkorrektur bestehender Belege würde eine Migration brauchen und ist nicht Teil dieses PRs.
- Kein Test gegen eine Datenbank.

Ohne eigenes Issue, gefunden beim Prüfen von #3170 und #3488.

🤖 Generated with [Claude Code](https://claude.com/claude-code)
