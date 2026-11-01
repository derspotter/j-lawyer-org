fix(documents): Ablegen auf schreibgeschützten Ordner bzw. ohne Akte sauber abweisen

## Problem
`FolderListCell.drop()` ruft für einen schreibgeschützten Ordner (Akte nur lesend geöffnet oder archiviert) und für den Fall ohne geladene Akte `dtde.rejectDrop()` auf, kehrt danach aber nicht zurück. Die Methode arbeitet weiter, als wäre der Drop angenommen, und jedes solche Ablegen hinterlässt eine Exception im Log.

## Ursache
Fehlendes `return` nach den beiden `rejectDrop()`-Aufrufen.

Laut Bytecode von JDK 17 schließt `rejectDrop()` den Drop über `dropComplete(false)` ab und setzt dabei den `DropTargetContext` zurück (Peer und Transferable werden `null`). Das anschließende `dtde.getTransferable()` im try-Block wirft deshalb eine `InvalidDnDOperationException`, die der catch-Block als Fehler protokolliert. `acceptDrop()` wird nie erreicht, verschoben oder hochgeladen wird nichts. Weitere `rejectDrop()`-Aufrufe (der zweite Check oder der catch-Block) laufen ins Leere, weil der Context keinen Peer mehr hat.

## Fix
Nach beiden `rejectDrop()`-Aufrufen direkt `return`. Die Verarbeitung endet also mit dem Abweisen, das vermeidet die protokollierte Exception. Zwei Zeilen.

## Verifiziert
- Ablauf an JDK-17-Bytecode nachgeprüft: `DropTargetContext.rejectDrop` und `getTransferable`, `SunDropTargetContextPeer.rejectDrop` und `dropComplete` (unter Linux erbt `XDropTargetContextPeer` diese Methoden unverändert).
- Build: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- JDK: Ubuntu OpenJDK 17.0.20 und OpenJFX 17.0.9 aus Maven Central, per jlink zu einem JDK zusammengefügt, weil die BellSoft-Hosts im Cloud-Proxy gesperrt waren. Mit diesem JDK sieht `--release 17` die JavaFX-Module nicht, deshalb `source`/`target` 17 statt `release`.
- Ergebnis: Der Build läuft durch, weil Testfehler mit `-Dmaven.test.failure.ignore=true` ignoriert werden. Fehlschlagende Testklassen sind nur FtpTest, SftpTest und SmbTest in j-lawyer-server-common (sie brauchen FTP-, SFTP- und SMB-Server), genau wie auf master. Client: 81 Tests, 0 Fehler (master: 81).

## Nicht abgedeckt
- Kein automatischer Test: `FolderListCell` legt im Konstruktor `DropTarget`s an, und ein `DropTargetDropEvent` braucht einen `DropTargetContext` aus einem `DropTarget`. Beides wirft headless eine `HeadlessException`, und eine Mock-Bibliothek gibt es im Client nicht.
- Nicht im laufenden Client per Drag and Drop ausprobiert. Die Peers für Windows und macOS sind in einem Linux-JDK nicht enthalten und nicht nachgeprüft.

Kein zugehöriges Issue.

🤖 Generated with [Claude Code](https://claude.com/claude-code)
