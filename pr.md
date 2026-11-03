fix(finanzen): Belegdialog verwirft beim erneuten Bestätigen von Belegart/Nummernkreis keine Eingaben mehr

## Problem
Wird bei einem bestehenden Beleg die Belegart oder der Nummernkreis über den Stift geändert und mit dem Haken bestätigt, springt der Status zurück auf „Entwurf“ und der Adressat verschwindet. Ebenso gehen alle anderen noch nicht gespeicherten Eingaben im Dialog verloren (Bezeichnung, Texte, Absender, Zahlungsart, Änderungen an Positionen). Beim erstmaligen Anlegen des Belegs tritt das nicht auf.

## Ursache
`InvoiceDialog.cmdConfirmInvoiceNumberActionPerformed()` übernimmt im Änderungszweig die Eingaben nur in das lokale Objekt (`fillCurrentEntry()`). `ArchiveFileService.updateInvoiceType()` speichert serverseitig aber nur Belegart, Nummernkreis und Nummer. Danach lädt `setEntry(updatedInvoice)` den Beleg samt Positionen neu vom Server und überschreibt so alle ungespeicherten Eingaben mit dem zuletzt gespeicherten Stand.

## Fix
- Vor `updateInvoiceType()` wird `save()` aufgerufen, so wie der Dialog es schon vor dem Erstellen oder Hochladen eines Belegdokuments tut. Damit liegen Eingaben und Positionen auf dem Server, bevor neu geladen wird.
- Schlägt dabei das Speichern einer Position oder des Belegs fehl, bricht die Bestätigung ab und der Dialog bleibt im Bearbeitungsmodus, damit nichts verworfen wird. Dafür gibt `save()` jetzt zurück, ob alles gespeichert wurde. Die übrigen Aufrufer ignorieren den Rückgabewert wie bisher.
- Belege ohne Belegart (die Spalte `invoice_type` kam mit V2_3_0_20 dazu und darf leer sein) ließen sich bisher gar nicht speichern: `ArchiveFileService.updateInvoice()` setzt den Historieneintrag mit `getInvoiceType().getDisplayName()` zusammen und bricht mit einer NullPointerException ab. Durch das Speichern vor dem Bestätigen hätte das auch das Zuweisen einer Belegart verhindert. Der Historieneintrag nennt jetzt „ohne Belegart“.

## Verifiziert
- Neuer Test `UpdateInvoiceWithoutTypeTest` (j-lawyer-server-ejb): `updateInvoice()` für einen Beleg ohne Belegart kehrt ohne Fehler zurück, der zurückgegebene Beleg enthält die Eingaben und der Historieneintrag nennt „ohne Belegart“. Danach weist `updateInvoiceType()` demselben Beleg Belegart und Nummernkreis zu, mit neuer Belegnummer und eigenem Historieneintrag. Der Historieneintrag ist im Test durch eine Unterklasse ersetzt, weil er die Akte zusätzlich per JDBC aktualisiert und im Unit-Test keine Datenbank vorhanden ist. Fehler werden nicht abgefangen. Mit der Fassung von master scheitert der Test mit einer `NullPointerException` in `updateInvoice()`.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches und Vergleichsbuild von master 7b3b3dfa8 mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 1098 gesamt, davon 1082 ausgeführt und 16 übersprungen, 18 fehlgeschlagen. Auf master: 1097 gesamt, 1081 ausgeführt, 16 übersprungen, 18 fehlgeschlagen. Neu ist 1 Test.
- Die fehlgeschlagenen Tests sind genau dieselben wie auf master: FtpTest und SftpTest in j-lawyer-server-common brauchen einen echten FTP- bzw. SFTP-Server mit Zugangsdaten aus Umgebungsvariablen. SmbTest braucht die im Test fest eingetragene SMB-Freigabe smb://j-lawyer-box/j-lawyer-share/. `LibreOfficeODFTest.setReplaceEmptyLine` in j-lawyer-server-ejb ist ein bekannter Fehler auf master (behoben durch den offenen PR #3703).

## Nicht abgedeckt
- Nicht in einem laufenden Client gegen einen Server ausprobiert. Für den Dialog selbst gibt es keinen automatischen Test, er holt seine Dienste über den `JLawyerServiceLocator` vom Server.
- Durch das Speichern vor dem Bestätigen bleiben diese Eingaben auch dann gespeichert, wenn der Dialog danach mit „Abbrechen“ geschlossen wird. Belegart, Nummernkreis und Nummer wurden an dieser Stelle schon bisher sofort gespeichert.

Closes #3371

🤖 Generated with [Claude Code](https://claude.com/claude-code)
