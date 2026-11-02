feat(documents): Dateinamen beim Kopieren/Verschieben in eine andere Akte beibehalten

## Problem
Beim Kopieren oder Verschieben mehrerer Dokumente in eine andere Akte fragt j-lawyer für jedes Dokument einzeln nach dem Dateinamen. Beim Verschieben wird zusätzlich das Standard-Namensschema vorangestellt. Meist sind die Dokumente in der Ausgangsakte aber schon sinnvoll benannt, und die Namen sollen einfach erhalten bleiben.

## Ursache
`ArchiveFilePanel.copyOrMoveDocumentsToOtherCase()` ruft für jedes ausgewählte Dokument `FileUtils.getNewFileName()` auf, beim Verschieben mit `applyNameTemplate=true`. Einen Weg, die vorhandenen Namen für alle Dokumente zu übernehmen, gibt es nicht. Der Server (`copyDocumentsToCase` bzw. `moveDocumentsToCase`) verwendet ohne neuen Namen bereits den bisherigen Namen.

## Fix
- Nach der Wahl der Zielakte fragt j-lawyer einmal: "Sollen die vorhandenen Dateinamen beibehalten werden?" mit "Beibehalten", "Neu benennen" und "Abbrechen".
- "Beibehalten": Alle Dokumente behalten ihren Namen, ohne Namensschema. Nur wenn die Zielakte den Namen schon hat (auch im Papierkorb, geprüft mit `doesDocumentExist`) oder ein vorheriges Dokument desselben Vorgangs ihn bekommt, erscheint der bekannte Dialog für einen neuen Namen (ohne Namensschema), so lange, bis der Name frei ist. "Abbrechen" oder Escape in diesem Dialog bricht den ganzen Vorgang ab, bevor kopiert oder verschoben wird.
- "Neu benennen": unverändertes bisheriges Verhalten (Dialog pro Dokument, beim Verschieben mit Namensschema).
- Neue Hilfsmethoden in `FileUtils`: `keepFileNameUnlessTaken()` (Name behalten, solange er frei ist, sonst neu fragen) und `keepFileNames()` (dasselbe für alle Namen eines Vorgangs). Namen werden dabei ohne Berücksichtigung von Groß- und Kleinschreibung verglichen, mit `String.CASE_INSENSITIVE_ORDER`, also wie `equalsIgnoreCase` in `doesDocumentExist` und unabhängig von der Default-Locale.
- `FileUtils.getNewFileName()` bekommt eine Variante mit `escapeCancels`. Ist es gesetzt, liefert Escape `null`. Die bisherige Signatur ruft sie mit `false` auf, für alle anderen Aufrufer übernimmt Escape also wie bisher den angezeigten Namen.

Die Wahl gilt für einen Vorgang. Eine dauerhafte Einstellung ist bewusst nicht eingebaut (keine neuen Einstellungen, keine Formularänderung).

## Verifiziert
- Neue Tests in `FileUtilsTest`: ein freier Name bleibt ohne Rückfrage, ein belegter Name wird so lange neu erfragt, bis die Antwort frei ist, Abbrechen (null oder leer) liefert null, ein von einem früheren Dokument des Vorgangs belegter Name (auch ein umbenannter) wird neu erfragt, Namen, die sich nur in der Schreibweise unterscheiden ("I.pdf" und "i.pdf" unter türkischer Locale, "Ärger.pdf" und "ärger.pdf"), gelten als gleich.
- Escape, OK und "Abbrechen" im echten Dialog von `getNewFileName()` unter Xvfb geprüft (Scratch-Programm, nicht committet, Aktion über die Escape-Belegung des JOptionPane bzw. die Knöpfe ausgelöst, je zweimal): mit `escapeCancels=true` liefert Escape `null`, ohne liefert Escape wie bisher den angezeigten Namen, OK liefert den Namen, "Abbrechen" liefert `null`.
- Build: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- JDK: Ubuntu OpenJDK 17.0.20 und OpenJFX 17.0.9 aus Maven Central, per jlink zu einem JDK zusammengefügt, weil die BellSoft-Hosts im Cloud-Proxy gesperrt waren. Mit diesem JDK sieht `--release 17` die JavaFX-Module nicht, deshalb `source`/`target` 17 statt `release`.
- Ergebnis: Der Build läuft durch, weil Testfehler mit `-Dmaven.test.failure.ignore=true` ignoriert werden. Ausgeführt 272 Tests, fehlgeschlagen 17, Fehler 0, übersprungen 0. Alle 17 Fehlschläge sind FtpTest (6), SftpTest (6) und SmbTest (5) in j-lawyer-server-common, die FTP-, SFTP- und SMB-Server brauchen, genau wie auf master. Client: 85 ausgeführt, 0 fehlgeschlagen, 0 übersprungen (master: 81).

## Nicht abgedeckt
- Nicht im laufenden Client gegen einen Server ausprobiert. Der Ablauf in `ArchiveFilePanel` (Auswahl, Konfliktdialoge, Kopieren bzw. Verschieben) ist nicht automatisch getestet, nur die Hilfsmethoden und der Dialog selbst. Dass ein Abbruch das Kopieren bzw. Verschieben verhindert, ergibt sich aus dem Code (`return` vor dem Serveraufruf).
- Die Prüfung gegen die Zielakte läuft wie das Kopieren bzw. Verschieben selbst im Event-Thread, mit einer Serverabfrage pro Dokument. Bei sehr vielen Dokumenten kann der Client dabei kurz hängen, gemessen ist es nicht.
- Die neue Frage erscheint auch bei einem einzelnen Dokument. Mit "Beibehalten" bleibt es bei einem Klick, mit "Neu benennen" ist es ein Klick mehr als bisher.

Closes #3407

🤖 Generated with [Claude Code](https://claude.com/claude-code)
