fix(finanzen): Bruttosumme eines Belegs ohne ausgewiesene USt entspricht der Nettosumme

## Problem
Ist bei einem Beleg „USt ausweisen“ nicht angehakt, zeigt der Belegdialog als Gesamtbetrag die Nettosumme. Gespeichert wird als Bruttosumme aber die Summe inklusive der Steuersätze der Positionen, z.B. 172,50 statt 150,00. Diese gespeicherte Bruttosumme nutzen unter anderem die Zuordnung von Kontoauszügen (Abgleich mit dem Zahlbetrag), die offenen Rechnungen auf dem Desktop, die Auswertungen (Bruttobetrag), die Rechnungssumme beim Kontakt, die Erinnerungsmails zu fälligen Belegen und der vorgeschlagene Betrag beim grünen Haken. Dort erscheint ein solcher Beleg mit Umsatzsteuer, die gar nicht berechnet wird.

## Ursache
`ArchiveFileService.updateInvoiceTotal()` berechnet die Bruttosumme immer aus den Steuersätzen der Positionen und berücksichtigt `Invoice.smallBusiness` nicht. `smallBusiness=true` bedeutet „USt ausweisen“ (siehe #3170). Der Belegdialog (`InvoiceDialog.updateTotals()`) und die Belegübersicht (`InvoiceEntryPanel`) behandeln Belege ohne USt schon jetzt mit der Nettosumme.

## Fix
- Ohne ausgewiesene USt ist die Bruttosumme gleich der Nettosumme, wie im Belegdialog.
- Nach einem Wechsel des Nummernkreises in `updateInvoiceType()` werden die Summen neu berechnet, weil der Nummernkreis die Einstellung „USt ausweisen“ setzt.
- Der grüne Haken in der Belegübersicht rechnet mit dem Beleg weiter, den der Server nach dem Speichern zurückgibt. Bisher nutzte er den alten Stand und schlug für bestehende Belege ohne USt weiter den Betrag mit Umsatzsteuer vor.

## Verifiziert
- Test `InvoiceTotalTest` (j-lawyer-server-ejb): mit USt 150,00 netto und 172,50 brutto, ohne USt 150,00 und 150,00, und `updateInvoiceType()` mit einem Wechsel von einem Nummernkreis mit USt zu einem ohne USt und zurück gibt einen Beleg mit der Bruttosumme 100,00 bzw. 119,00 zurück. Geprüft wird der zurückgegebene, gespeicherte Beleg, Fehler werden nicht abgefangen. Ohne die Änderung schlagen der zweite und der dritte Fall fehl.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches und Vergleichsbuild von master 7b3b3dfa8 mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 1100 gesamt, davon 1084 ausgeführt und 16 übersprungen, 18 fehlgeschlagen. Auf master: 1097 gesamt, 1081 ausgeführt, 16 übersprungen, 18 fehlgeschlagen. Neu sind 3 Tests.
- Die fehlgeschlagenen Tests sind genau dieselben wie auf master: FtpTest und SftpTest in j-lawyer-server-common brauchen einen echten FTP- bzw. SFTP-Server mit Zugangsdaten aus Umgebungsvariablen. SmbTest braucht die im Test fest eingetragene SMB-Freigabe smb://j-lawyer-box/j-lawyer-share/. `LibreOfficeODFTest.setReplaceEmptyLine` in j-lawyer-server-ejb ist ein bekannter Fehler auf master (behoben durch den offenen PR #3703).

## Nicht abgedeckt
- Nicht in einem laufenden Client und nicht gegen eine Datenbank ausprobiert. Für den grünen Haken gibt es keinen automatischen Test, er steckt im Swing-Panel mit Serveraufrufen.
- Bereits gespeicherte Belege behalten ihre Bruttosumme, bis sie das nächste Mal gespeichert werden. Eine Datenkorrektur bestehender Belege würde eine Migration brauchen und ist nicht Teil dieses PRs.

Ohne eigenes Issue, gefunden beim Prüfen von #3170 und #3488.

🤖 Generated with [Claude Code](https://claude.com/claude-code)
