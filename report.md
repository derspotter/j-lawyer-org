# Report #3407: Dateinamen beim Kopieren/Verschieben beibehalten

- Branch: `fix/3407-keep-file-names-copy-move` (ursprüngliche Basis: upstream/master 7b3b3dfa8, am 03.11. mit master 4dc03980f zusammengeführt)
- Commits: `1f4955c` (Feature), `2c94a25` (Review-Punkte 02.11.), `f1b7eb1` (Merge von master 4dc03980f), Head `f1b7eb1`
- Autor aller drei Commits: derspotter <jayjag@posteo.de>, Co-Authored-By Claude
- Status: Review-Punkte 1 und 3 umgesetzt, Punkt 2 beantwortet (siehe unten). Bereit für erneuten Codex-Review und lokalen Liberica-Build.

## Antwort auf Review 02.11.

### Punkt 1 (Escape bricht den Konfliktdialog nicht ab): umgesetzt
Stimmt. `getNewFileName()` wertet `pane.getValue()` nicht aus. Escape löst die Belegung "close" des JOptionPane aus, setzt den Wert auf `CLOSED_OPTION` und schließt den Dialog, der Text bleibt stehen, also kam der belegte Name zurück und die Schleife fragte erneut. Nur der eigene Knopf "Abbrechen" des Panels leert das Feld und bricht ab.
Fix in `2c94a25`: neue Variante `getNewFileName(..., boolean escapeCancels)`, die bei `CLOSED_OPTION` `null` liefert. Nur der Konfliktdialog nutzt sie (`true`). Die bisherige Signatur delegiert mit `false`, alle anderen Aufrufer behalten ihr Verhalten.
Geprüft unter Xvfb mit einem Scratch-Programm, das den echten Dialog öffnet und per javax.swing.Timer im Event-Thread die Escape-Aktion aus der InputMap des JOptionPane bzw. die Knöpfe OK und "Abbrechen" auslöst, zwei Durchläufe je Fall, alle gleich:
- escapeCancels=true, Escape: null
- escapeCancels=false, Escape: brief.pdf (bisheriges Verhalten, bestätigt den Befund)
- OK (beide): brief.pdf
- Abbrechen (beide): null
Ein erster Versuch mit java.awt.Robot bzw. mit Aufrufen aus einem fremden Thread blieb unter Xvfb (ohne Fenstermanager) teils im modalen Loop hängen. Das lag am Testaufbau, mit dem Timer im EDT ist es deterministisch.
Dass ein Abbruch das Kopieren/Verschieben verhindert: `keepFileNames()` liefert dann `null`, `copyOrMoveDocumentsToOtherCase()` kehrt vor `moveDocumentsToCase`/`copyDocumentsToCase` zurück. Per Code-Lesen, nicht automatisch getestet (ArchiveFilePanel braucht einen Server).

### Punkt 2 (Serverprüfungen blockieren den EDT): nicht geändert, Begründung
- Die Methode führt schon heute `getDocumentsOpenForWrite`, `optionalUnarchiveCase` und vor allem `copyDocumentsToCase`/`moveDocumentsToCase` (Inhalt jedes Dokuments lesen und neu schreiben) synchron im EDT aus. Die neue Prüfung ist pro Dokument ein `doesDocumentExist` (Server: zwei Abfragen auf die Dokumente der Zielakte, Rückgabe boolean) und damit deutlich leichter als das Kopieren selbst.
- Nur die Prüfung in einen Worker zu verlagern, beseitigt das Hängen beim eigentlichen Kopieren nicht und würde den Ablauf zerreißen (Konfliktdialoge müssen im EDT dazwischen). Sauber wäre, den ganzen Vorgang in eine ProgressableAction zu verlagern. Das ist ein eigener Umbau und nicht Teil dieses Issues.
- Namen einmal laden geht mit der Remote-API nicht billiger: `getDocuments(caseId)` liefert nur aktive Dokumente, `getDocumentsBin()` lädt serverseitig alle gelöschten Dokumente des Systems (findDeleted) und prüft für jedes die Berechtigung. Der Papierkorb muss aber geprüft werden, weil der Server beim Anlegen auch dort nach dem Namen sucht und sonst mitten im Vorgang mit einer geprüften Exception abbricht (vermutlich ohne Rollback der schon angelegten Kopien, nicht nachgeprüft).
- Vorschlag: Folge-PR, der copy/move samt Prüfung in eine ProgressableAction verlegt, falls Jens das möchte.

### Punkt 3 (Locale-abhängiges toLowerCase): umgesetzt
Stimmt. Neu in `2c94a25`: `FileUtils.keepFileNames()` reserviert die Namen eines Vorgangs in einem `TreeSet` mit `String.CASE_INSENSITIVE_ORDER` (zeichenweise wie `equalsIgnoreCase`, unabhängig von der Default-Locale). ArchiveFilePanel nutzt die Methode statt der eigenen Schleife. Tests: früher belegte Namen (auch ein umbenannter, der mit einem späteren Quellnamen kollidiert), Schreibweisen-Varianten, unter Locale tr-TR "I.pdf"/"i.pdf" (mit dem alten toLowerCase wären sie verschieden) und "Ärger.pdf"/"ärger.pdf", Abbruch.

## Merge von master (03.11.)
master bekam am 02.11. bc3a1497b (macOS-Menüleiste nach Dateinamen-Dialog, #3731), ee897221b und weitere Commits. bc3a1497b ändert `FileUtils.getNewFileName()` an derselben Stelle wie dieser Branch (direkt nach `dialog.setVisible(true)`). Wie gewünscht per Merge (kein Rebase, kein Force-Push) in `f1b7eb1` zusammengeführt.
- Konflikt nur in FileUtils.java. Auflösung: zuerst `dialog.dispose()` und die macOS-Reaktivierung des Besitzerfensters aus master, danach die Prüfung `escapeCancels`. So wird der Dialog auch beim Abbruch per Escape freigegeben, und die macOS-Korrektur greift auch dann.
- ArchiveFilePanel.java (master: 75c06fd4a) ließ sich automatisch zusammenführen. Der Unterschied zu master ist danach genau die Änderung dieses Branches (46 geänderte Zeilen wie vorher).
- Nach dem Merge: `git diff upstream/master` zeigt nur die Änderungen dieses Branches, `git merge-tree` gegen master Exit 0.
- Nach dem Merge neu gebaut (Tabelle unten, gleiche Zahlen wie vorher) und die Xvfb-Prüfung des Dialogs wiederholt: unverändert (escapeCancels=true + Escape: null, false + Escape: Name, OK: Name, Abbrechen: null).

## Dateien (gegen master 4dc03980f)
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/ArchiveFilePanel.java` +37 -9
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/utils/FileUtils.java` +75 -0
- `j-lawyer-client/src/test/java/org/jlawyer/test/client/utils/FileUtilsTest.java` +72 -0

Gesamt: 3 files changed, 184 insertions(+), 9 deletions(-).

## Build und Tests
Befehl: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`

JDK: Ubuntu OpenJDK 17.0.20 plus OpenJFX 17.0.9 per jlink (BellSoft-Hosts im Proxy gesperrt), `--release` durch source/target 17 ersetzt.

Ergebnis Head `f1b7eb1` (nach dem Merge, 03.11.): Build läuft durch, weil Testfehler ignoriert werden. Gleiche Zahlen wie für `2c94a25` vor dem Merge.

| Modul | ausgeführt | fehlgeschlagen | Fehler | übersprungen |
|---|---|---|---|---|
| j-lawyer-fax | 23 | 0 | 0 | 0 |
| j-lawyer-server-common | 104 | 17 | 0 | 0 |
| j-lawyer-cloud | 4 | 0 | 0 | 0 |
| j-lawyer-server-entities | 56 | 0 | 0 | 0 |
| j-lawyer-client | 85 | 0 | 0 | 0 |
| Summe | 272 | 17 | 0 | 0 |

j-lawyer-invoicing und j-lawyer-server-api haben keine Tests. Die 17 Fehlschläge sind FtpTest (6), SftpTest (6), SmbTest (5), wie auf master (dort Client 81 Tests). Neu im Client: FileUtilsTest von 3 auf 6 Tests (testKeepFileNameUnlessTaken, testKeepFileNamesReservesEarlierNames, testKeepFileNamesUnicodeCaseIndependentOfLocale, testKeepFileNamesCancel).

## Merge-Prüfung
`git merge-tree --write-tree` gegen alle Branches unter origin/fix/*, die ArchiveFilePanel.java oder FileUtils.java ändern (nur `fix/duplicate-document-keep-metadata`): Exit 0, für den Feature-Commit und erneut für Head `2c94a25`.

## Nicht im laufenden Client oder gegen einen Server ausprobiert
- Der ganze Ablauf in ArchiveFilePanel: Auswahl der Zielakte, Frage "Beibehalten/Neu benennen", Konfliktdialoge, Kopieren bzw. Verschieben, Verhalten bei Abbruch.
- `doesDocumentExist` gegen echte Zielakten mit Papierkorb-Einträgen.
- Laufzeit bei vielen Dokumenten.
- Unter Xvfb geprüft ist nur der Dialog von `getNewFileName()` selbst, ohne Server (NewFilenameOptionPanel protokolliert dabei nur, dass keine Serververbindung besteht).

## Offene Fragen
- Soll die Frage bei genau einem Dokument entfallen (dann nur der bisherige Einzeldialog)?
- Texte der Knöpfe und der Frage sind Vorschläge.
- Folge-PR für copy/move als ProgressableAction (siehe Punkt 2)?
