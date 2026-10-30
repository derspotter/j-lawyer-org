# Report: FolderListCell.drop() ohne return nach rejectDrop()

- Branch: `fix/folderlistcell-reject-drop` (Basis: upstream/master 7b3b3dfa8)
- Commit: `6cf6849a1b311da2552c881e3f852d9902702f15`
- Status: fertig, bereit für Codex-Review. Kein Issue.

## Dateien
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/ui/folders/FolderListCell.java` +2 -0

Gesamt: 1 file changed, 2 insertions(+).

## Build und Tests
Befehl: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`

- Baseline (upstream/master): BUILD SUCCESS. Fehlschlagende Testklassen: FtpTest, SftpTest, SmbTest. Client: 81 Tests, 0 Fehler.
- Mit Fix: BUILD SUCCESS. Gleiche fehlschlagende Klassen. Client: 81 Tests, 0 Fehler. Kein neuer Test (siehe unten).

JDK wie bei #2151 beschrieben (Ubuntu OpenJDK 17 plus OpenJFX 17.0.9 per jlink, `--release` durch source/target 17 ersetzt).

## Abweichung von der Aufgabenbeschreibung
Die Aufgabe sagt, die Dokumente würden trotz rejectDrop() verschoben. Mit den JDK-Peers stimmt das so nicht: `javap -c` auf JDK 17 zeigt, dass `DropTargetContext.acceptDrop` an den Peer delegiert und `SunDropTargetContextPeer.acceptDrop` bei dropStatus != WAIT/ACCEPT eine `InvalidDnDOperationException("invalid acceptDrop()")` wirft. `rejectDrop` setzt den Status auf REJECT. `XDropTargetContextPeer` (Linux) überschreibt beide Methoden nicht. Damit bricht der Ablauf vor `moveDocumentsToFolder()` ab. Tatsächliche Folge: Fehler im Log (catch-Block), zweiter `rejectDrop()` wirft unbehandelt aus `drop()` heraus. pr.md beschreibt das so und erwähnt das Verschieben nur als Folge bei einem nachsichtigeren Peer. Der Fix ist derselbe.

## Nicht verifiziert
- Kein automatischer Test möglich: `new DropTarget(...)` im Konstruktor von FolderListCell und jeder `DropTargetContext` werfen headless `HeadlessException`. Kein Mockito im Client. Eine Robot-Prüfung unter Xvfb wäre aufwendig und wackelig.
- Windows-/macOS-Peers (WDropTargetContextPeer, CDropTargetContextPeer) nicht im Linux-JDK enthalten, also nicht nachgeprüft.
- Wie der j-lawyer-Client eine unbehandelte Exception im EDT anzeigt (nur Log oder Dialog), nicht geprüft.

## Offene Fragen
- Soll `dragEnter/dragOver` bei schreibgeschützten Ordnern gleich `rejectDrag()` aufrufen, damit der Mauszeiger schon beim Ziehen "nicht erlaubt" zeigt? Bewusst nicht Teil dieses Fixes.
- Commit-Autor ist die Git-Identität der Cloud-Session (Claude), mit Co-Authored-By-Zeile.
