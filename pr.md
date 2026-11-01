fix(finanzen): Belegdialog verwirft beim erneuten Bestätigen von Belegart/Nummernkreis keine Eingaben mehr

## Problem
Wird bei einem bestehenden Beleg die Belegart oder der Nummernkreis über den Stift geändert und mit dem Haken bestätigt, springt der Status zurück auf „Entwurf“ und der Adressat verschwindet. Ebenso gehen alle anderen noch nicht gespeicherten Eingaben im Dialog verloren (Bezeichnung, Texte, Absender, Zahlungsart, Änderungen an Positionen). Beim erstmaligen Anlegen des Belegs tritt das nicht auf.

## Ursache
`InvoiceDialog.cmdConfirmInvoiceNumberActionPerformed()` übernimmt im Änderungszweig die Eingaben nur in das lokale Objekt (`fillCurrentEntry()`). `ArchiveFileService.updateInvoiceType()` speichert serverseitig aber nur Belegart, Nummernkreis und Nummer. Danach lädt `setEntry(updatedInvoice)` den Beleg samt Positionen neu vom Server und überschreibt so alle ungespeicherten Eingaben mit dem zuletzt gespeicherten Stand.

## Fix
Vor `updateInvoiceType()` wird `save()` aufgerufen, so wie der Dialog es schon vor dem Erstellen oder Hochladen eines Belegdokuments tut. Damit liegen Eingaben und Positionen auf dem Server, bevor neu geladen wird. Schlägt das Speichern des Belegs fehl, bricht die Bestätigung ab und der Dialog bleibt im Bearbeitungsmodus, damit nichts verworfen wird. Dafür gibt `save()` jetzt zurück, ob das Speichern geklappt hat. Die übrigen Aufrufer ignorieren den Rückgabewert wie bisher.

## Verifiziert
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Alle Module des Reactors bauen. Tests: 268 ausgeführt, 17 fehlgeschlagen. Fehlgeschlagen sind nur FtpTest, SftpTest und SmbTest in j-lawyer-server-common (sie brauchen echte FTP-, SFTP- und SMB-Server). Das sind dieselben Klassen und Testfälle wie auf master 7b3b3dfa8 mit demselben Setup.
- Kein Unit-Test: Die Logik steckt in einem modalen Swing-Dialog mit Serveraufrufen.

## Nicht abgedeckt
- Durch das Speichern vor dem Bestätigen bleiben diese Eingaben auch dann gespeichert, wenn der Dialog danach mit „Abbrechen“ geschlossen wird. Belegart, Nummernkreis und Nummer wurden an dieser Stelle schon bisher sofort gespeichert.
- Kein manueller Test gegen einen laufenden Server.

Closes #3371

🤖 Generated with [Claude Code](https://claude.com/claude-code)
