# Report #3407: Dateinamen beim Kopieren/Verschieben beibehalten

- Branch: `fix/3407-keep-file-names-copy-move` (Basis: upstream/master 7b3b3dfa8)
- Commit: `1f4955c16fe5fede3baccbe87774da3168fe7f65`
- Autor: derspotter <jayjag@posteo.de>, Co-Authored-By Claude
- Status: fertig, bereit für Codex-Review und lokalen Liberica-Build

## Dateien
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/ArchiveFilePanel.java` +24 -5
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/utils/FileUtils.java` +23 -0
- `j-lawyer-client/src/test/java/org/jlawyer/test/client/utils/FileUtilsTest.java` +25 -0

Gesamt: 3 files changed, 72 insertions(+), 5 deletions(-).

## Build und Tests
Befehl: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`

- Baseline (upstream/master): Build läuft durch (Testfehler ignoriert). Fehlschlagend nur FtpTest, SftpTest, SmbTest (17 von 104 in j-lawyer-server-common). Client: 81 Tests, 0 Fehler.
- Mit Fix: gleich. Client: 82 Tests, 0 Fehler (neu: FileUtilsTest.testKeepFileNameUnlessTaken).
- JDK: Ubuntu OpenJDK 17.0.20 plus OpenJFX 17.0.9 per jlink (BellSoft-Hosts im Proxy gesperrt), `--release` durch source/target 17 ersetzt.

## Merge-Prüfung
`git merge-tree --write-tree` gegen jeden Branch unter origin/fix/*, der ArchiveFilePanel.java oder FileUtils.java ändert: nur `fix/duplicate-document-keep-metadata`, Exit 0. Fünf ältere Branches meldeten zuerst "unrelated histories" (flacher Klon). Nach `git fetch --deepen=2000 upstream master` zeigte sich, dass sie diese Dateien gar nicht ändern.

## Designentscheidungen
- Einmalige Frage pro Vorgang statt dauerhafter Einstellung: das Issue wünscht eine Option, die "bei allen Dokumenten" gilt. Eine Einstellung bräuchte Settings-Key und UI, also mehr Diff. Kann nachgezogen werden, falls Jens das lieber hat.
- Bei "Beibehalten" kein Namensschema, auch nicht beim Verschieben. "Neu benennen" lässt das alte Verhalten (Schema beim Verschieben) unverändert.
- Namenskonflikt: `doesDocumentExist` (aktive Dokumente plus Papierkorb, case-insensitive) plus die schon vergebenen Namen des Vorgangs. Der Server prüft beim Anlegen per JPQL `name = ?2` (`findByArchiveFileKey(aFile, fileName)`, ohne Filter auf gelöscht, also inkl. Papierkorb). Ob dabei Groß-/Kleinschreibung zählt, hängt von der DB-Kollation ab. Bei einem Treffer wirft er mitten im Vorgang.

## Nicht verifiziert
- Kein laufender Client oder Server, Dialogfolge nicht durchgeklickt.
- Performance von `doesDocumentExist` pro Dokument bei großen Zielakten nicht gemessen.

## Offene Fragen
- Soll die Frage bei genau einem Dokument entfallen (dann nur der bisherige Einzeldialog)? Ich habe sie immer gestellt, damit auch beim einzelnen Verschieben das Namensschema vermieden werden kann.
- Texte der Knöpfe ("Beibehalten", "Neu benennen", "Abbrechen") und der Frage sind Vorschläge.
