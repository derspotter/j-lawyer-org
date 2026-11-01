fix(finanzen): klare Fehlermeldung bei bereits vergebener Belegnummer beim Wechsel des Nummernkreises

## Problem
Wird der Startwert eines Nummernkreises zurückgesetzt, etwa weil eine Belegnummer versehentlich vergeben wurde, vergibt der Nummernkreis danach wieder Nummern, die es schon gibt. Wechselt man dann bei einem Beleg über den Stift den Nummernkreis, scheitert das Speichern mit einem technischen Datenbankfehler statt mit einem verständlichen Hinweis. Beim erneuten Versuch erscheint derselbe Fehler, weil die Nummer im Nummernkreis nicht weitergezählt wurde.

## Ursache
Für neu angelegte Belege prüft `ArchiveFileService.addInvoice()` seit 80ab1c597 (#2394), ob die Nummer schon vergeben ist, und meldet „Es gibt bereits einen Beleg mit der Nummer … - bitte die Einstellungen des Nummernkreises … prüfen!“. `updateInvoiceType()`, das beim Wechsel des Nummernkreises die neue Nummer zieht, hat diese Prüfung nicht. Die doppelte Nummer fällt erst beim Commit am eindeutigen Index `IDX_INVOICES_INVOICENO_UQ` auf. Die Transaktion wird zurückgerollt, auch das Weiterzählen des Nummernkreises.

## Fix
`updateInvoiceType()` prüft die neue Nummer wie `addInvoice()` und meldet denselben Hinweis, bevor der Beleg verändert wird. Dafür wird der Wechsel des Nummernkreises vor der Änderung der Belegart ausgeführt, damit bei diesem Fehler auch keine Belegart-Änderung gespeichert wird. Wie bei `addInvoice()` bleibt das Weiterzählen des Nummernkreises dabei erhalten (Anwendungsfehler, kein Rollback). Ein erneuter Versuch bekommt also die nächste Nummer.

## Verifiziert
- Neuer Test `InvoiceTypeChangeConflictTest` (j-lawyer-server-ejb): Nummernkreis liefert eine vergebene Nummer, `updateInvoiceType()` meldet den Hinweis mit der Nummer, der Beleg behält Nummer und Nummernkreis und wird nicht gespeichert. Die Abhängigkeiten werden über einfache Proxys ersetzt, es ist kein Server nötig.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build: `mvn -B -ntp -pl j-lawyer-server/j-lawyer-server-ejb -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Alle Module des Reactors bauen. Tests: 1017 ausgeführt (master plus 1 neuer), 18 fehlgeschlagen. Fehlgeschlagen sind nur FtpTest, SftpTest und SmbTest in j-lawyer-server-common (brauchen echte FTP-, SFTP- und SMB-Server) sowie `LibreOfficeODFTest.setReplaceEmptyLine` in j-lawyer-server-ejb (bekannter Fehler auf master). Das sind dieselben Klassen und Testfälle wie auf master 7b3b3dfa8 mit demselben Setup.

## Nicht abgedeckt
- Der Vorschlag aus dem Issue, schon beim Zurücksetzen des Startwerts auf vorhandene Nummern hinzuweisen, ist nicht umgesetzt. j-dimension bevorzugt, falsche Belege auf „storniert“ zu setzen statt sie zu löschen.
- Kein Test gegen eine echte Datenbank.

Refs #2203

🤖 Generated with [Claude Code](https://claude.com/claude-code)
