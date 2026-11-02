fix(finanzen): klare Fehlermeldung bei bereits vergebener Belegnummer beim Wechsel des Nummernkreises

## Problem
Wird der Startwert eines Nummernkreises zurückgesetzt, etwa weil eine Belegnummer versehentlich vergeben wurde, vergibt der Nummernkreis danach wieder Nummern, die es schon gibt. Wechselt man dann bei einem Beleg über den Stift den Nummernkreis, scheitert das Speichern mit einem technischen Datenbankfehler statt mit einem verständlichen Hinweis. Beim erneuten Versuch erscheint derselbe Fehler, weil die Nummer im Nummernkreis nicht weitergezählt wurde.

## Ursache
Für neu angelegte Belege prüft `ArchiveFileService.addInvoice()` seit 80ab1c597 (#2394), ob die Nummer schon vergeben ist, und meldet „Es gibt bereits einen Beleg mit der Nummer … - bitte die Einstellungen des Nummernkreises … prüfen!“. `updateInvoiceType()`, das beim Wechsel des Nummernkreises die neue Nummer zieht, hat diese Prüfung nicht. Die doppelte Nummer fällt erst beim Commit am eindeutigen Index `IDX_INVOICES_INVOICENO_UQ` auf. Die Transaktion wird zurückgerollt, auch das Weiterzählen des Nummernkreises.

## Fix
`updateInvoiceType()` prüft die neue Nummer wie `addInvoice()` und meldet denselben Hinweis, bevor der Beleg verändert wird. Dafür wird der Wechsel des Nummernkreises vor der Änderung der Belegart ausgeführt, damit bei diesem Fehler auch keine Belegart-Änderung gespeichert wird. Wie bei `addInvoice()` bleibt das Weiterzählen des Nummernkreises dabei erhalten (Anwendungsfehler, kein Rollback). Ein erneuter Versuch bekommt also die nächste Nummer.

## Verifiziert
- Test `InvoiceTypeChangeConflictTest` (j-lawyer-server-ejb) mit zwei Fällen: Wechsel nur des Nummernkreises und Wechsel von Nummernkreis und Belegart. Der Nummernkreis liefert eine vergebene Nummer, `updateInvoiceType()` meldet den Hinweis mit der Nummer, der Beleg behält Nummer, Nummernkreis und Belegart und wird nicht gespeichert. Die Abhängigkeiten werden über einfache Proxys ersetzt, es ist kein Server nötig.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches und Vergleichsbuild von master 7b3b3dfa8 mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-server/j-lawyer-server-ejb -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 1018 gesamt, davon 1002 ausgeführt und 16 übersprungen, 18 fehlgeschlagen. Auf master: 1016 gesamt, 1000 ausgeführt, 16 übersprungen, 18 fehlgeschlagen. Neu sind 2 Tests.
- Die fehlgeschlagenen Tests sind genau dieselben wie auf master: FtpTest, SftpTest und SmbTest in j-lawyer-server-common brauchen echte FTP-, SFTP- und SMB-Server mit Zugangsdaten aus Umgebungsvariablen. `LibreOfficeODFTest.setReplaceEmptyLine` in j-lawyer-server-ejb ist ein bekannter Fehler auf master (behoben durch den offenen PR #3703).

## Nicht abgedeckt
- Kein Test gegen eine echte Datenbank oder einen laufenden Server. Das Transaktionsverhalten ist aus den EJB-Regeln abgeleitet.
- Der Vorschlag aus dem Issue, schon beim Zurücksetzen des Startwerts auf vorhandene Nummern hinzuweisen, ist nicht umgesetzt. j-dimension bevorzugt, falsche Belege auf „storniert“ zu setzen statt sie zu löschen.

Refs #2203

🤖 Generated with [Claude Code](https://claude.com/claude-code)
