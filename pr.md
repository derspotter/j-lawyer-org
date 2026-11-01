feat(documents): Dateinamen beim Kopieren/Verschieben in eine andere Akte beibehalten

## Problem
Beim Kopieren oder Verschieben mehrerer Dokumente in eine andere Akte fragt j-lawyer für jedes Dokument einzeln nach dem Dateinamen. Beim Verschieben wird zusätzlich das Standard-Namensschema vorangestellt. Meist sind die Dokumente in der Ausgangsakte aber schon sinnvoll benannt, und die Namen sollen einfach erhalten bleiben.

## Ursache
`ArchiveFilePanel.copyOrMoveDocumentsToOtherCase()` ruft für jedes ausgewählte Dokument `FileUtils.getNewFileName()` auf, beim Verschieben mit `applyNameTemplate=true`. Einen Weg, die vorhandenen Namen für alle Dokumente zu übernehmen, gibt es nicht. Der Server (`copyDocumentsToCase` bzw. `moveDocumentsToCase`) verwendet ohne neuen Namen bereits den bisherigen Namen.

## Fix
- Nach der Wahl der Zielakte fragt j-lawyer einmal: "Sollen die vorhandenen Dateinamen beibehalten werden?" mit "Beibehalten", "Neu benennen" und "Abbrechen".
- "Beibehalten": Alle Dokumente behalten ihren Namen, ohne Namensschema. Nur wenn die Zielakte den Namen schon hat (auch im Papierkorb, geprüft mit `doesDocumentExist`, Groß-/Kleinschreibung egal) oder ein vorheriges Dokument desselben Vorgangs ihn bekommt, erscheint der bekannte Dialog für einen neuen Namen (ohne Namensschema), so lange, bis der Name frei ist. Abbrechen dort bricht den ganzen Vorgang ab, wie bisher.
- "Neu benennen": unverändertes bisheriges Verhalten (Dialog pro Dokument, beim Verschieben mit Namensschema).
- Die Schleife "Name behalten, solange er frei ist, sonst neu fragen" steckt in der neuen Hilfsmethode `FileUtils.keepFileNameUnlessTaken()`, damit sie testbar ist.

Die Wahl gilt für einen Vorgang. Eine dauerhafte Einstellung ist bewusst nicht eingebaut (keine neuen Einstellungen, keine Formularänderung).

## Verifiziert
- Neuer Test `FileUtilsTest.testKeepFileNameUnlessTaken`: ein freier Name bleibt ohne Rückfrage, ein belegter Name wird so lange neu erfragt, bis die Antwort frei ist, Abbrechen (null oder leer) liefert null.
- `git merge-tree` gegen alle Branches im Fork, die `ArchiveFilePanel.java` oder `FileUtils.java` ändern (nur `fix/duplicate-document-keep-metadata`): ohne Konflikt.
- Build: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- JDK: Ubuntu OpenJDK 17.0.20 und OpenJFX 17.0.9 aus Maven Central, per jlink zu einem JDK zusammengefügt, weil die BellSoft-Hosts im Cloud-Proxy gesperrt waren. Mit diesem JDK sieht `--release 17` die JavaFX-Module nicht, deshalb `source`/`target` 17 statt `release`.
- Ergebnis: Der Build läuft durch, weil Testfehler mit `-Dmaven.test.failure.ignore=true` ignoriert werden. Fehlschlagende Testklassen sind nur FtpTest, SftpTest und SmbTest in j-lawyer-server-common (sie brauchen FTP-, SFTP- und SMB-Server), genau wie auf master. Client: 82 Tests, 0 Fehler (master: 81).

## Nicht abgedeckt
- Nicht im laufenden Client gegen einen Server ausprobiert. Der Dialog-Ablauf in `ArchiveFilePanel` ist nicht automatisch getestet, nur die Hilfsmethode.
- Die neue Frage erscheint auch bei einem einzelnen Dokument. Mit "Beibehalten" bleibt es bei einem Klick, mit "Neu benennen" ist es ein Klick mehr als bisher.
- Pro Dokument eine Serverabfrage `doesDocumentExist`. Bei sehr vielen Dokumenten in der Zielakte könnte das spürbar sein, gemessen ist es nicht.
- Die Prüfung ignoriert Groß-/Kleinschreibung. Der Server vergleicht beim Anlegen per Datenbankabfrage auf den Namen, ob Groß-/Kleinschreibung dort zählt, hängt von der Kollation der Datenbank ab. Im Zweifel wird also eher einmal zu viel nach einem Namen gefragt.

Closes #3407

🤖 Generated with [Claude Code](https://claude.com/claude-code)
