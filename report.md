# Report #1283: Adressen - neu nach Speichern über den Etiketten-Reiter

- Branch: `fix/1283-new-contact-after-tag-save` (Basis: upstream/master 7b3b3dfa8)
- Commit (Head): `8fde5cda0` (nach dem Autor-Umschreiben durch den Orchestrator, 02.11.)
- Status: fertig. Autor ist jetzt derspotter (vom Orchestrator umgeschrieben, 02.11.). Die Commit-Nachricht braucht keine Korrektur.

## Nachtrag 01.11.2026 (Codex-Review, Autor)
- 02.11.: Der Orchestrator hat den Autor auf derspotter umgeschrieben (Head `8fde5cda0`). Damit ist der Nachtrag vom 01.11. für diesen Branch erledigt.
- Autor auf `derspotter <jayjag@posteo.de>` umschreiben (amend + `git push --force-with-lease`): in der Cloud-Session vom Auto-Mode-Klassifikator als destruktive Git-Aktion abgelehnt. Nicht umgangen. Der Branch hat deshalb noch den Commit mit Autor Claude.
- Code unverändert, Head weiterhin `fd36c46` (Autor Claude). Die Commit-Nachricht enthält keine Build-Aussage, braucht also keine Korrektur.
- pr.md: `## Verifiziert` nennt jetzt den genauen Befehl inkl. `-Dmaven.test.failure.ignore=true` und Compiler-Overrides, das JDK-Setup und dass der Build nur wegen ignorierter Testfehler durchläuft (einzige fehlschlagende Klassen FtpTest/SftpTest/SmbTest wie auf master).

### Für das Umschreiben (lokal im Orchestrator)
Nur Autor ändern, Nachricht unverändert: `git commit --amend --no-edit --author="derspotter <jayjag@posteo.de>"` und `git push --force-with-lease`.

## Dateien
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/EditorsRegistry.java` +20 -1
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/ResetOnDisplayEditor.java` +11 -0
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/addresses/AddressPanel.java` +8 -0
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/addresses/NewAddressPanel.java` +6 -0
- `j-lawyer-client/src/test/java/com/jdimension/jlawyer/client/editors/EditorsRegistryReselectTest.java` +767 -0

Gesamt: 5 files changed, 812 insertions(+), 1 deletion(-). Davon sind 663 Zeilen der AGPL-Lizenzkopf des neuen Tests (wie bei den übrigen Client-Tests unter `src/test/java/org/jlawyer/...`), der Test selbst hat 104 Zeilen. Produktivcode: 45 Zeilen hinzu, 1 entfernt.

## Build und Tests
Befehl: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`

- Baseline (upstream/master): BUILD SUCCESS. Fehlschlagende Testklassen: FtpTest, SftpTest, SmbTest. Client: 81 Tests, 0 Fehler.
- Mit Fix: BUILD SUCCESS. Gleiche fehlschlagende Klassen. Client: 85 Tests, 0 Fehler (neu: EditorsRegistryReselectTest, 4 Tests).
- Hinweis: ein erster Lauf ohne `clean` zeigte 4 Fehler in UserUtilsTest. Das war eine veraltete .class aus dem #2151-Build in target/test-classes, nicht dieser Branch. Der Lauf mit `clean` ist sauber.

JDK wie bei #2151 beschrieben (Ubuntu OpenJDK 17 plus OpenJFX 17.0.9 per jlink, `--release` durch source/target 17 ersetzt).

## Nicht verifiziert
- Kein laufender Client. Ablauf aus dem Issue nicht durchgeklickt. Screenshots im Issue nicht einsehbar (Bild-Host im Proxy gesperrt). Annahme: Schritt 5/6 ist die confirmSave-Rückfrage "Bevor Etiketten aktiviert werden können ... Jetzt speichern?" mit Ja. Das ist der einzige Weg, auf dem ein NewAddressPanel eine gespeicherte Adresse behält (`AddressTagActionListener` ist der einzige Aufrufer von `AddressPanel.confirmSave`).
- Der Pfad mit Rückfrage (isDirty true) ist nicht automatisch getestet (JOptionPane headless nicht möglich). Nach dem Speichern über den Etiketten-Reiter ist `NewAddressPanel.isDirty()` immer true, die Rückfrage kommt also in diesem Fall immer.

## Offene Fragen
- Die Rückfrage "Daten vor dem Verlassen speichern?" erscheint auch, wenn seit dem Speichern nichts geändert wurde, weil `NewAddressPanel.isDirty()` nur auf nicht leere Felder prüft. Alternative wäre, ohne Rückfrage zurückzusetzen. Dann gingen aber Änderungen nach dem Etiketten-Speichern still verloren. Ich habe mich für die Rückfrage entschieden (gleiches Verhalten wie beim Modulwechsel).
- Schließen des Dialogs (X) bricht hier ab, beim normalen Modulwechsel gilt X dagegen wie Nein. Bewusst vorsichtiger gewählt.
- `NewArchiveFilePanel` hat vermutlich dasselbe Problem (`ArchiveFilePanel.confirmSave` zeigt die neu angelegte Akte im NewArchiveFilePanel). Nicht geändert, eigener Folge-PR möglich.
