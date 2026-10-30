fix(documents): Ablegen auf schreibgeschützten Ordner bzw. ohne Akte sauber abweisen

## Problem
`FolderListCell.drop()` ruft für einen schreibgeschützten Ordner (Akte nur lesend geöffnet oder archiviert) und für den Fall ohne geladene Akte `dtde.rejectDrop()` auf, kehrt danach aber nicht zurück. Der Code läuft weiter in den Zweig für verschobene Dokumente bzw. Dateien und ruft dort `dtde.acceptDrop()` auf.

## Ursache
Fehlendes `return` nach den beiden `rejectDrop()`-Aufrufen.

Was dann passiert, hängt am DnD-Peer des JDK. In `SunDropTargetContextPeer` (Basisklasse der Plattform-Peers, unter Linux erbt `XDropTargetContextPeer` die Methoden unverändert) wirft `acceptDrop()` nach einem `rejectDrop()` eine `InvalidDnDOperationException("invalid acceptDrop()")`. Die Dokumente werden dadurch nicht verschoben, weil `acceptDrop()` vor `moveDocumentsToFolder()` bzw. vor dem Hochladen steht. Der catch-Block protokolliert die Exception als Fehler und ruft `rejectDrop()` ein zweites Mal auf. Das wirft erneut (`invalid rejectDrop()`), und diese Exception verlässt `drop()` unbehandelt. Ist der Ordner schreibgeschützt und zugleich keine Akte geladen, wirft schon der zweite `rejectDrop()` vor dem try-Block.

Ein Ablegen auf einen schreibgeschützten Ordner führt also zu Fehlern im Log und einer unbehandelten Exception im Event-Thread statt zu einem sauberen Abweisen. Hinge der Ablauf an einem Peer, der `acceptDrop()` nach `rejectDrop()` zulässt, würden die Dokumente trotz Schreibschutz verschoben.

## Fix
Nach beiden `rejectDrop()`-Aufrufen direkt `return`. Zwei Zeilen.

## Verifiziert
- `mvn -pl j-lawyer-client -am clean package` mit Tests: BUILD SUCCESS. Es scheitern nur FtpTest, SftpTest und SmbTest wie auf master (brauchen externe Zugangsdaten).
- Das Verhalten von `acceptDrop()`/`rejectDrop()` nach einem `rejectDrop()` ist am Bytecode von `java.awt.dnd.DropTargetContext` und `sun.awt.dnd.SunDropTargetContextPeer` aus JDK 17 nachgeprüft.

## Nicht abgedeckt
- Kein automatischer Test: `FolderListCell` legt im Konstruktor `DropTarget`s an, und ein `DropTargetDropEvent` braucht einen `DropTargetContext` aus einem `DropTarget`. Beides wirft headless eine `HeadlessException`, und eine Mock-Bibliothek gibt es im Client nicht.
- Nicht im laufenden Client per Drag and Drop ausprobiert. Die Peers für Windows und macOS sind in einem Linux-JDK nicht enthalten und nicht nachgeprüft.

Kein zugehöriges Issue.

🤖 Generated with [Claude Code](https://claude.com/claude-code)
