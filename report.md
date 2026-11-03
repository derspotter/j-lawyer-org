# Report: FolderListCell.drop() ohne return nach rejectDrop()

- Branch: `fix/folderlistcell-reject-drop` (Basis: upstream/master 7b3b3dfa8)
- Commit (Head): `df9e0fa6c` (Autor und Commit-Nachricht vom Orchestrator umgeschrieben, 03.11., Baum unverändert)
- Status: fertig. Autor derspotter, Commit-Nachricht korrigiert (beides vom Orchestrator, 03.11.).

## Nachtrag 01.11.2026 (Codex-Review, Autor)
- 03.11.: Der Orchestrator hat Autor und Commit-Nachricht umgeschrieben (Head `df9e0fa6c`, Baum identisch mit dem vorherigen Stand). Damit ist der Nachtrag erledigt.
- Autor auf `derspotter <jayjag@posteo.de>` umschreiben (amend + `git push --force-with-lease`): in der Cloud-Session vom Auto-Mode-Klassifikator als destruktive Git-Aktion abgelehnt. Nicht umgangen. Der Branch hat deshalb noch den Commit mit Autor Claude.
- Code unverändert, Head weiterhin `6cf6849` (Autor Claude). Die Commit-Nachricht beschreibt noch den falschen Ablauf (zweite, unbehandelte Exception). Erledigt am 03.11. durch den Orchestrator (Text siehe unten).
- pr.md: `## Verifiziert` nennt jetzt den genauen Befehl inkl. `-Dmaven.test.failure.ignore=true` und Compiler-Overrides, das JDK-Setup und dass der Build nur wegen ignorierter Testfehler durchläuft (einzige fehlschlagende Klassen FtpTest/SftpTest/SmbTest wie auf master). pr.md: Ursache und Fix nach dem Review neu beschrieben, siehe Abschnitt "Korrektur der Analyse".

### Für das Umschreiben (lokal im Orchestrator)
Autor derspotter, Nachricht:

```
fix(documents): Ablegen auf schreibgeschützten Ordner bzw. ohne Akte sauber abweisen.

FolderListCell.drop() rief für einen schreibgeschützten Ordner oder
ohne geladene Akte dtde.rejectDrop() auf, arbeitete danach aber weiter.
rejectDrop() setzt den DropTargetContext zurück, das folgende
getTransferable() wirft eine InvalidDnDOperationException, die der
catch-Block protokolliert. Nach dem Abweisen wird jetzt direkt
zurückgekehrt, das vermeidet die protokollierte Exception.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
```

## Dateien
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/ui/folders/FolderListCell.java` +2 -0

Gesamt: 1 file changed, 2 insertions(+).

## Build und Tests
Befehl: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`

- Baseline (upstream/master): BUILD SUCCESS. Fehlschlagende Testklassen: FtpTest, SftpTest, SmbTest. Client: 81 Tests, 0 Fehler.
- Mit Fix: BUILD SUCCESS. Gleiche fehlschlagende Klassen. Client: 81 Tests, 0 Fehler. Kein neuer Test (siehe unten).

JDK wie bei #2151 beschrieben (Ubuntu OpenJDK 17 plus OpenJFX 17.0.9 per jlink, `--release` durch source/target 17 ersetzt).

## Korrektur der Analyse
Meine erste Beschreibung war falsch. Ich hatte `getTransferable()` und das Zurücksetzen des Contexts übersehen. Der Review hatte recht, per `javap -c` auf JDK 17 nachgeprüft:
- `DropTargetContext.rejectDrop()` ruft `peer.rejectDrop()`, das setzt dropStatus auf REJECT und ruft `dropComplete(false)`.
- `SunDropTargetContextPeer.dropComplete` setzt über `AWTAccessor...reset(currentDTC)` den Context zurück: `DropTargetContext.reset()` setzt `dropTargetContextPeer` und `transferable` auf null. dropStatus wird NONE.
- `DropTargetContext.getTransferable()` wirft bei Peer null `new InvalidDnDOperationException()`. Das passiert im try-Block vor `isDataFlavorSupported`/`acceptDrop`, der catch-Block protokolliert es.
- `DropTargetContext.rejectDrop()` tut bei Peer null nichts. Der zweite Check und der rejectDrop() im catch-Block sind also No-ops.
Folge vor dem Fix: genau eine protokollierte Exception pro abgewiesenem Drop, nichts wird verschoben oder hochgeladen, nichts verlässt `drop()`. Ein Umgehen des Schreibschutzes gibt es nicht. Die Aufgabenbeschreibung ("verschiebt trotzdem") trifft damit nicht zu, der Fix bleibt derselbe.

## Nicht verifiziert
- Kein automatischer Test möglich: `new DropTarget(...)` im Konstruktor von FolderListCell und jeder `DropTargetContext` werfen headless `HeadlessException`. Kein Mockito im Client. Eine Robot-Prüfung unter Xvfb wäre aufwendig und wackelig.
- Windows-/macOS-Peers (WDropTargetContextPeer, CDropTargetContextPeer) nicht im Linux-JDK enthalten, also nicht nachgeprüft.

## Offene Fragen
- Soll `dragEnter/dragOver` bei schreibgeschützten Ordnern gleich `rejectDrag()` aufrufen, damit der Mauszeiger schon beim Ziehen "nicht erlaubt" zeigt? Bewusst nicht Teil dieses Fixes.
