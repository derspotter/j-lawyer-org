# Report #1283: Adressen - neu nach Speichern über den Etiketten-Reiter

- Branch: `fix/1283-new-contact-after-tag-save` (Basis: upstream/master 7b3b3dfa8)
- Commits: `8fde5cd` (Fix, Autor vom Orchestrator umgeschrieben), `729e3a1` (Review-Punkte 03.11.), `96ccf89` (fehlgeschlagenes Speichern, Review 03.11. (2)), Head `96ccf89`
- Status: Review 03.11. (2) umgesetzt (siehe "Antwort auf Review 03.11. (2)"). Bereit für erneuten Codex-Review und lokalen Liberica-Build.

## Antwort auf Review 03.11. (2)

### [P2] Fehlgeschlagenes Speichern macht ungespeicherte Änderungen scheinbar sauber: umgesetzt
Stimmt. `AddressPanel.save()` setzt `this.dto = new AddressBean()` und füllt es mit den Eingaben, bevor `updateAddress()` aufgerufen wird. Wirft der Server, blieb dieses ungespeicherte DTO stehen, `AddressPanel.isDirty()` verglich die Eingaben mit sich selbst, und ein zweites "Adressen - neu" setzte ohne Rückfrage zurück.
Fix in `96ccf89`: `save()` merkt sich vor dem Ersetzen das bisherige DTO (`lastSaved`) und stellt es im catch-Block wieder her. Danach vergleicht `isDirty()` wieder gegen den zuletzt gespeicherten Stand, die Änderungen gelten weiter als ungespeichert, und das erneute Wählen fragt wieder nach. Das betrifft alle Adress-Editoren gleich (auch die Bearbeiten-Ansicht hatte dieselbe Lücke in ihrer Dirty-Prüfung), andere Pfade von `save()` bleiben unverändert.
Test: `reselectingDirtyEditorKeepsItWhenSavingFails` wählt den Editor nach dem fehlgeschlagenen Speichern ein zweites Mal und prüft, dass wieder gefragt und nicht zurückgesetzt wird. Wie der Review richtig anmerkt, bleibt der Test-Editor dabei immer dirty. Er prüft also die Registry-Seite des Ablaufs, nicht die Wiederherstellung in `AddressPanel.save()`. Die lässt sich ohne Server nicht testen (AddressPanel ist ohne Server nicht instanziierbar, siehe P1 vom 03.11.).

### Build Head `96ccf89`
`mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`: 276 ausgeführt, 17 fehlgeschlagen (FtpTest 6, SftpTest 6, SmbTest 5, wie auf master), 0 Fehler, 0 übersprungen. Client 89 ausgeführt, 0 fehlgeschlagen (EditorsRegistryReselectTest 8). `git merge-tree` gegen den aktuellen master 913123ae9: Exit 0.

## Antwort auf Review 03.11.

### P1 (erneutes Wählen kann gespeicherte Auswahllisten leeren): umgesetzt
Stimmt. `ModuleButton.actionPerformed()` ruft `populateOptions()` vor `setMainEditorsPaneView()`. `AddressPanel.populateOptions()` setzt über `ComponentUtils.populateOptionsCombobox()` für zwölf Auswahllisten (Anrede, Grußformel, Titel, Titel in der Anschrift, Nationalität, Land, Bundesland, Rechtsform, Grade, Beruf, Rolle) neue Modelle, die Auswahl springt auf den leeren ersten Eintrag. Danach hätte "Ja" auf die neue Rückfrage diese leeren Werte über den Kontakt gespeichert.
Fix in `729e3a1`: `NewAddressPanel.populateOptions()` tut nichts, solange eine gespeicherte Adresse angezeigt wird (`isContactSaved()`). Nach dem Zurücksetzen ist die Adresse nicht mehr gespeichert, beim nächsten Anzeigen wird wieder normal befüllt. Andere Editoren und der normale Modulwechsel sind nicht betroffen.
Bewusst nicht in `ModuleButton` oder `ComponentUtils` gelöst: das würde das Verhalten aller Editoren bzw. aller Auswahllisten ändern.
Kein automatischer Test für diese Prüfung: `NewAddressPanel` lässt sich ohne Server nicht instanziieren (Konstruktor scheitert an `ServerSettings`, `this.mgmt` ist null, in einem Scratch-Programm ausprobiert). Die Prüfung ist eine einzelne Bedingung.

### P2 (Dirty-Prüfung kann Änderungen verwerfen): umgesetzt
Stimmt. `NewAddressPanel.isDirty()` prüft nur ausgewählte Felder auf "nicht leer", die Notiz z.B. nicht. Fix in `729e3a1`: für eine gespeicherte Adresse delegiert `isDirty()` an `AddressPanel.isDirty()`, das alle Felder mit dem gespeicherten DTO vergleicht. Das Beispiel aus dem Review (Nachname leeren, Notiz eintragen) meldet damit ungespeicherte Daten und führt zur Rückfrage.
Die Aussage in pr.md, die Rückfrage komme nach dem Speichern über den Etiketten-Reiter immer, ist entfernt. Sie kommt jetzt nur bei Änderungen seit dem Speichern.
Tests: die Rückfrage läuft jetzt über das package-private Feld `EditorsRegistry.reselectSaveQuestion` (Standard: der bisherige JOptionPane-Dialog). `EditorsRegistryReselectTest` hat 4 neue Tests für die Antworten Ja (speichert, setzt zurück), Ja mit fehlschlagendem Speichern (bleibt stehen), Nein (setzt ohne Speichern zurück) und Schließen (bleibt stehen), dazu prüft der bestehende Test für den unveränderten Editor, dass keine Rückfrage kommt. Die Delegation in `NewAddressPanel.isDirty()` selbst ist aus demselben Grund wie bei P1 nicht automatisch getestet.

### Build nach den Änderungen
`mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package` mit Ubuntu OpenJDK 17.0.20 plus OpenJFX 17.0.9 per jlink.

| Modul | ausgeführt | fehlgeschlagen | Fehler | übersprungen |
|---|---|---|---|---|
| j-lawyer-fax | 23 | 0 | 0 | 0 |
| j-lawyer-server-common | 104 | 17 | 0 | 0 |
| j-lawyer-cloud | 4 | 0 | 0 | 0 |
| j-lawyer-server-entities | 56 | 0 | 0 | 0 |
| j-lawyer-client | 89 | 0 | 0 | 0 |
| Summe | 276 | 17 | 0 | 0 |

Die 17 Fehlschläge sind FtpTest (6), SftpTest (6), SmbTest (5), wie auf master. Der Branch basiert weiter auf master 7b3b3dfa8, `git merge-tree` gegen den aktuellen master 4dc03980f ist konfliktfrei (geprüft am 03.11. für 8fde5cd und für 729e3a1).

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
