# Report 2460

- Branch: `fix/2460-ablegen-als-datei-ersetzen` (fork derspotter/j-lawyer-org), base upstream/master 7b3b3dfa8
- Commits (newest first):
  - c47d1e66b derspotter <jayjag@posteo.de> | fix(documents): Ersetzen löscht nichts mehr endgültig. Eine Datei im Papierkorb mit dem Namen wird wie eine in der Akte in Ersetzt_ umbenannt und bleibt im Papierkorb, bis dessen Aufbewahrungsfrist sie entfernt. Bei einem Fehler bekommt sie ihren Namen zurück, wurde sie inzwischen wiederhergestellt, bleibt sie umbenannt in der Akte und die Meldung nennt das. Refs #2460
  - f84405b16 derspotter <jayjag@posteo.de> | Revert "fix(documents): Endgültiges Löschen aus dem Papierkorb liest das Dokument im Server erneut und gesperrt"
  - 066677331 derspotter <jayjag@posteo.de> | fix(documents): Endgültiges Löschen aus dem Papierkorb liest das Dokument im Server erneut und gesperrt und löscht es nur, wenn es noch im Papierkorb liegt. Ein gerade von jemand anderem wiederhergestelltes Dokument wird nicht gelöscht, das Ersetzen meldet dann einen Fehler und stellt den vorherigen Zustand her, die automatische Bereinigung des Papierkorbs überspringt es. Refs #2460
  - 07ec481fe derspotter <jayjag@posteo.de> | fix(documents): Name der ersetzten Datei höchstens 200 Zeichen, damit der Server die Umbenennung in der Historie der Akte speichern kann, und die Dokumentliste wird auch nach einem gescheiterten Verschieben in den Ordner abgeglichen. Refs #2460
  - d5ceca160 derspotter <jayjag@posteo.de> | fix(documents): nach dem Ersetzen wird die Dokumentliste der Akte neu geladen, Etiketten behalten ihr Datum, eine behobene Warnung zu Etiketten entfällt, der Papierkorb wird nur bei Bedarf geladen. Refs #2460
  - 757a6ab74 derspotter <jayjag@posteo.de> | fix(documents): Ersetzen bei „ablegen als“ stellt die Etiketten der bisherigen Datei nach jedem Umbenennen wieder her, eindeutige Zwischennamen auch bei mehreren Ersetzungen in derselben Millisekunde. Refs #2460
  - 7fd94c432 derspotter <jayjag@posteo.de> | fix(documents): Ersetzen bei „ablegen als“ löscht bei Fehlern nichts mehr, die bisherige Datei wird erst nach dem Ersetzen umbenannt in den Papierkorb gelegt, und der tatsächliche Stand aller Dateien wird angezeigt. Refs #2460
  - fba8fc68f derspotter <jayjag@posteo.de> | fix(documents): Ersetzen bei „ablegen als“ ermittelt die zu ersetzende Datei beim Server und prüft ihren Namen vor dem Löschen, Etiketten richten sich nach dem endgültigen Namen, kurzer Zwischenname für lange Dateinamen, Teilfehler werden angezeigt und rückgängig gemacht. Refs #2460
  - a3d57f3ca derspotter <jayjag@posteo.de> | fix(documents): Ersetzen bei „ablegen als“ löscht die vorhandene Datei erst nach erfolgreicher Ablage, beachtet keine Groß- und Kleinschreibung und bricht beim Schließen der Rückfrage ab. Refs #2460
  - 3fca54c11 derspotter <jayjag@posteo.de> | fix(documents): „ablegen als“ fragt bei vorhandener Datei nach Ersetzen oder neuem Namen, auch wenn sie im Papierkorb liegt. Refs #2460

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/ArchiveFilePanel.java` | 85 | 4 |
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/DocumentReplacement.java` | 1080 | 0 |
| `j-lawyer-client/src/test/java/com/jdimension/jlawyer/client/editors/files/DocumentReplacementTest.java` | 1303 | 0 |
| total | 2468 | 4 |

## Build and tests

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch at commit c47d1e66b built (started 03.11.2026 23:26 UTC) and master 7b3b3dfa8 built (02.11.2026, in a separate worktree) with the identical command in the same container:

```
mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| j-lawyer-client | 101 / 101 / 0 / 0 | 81 / 81 / 0 / 0 |
| j-lawyer-cloud | 4 / 4 / 0 / 0 | 4 / 4 / 0 / 0 |
| j-lawyer-fax | 23 / 23 / 0 / 0 | 23 / 23 / 0 / 0 |
| j-lawyer-server-common | 104 / 104 / 0 / 17 | 104 / 104 / 0 / 17 |
| j-lawyer-server-entities | 56 / 56 / 0 / 0 | 56 / 56 / 0 / 0 |
| total | 288 / 288 / 0 / 17 | 268 / 268 / 0 / 17 |

Failing tests: identical to master

- j-lawyer-server-common FtpTest#testCopy
- j-lawyer-server-common FtpTest#testCreateDeleteDirectory
- j-lawyer-server-common FtpTest#testDelete
- j-lawyer-server-common FtpTest#testIsDirectory
- j-lawyer-server-common FtpTest#testIsFile
- j-lawyer-server-common FtpTest#testTraverse
- j-lawyer-server-common SftpTest#testCopy
- j-lawyer-server-common SftpTest#testCreateDeleteDirectory
- j-lawyer-server-common SftpTest#testDelete
- j-lawyer-server-common SftpTest#testIsDirectory
- j-lawyer-server-common SftpTest#testIsFile
- j-lawyer-server-common SftpTest#testTraverse
- j-lawyer-server-common SmbTest#testCopy
- j-lawyer-server-common SmbTest#testCreateDeleteDirectory
- j-lawyer-server-common SmbTest#testDelete
- j-lawyer-server-common SmbTest#testIsFile
- j-lawyer-server-common SmbTest#testTraverse

## Antwort auf Review 02.11.
1. [P1] Original deleted before the upload: agreed, fixed in a3d57f3ca. `DocumentReplacement.addReplacing()` stores the new document under a temporary name, then removes the existing one, then renames (`renameDocument()`, which checks name conflicts). Test: a failing upload leaves `removeDocument` / `removeDocumentFromBin` uncalled. If only the final rename fails, the new document stays under its temporary name (stated in the PR).
2. [P2] Case-only differences: agreed, fixed. Lookups use `equalsIgnoreCase` like `doesDocumentExist()`, tested for the case and for the bin. The active documents come from the case view (`caseFolderPanel1.getDocuments()`), the bin from the server (`getDocumentsBin()`).
3. [P3] Closing the dialog: agreed, fixed. `CLOSED_OPTION` cancels, only "Nein" opens the rename dialog. Not covered by an automatic test (JOptionPane in the Swing handler).
- `convertDocumentToPdf()` has the same delete-first order and is deliberately left unchanged in this PR.

## Evidence
- `DocumentReplacementTest` uses a proxy implementation of `ArchiveFileServiceRemote` that records the calls, so the order (add under temporary name, remove, remove from bin, rename) and the absence of any delete after a failed upload are asserted.

## Antwort auf Review 03.11.
1. [P1] Cached lookup: agreed, reproduced and fixed in fba8fc68f. The caller no longer passes a document from the client's list. `DocumentReplacement.addReplacing(..., replace)` determines the target from the server after staging (`getDocuments(caseId)` and `getDocumentsBin()` for this case, ignoring case), fetches it again with `getDocument(id)` right before deleting and skips it if its name no longer matches. With the 02.11. code and the stateful fake server, the reviewer's scenario (A renamed to Behalten.pdf, B created as Brief.pdf, client list still showing A as Brief.pdf) deletes A for good and then fails renaming. Now B is replaced and A stays (`testStaleCacheRenamedAndRecreated`, `testRenamedRightBeforeDeleting`, `testNewlyAddedConflictWithoutReplacing`). A server-side atomic replace was not added, it would need a new EJB method, and the remaining window between the last check and the delete is stated under "Nicht abgedeckt".
2. [P2] Tags of the staging name: agreed, fixed. Right after staging, all tags of the staged document are removed (`getDocumentTags()` and `setDocumentTag(..., false)`). A new document has no other tags than the automatic ones. `renameDocument()` then applies the rules for the final name (`applyAutomatedDocumentTags()` is called in both `addDocumentImpl()` and `renameDocument()`). `testTagsFollowTheFinalName` covers a "name is not Brief.pdf" rule (no tag) and a "name contains brief" rule (tag present). With the 02.11. code the replacement keeps the tag of the negative rule.
3. [P2] Long names: agreed, fixed. The staging name is "Ersetzen_" + timestamp + the extension of the final name (at most 16 characters), so at most about 40 characters. `testLongestPossibleName` replaces a document with a 250-character name. With the 02.11. code the fake server refuses the 273-character staging name (250 characters plus the 23 of "Ersetzen_", timestamp and "_", the 03.11. report said 263 by mistake).
4. [P2] Partial failures: agreed, fixed. Failures before anything is deleted for good are undone (the staged document is removed, a target already moved to the bin is restored with `restoreDocumentFromBin()`), and the original error is shown. If something cannot be undone or the final rename fails, a `DocumentReplacement.ReplacementException` carries the stored document (with its temporary name) and the deleted documents. The handler removes the deleted documents from the list, adds the stored one under its temporary name and shows a message naming both. Tests for failures when moving to the bin, when deleting from the case, when deleting from the bin and when renaming after deleting from the case or from the bin.
5. [P3] SMB test prerequisites: agreed. FtpTest and SftpTest read their credentials from environment variables, SmbTest uses the hard-coded share smb://j-lawyer-box/j-lawyer-share/. pr.md now says this.

## Evidence 03.11.
- New tests: plain javac + JUnit 4.12 against the module classes of this worktree, 14 of 14 pass.
- Old behaviour: a probe with the same fake server and `DocumentReplacement` from a3d57f3ca prints `stale cache: document A still exists: false, B still exists: true` (after the error "Dokument Brief.pdf existiert bereits ..."), `tags of the replacement named Brief.pdf: [Sonstiges]` and `250 characters: error 'Data too long for column 'name''`.

## Antwort auf Review 03.11. (2)
1. [P1] Recovery could delete the replacement after the original's content was lost: agreed, reproduced and fixed by removing destructive recovery altogether (7fd94c432). With the version from the morning (fba8fc68f) and a fake server that, like `removeDocumentFromBin()`, deletes the file before the database entry: if storing the deletion fails, the original is restored without content and the new document is deleted, so both contents are gone. The new flow never deletes or bins the new document, in no path. A document in the case is no longer deleted for good: it is renamed to "Ersetzt_<timestamp>_<name>" (bounded to 250 characters, extension kept), the new document is renamed to the final name, and only then the old one is moved to the bin with `removeDocument()`, where it can be restored. `removeDocumentFromBin()` is only called for a target that already was in the bin, as confirmed by the user. If that fails after the file is gone, its empty entry stays in the bin and the new document keeps its temporary name (`testDeletingTheDocumentInTheBinFailsAfterItsFileIsGone`). An `@After` check in every test asserts that the new document was never passed to `removeDocument()` or `removeDocumentFromBin()` and still has its file.
2. [P2] Undo did not restore folder and invoice: agreed, fixed by not needing that undo. Before the replacement is complete the old document is only renamed, which keeps its folder and invoice link, and a failure renames it back. `restoreDocumentFromBin()` is not used any more (the fake server throws if it is called). `testRenamingTheNewDocumentFailsAndTheExistingOneGetsItsNameBack` sets a subfolder and an invoice link and asserts both, plus name, bin flag and file, after the failure. When the replacement is complete, moving the old document to the bin unlinks invoices and resets its folder as every move to the bin does, pr.md says so. The unconditional recovery claims in pr.md and report are removed.
3. [P2] Inaccurate message and list after failed undo: agreed, fixed. After any failure the state of every document involved (the target and the new document) is read from the server with `getDocument()`, and both the message and `Outcome.getActiveDocuments()` are built from that: "in der Akte als '<current name>'", "im Papierkorb der Akte als '<name>'" or "nicht mehr vorhanden oder nicht abrufbar". The handler removes all affected documents from the list and adds back exactly the active ones (`showReplacementOutcome()`), also on success, where a failed move to the bin is shown as a warning with the name the old document still has in the case. Every failure test asserts that the reported active documents equal the server's state of the affected documents, including a failed rename back (`testRenamingBothBackFails`), a failed tag removal and a failed move to the bin. There is no restoration step and no cleanup of the new document any more, so those two failure modes do not exist.
4. [P3] 263: agreed, it is 273 (250 + 23), corrected above.

## Evidence 03.11. (2)
- `DocumentReplacementTest`: plain javac + JUnit 4.12 against the module classes of this worktree, 15 of 15 pass.
- Probe with `DocumentReplacement` from fba8fc68f and the same fake server (plus `restoreDocumentFromBin()`, which only resets the deleted flag like the server): failure of the database entry after the file is deleted leaves `old name=Brief.pdf deleted=false file=false` and no new document at all, a failure before that leaves `old ... folder=root invoice=false` for a document that was in a subfolder and linked to an invoice.

## Antwort auf Review 03.11. (3)
1. [P2] Tags of the original changed for good: agreed, reproduced and fixed in 757a6ab74. Before the original is renamed, its tags (name and value) are read. After renaming it to "Ersetzt_..." and again after renaming it back, `restoreTags()` gives it exactly these tags: tags the rules added are removed, changed values are set back, and a tag that had no value but got one is removed and set again without value (the server keeps an existing value when a tag is set without value). The fake server now models tags with values like `applyAutomatedDocumentTags()` and `setDocumentTag()`. Three rules ("name is not Brief.pdf", "name starts with Ersetzt_ sets Status=ersetzt", "name contains brief") and an original with `Status=offen` and `Mandant` but without the rule tag `Brief` (as if a user had removed it): after a failed replacement (`testRenamingTheNewDocumentFailsAndTheExistingOneGetsItsNameBack`) and after a successful one in the bin (`testReplacedDocumentKeepsItsTags`) the original has exactly `{Status=offen, Mandant=null}`. With 7fd94c432 both tests fail with `{Status=ersetzt, Mandant=null, Sonstiges=null, Brief=null}`. A failure while restoring tags is reported in the message (`testRestoringTagsFails`). Not restored: the date a tag was set, and the history entries of the tag changes.
2. [P3] Recovery overstated: agreed. pr.md now says that giving the original its name and tags back is attempted and may fail (then it keeps "Ersetzt_..."), that the new document is kept, that a document already deleted from the bin stays deleted, and that the resulting state is read from the server and reported. The earlier commit messages cannot be changed without rewriting history, which the rules exclude. The message of 757a6ab74 only claims what the code does.
3. Found while testing: two replacements of the same name within one millisecond got the same "Ersetzt_" name, and the second one failed (safely, but needlessly). Both temporary names now use a timestamp that never repeats within the client.

## Evidence 03.11. (3)
- `DocumentReplacementTest`: plain javac + JUnit 4.12 against the module classes of this worktree, 17 of 17 pass (three runs). With `DocumentReplacement` from 7fd94c432 the three tag tests fail, the other 14 pass.

## Antwort auf Copilot-Review 03.11.

Neue Commits: d5ceca160.

Build (Stand d5ceca160, gestartet 03.11.2026 18:38 UTC, Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9, Maven 3.9.11):

```
mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

Ergebnis: BUILD SUCCESS. 287 Tests gesamt, 287 ausgeführt, 0 übersprungen, 17 fehlgeschlagen. Die fehlgeschlagenen Tests sind dieselben wie auf master 7b3b3dfa8 (siehe Build and tests), die neuen Tests bestehen.

Korrektur zu Antwort (3): Das Datum, an dem ein Etikett gesetzt wurde, wird jetzt ebenfalls wiederhergestellt. ArchiveFilePanel wurde zusätzlich mit dem Klassenpfad des Client-Builds einzeln kompiliert. pr.md ist angepasst.

Antworten je Thread:

- PRRT_kwDOCUQTLc6zRDYk: Behoben in d5ceca160: Nach dem Ersetzen und nach einem Fehler wird die Dokumentliste der Akte samt Etiketten vom Server neu geladen und erst danach angezeigt. Umbenennungen durch andere Clients erscheinen mit dem aktuellen Namen, das neue Dokument wird nicht zusätzlich eingefügt. Schlägt das Laden fehl, werden wie bisher nur die beteiligten Dokumente angepasst.
- PRRT_kwDOCUQTLc6zRDZF: Behoben in d5ceca160: Ein wieder angezeigtes Dokument behält seine im Client bekannte Verknüpfung mit einem Beleg, beim Neuladen der Liste und im Rückfall.
- PRRT_kwDOCUQTLc6zRDZh: Behoben in d5ceca160: Die Sicherung der Etiketten enthält das Datum. Ein Etikett mit geändertem Wert oder Datum wird entfernt und mit dem gesicherten Wert und Datum neu angelegt, denn der Server übernimmt ein mitgegebenes Datum nur beim Anlegen. Der Test prüft die Daten, mit dem vorherigen Stand schlägt er fehl.
- PRRT_kwDOCUQTLc6zRDaF: Behoben in d5ceca160: Warnungen zu Etiketten werden je Dokument geführt und entfallen, wenn ein späterer Versuch gelingt. Neuer Test mit einem einmaligen Fehler beim ersten Wiederherstellen.
- PRRT_kwDOCUQTLc6zRDas: Behoben in d5ceca160: Hat ein Dokument der Akte den Namen schon, wird der Papierkorb nicht geladen, denn Namen sind in Akte und Papierkorb zusammen eindeutig. Eine Abfrage nur für den Papierkorb einer Akte bietet das Remote-Interface nicht, eine neue Methode wird bewusst nicht ergänzt.

## Antwort auf Codex 03.11. (Runde 2)

Neuer Commit: 07ec481fe.

Build (Stand 07ec481fe, gestartet 03.11.2026 19:10 UTC, Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9, Maven 3.9.11):

```
mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

Ergebnis: BUILD SUCCESS. 287 Tests gesamt, 287 ausgeführt, 0 übersprungen, 17 fehlgeschlagen. Die fehlgeschlagenen Tests sind dieselben wie auf master 7b3b3dfa8 (siehe Build and tests), die neuen Tests bestehen und schlagen mit dem vorherigen Stand fehl.

Punkte: Ein Fehler beim Verschieben in den Ordner der Ausgangsdatei wird jetzt als Warnung gemeldet, danach wird die Liste wie bei Erfolg abgeglichen. Für den Menüpunkt gibt es keinen automatischen Test (Swing-Handler mit Serveraufrufen), ArchiveFilePanel wurde mit dem Klassenpfad des Client-Builds kompiliert.

Gegen einen echten Server ausprobiert (WildFly 26.1.3 mit docker/wildfly/standalone.xml, MariaDB 10.11.14 mit docker/mariadb/create_database.sql und den Flyway-Migrationen bis 3.6.0.9, EAR aus diesem Branch): Ein Programm ruft DocumentReplacement über die EJB-Schnittstelle des Clients auf, mit drei Etikettenregeln wie im Unit-Test. Geprüft: Ersetzen in der Akte (neuer Inhalt, nur die Etiketten für Brief.pdf, die bisherige Datei mit Inhalt, Etiketten und deren Datum als Ersetzt_..._Brief.pdf im Papierkorb, keine vorläufige Datei übrig), Ersetzen aus dem Papierkorb, abweichende Groß- und Kleinschreibung, eine von einem anderen Client umbenannte Datei, Ablegen ohne Ersetzen bei vorhandenem Namen, ein Name mit 250 Zeichen und eine vom Benutzer user gesperrte Datei (Meldung „Gesperrt.odt ist gesperrt für Nutzer user“, die bisherige Datei mit Namen und Etiketten unverändert, die neue unter dem vorläufigen Namen, beide in der Meldung genannt). Mit 07ec481fe bestehen alle 22 Prüfungen.

Dabei gefunden und in 07ec481fe behoben: Mit d5ceca160 scheiterte das Ersetzen eines Namens mit 250 Zeichen sicher, aber unnötig. Der Server schreibt beim Umbenennen „Dokument umbenannt: alt (neuer Name: neu)“ in case_history.changeDescription (500 Zeichen), der Name Ersetzt_... hatte bis zu 250 Zeichen. Er ist jetzt auf 200 Zeichen begrenzt. Der nachgebildete Server im Unit-Test prüft diese Länge jetzt auch, testLongestPossibleName schlägt mit d5ceca160 fehl. Unabhängig davon scheitert auf master jedes Umbenennen, bei dem alter und neuer Name zusammen mehr als 465 Zeichen haben. Das liegt am Server und ist nicht Teil dieses PRs.

pr.md ist angepasst (siehe die Punkte oben).

Antworten je Thread:

- PRRT_kwDOCUQTLc6zRDYk: Behoben in d5ceca160 und 07ec481fe: Nach dem Ersetzen und nach einem Fehler wird die Dokumentliste der Akte samt Etiketten vom Server neu geladen, auch wenn das anschließende Verschieben der neuen Datei in den Ordner der Ausgangsdatei scheitert (dann mit einer Warnung). Umbenennungen durch andere Clients erscheinen mit dem aktuellen Namen. Gelingt das Laden der Liste nicht, werden nur die beteiligten Dokumente angepasst, ein von einem anderen Client umbenanntes Dokument erscheint dann bis zum nächsten Laden der Akte noch unter seinem alten Namen.

## Antwort auf Copilot Runde 2 03.11.

Neuer Commit: 066677331 (auf 07ec481fe).

Thread PRRT_kwDOCUQTLc6zSLXZ (DocumentReplacement um Zeile 910): Stellt ein anderer Client das Zieldokument zwischen getDocument() und removeDocumentFromBin() wieder her, löschte der Server bisher das jetzt aktive Dokument endgültig, weil removeDocumentFromBinImpl() den Zustand nicht erneut prüfte.

Änderung im Server: removeDocumentFromBinImpl() liest das Dokument über die neue lokale Fassadenmethode ArchiveFileDocumentsBeanFacadeLocal.findForUpdate() erneut (refresh mit PESSIMISTIC_WRITE, also SELECT … FOR UPDATE, Sperre bis zum Ende der Transaktion) und löscht nur, wenn deleted gesetzt ist. Sonst gibt es false zurück, ohne Datei oder Eintrag anzufassen. removeDocumentFromBin() meldet dann einen Fehler ("Das Dokument liegt nicht im Papierkorb und wird nicht endgültig gelöscht. Es wurde vielleicht gerade wiederhergestellt."), purgeDocumentBin() überspringt das Dokument und schreibt einen Logeintrag. Keine neue Remote-Methode, die Javadoc von removeDocumentFromBin in ArchiveFileServiceRemote beschreibt den Fehler. Im Client ist nichts geändert: DocumentReplacement fängt den Fehler schon im vorhandenen catch-Zweig, gibt umbenannten Dateien Namen und Etiketten zurück und meldet den Stand auf dem Server.

Andere Aufrufer geprüft, alle löschen nur Dokumente im Papierkorb und funktionieren weiter: DocumentsBinDialog (Papierkorb-Dialog), ArchiveFilePanel an vier Stellen (erst removeDocument, dann removeDocumentFromBin in getrennten Aufrufen, oder ein Dokument aus getDocumentsBin), CasesEndpointV7 /document/{id}/permanent und DocumentsBinEndpointV8 /case/{id} (beide laut Doku für Dokumente im Papierkorb), purgeDocumentBin (siehe oben). AddressDocumentService.removeDocumentFromBin() für Kontaktdokumente ist unverändert, das Ersetzen nutzt es nicht.

Tests: Neu DocumentBinRemovalTest (j-lawyer-server-ejb, 2 Fälle) gegen ArchiveFileService mit nachgebildeten Fassaden und echter Datei im Basisverzeichnis: ein wiederhergestelltes Dokument wird weder mit Datei noch mit Eintrag gelöscht, eines im Papierkorb schon, und purgeDocumentBin() überspringt ein wiederhergestelltes. Mit 07ec481fe schlagen beide fehl (Datei und Eintrag des aktiven Dokuments werden gelöscht). In DocumentReplacementTest lehnt der nachgebildete Server das Löschen eines Dokuments außerhalb des Papierkorbs jetzt wie der echte ab (vorher eine Assertion), und der neue Fall testDocumentRestoredFromTheBinRightBeforeReplacing stellt das Dokument nach getDocument() wieder her: Das Ersetzen bricht mit ReplacementException ab, das Dokument bleibt mit Inhalt und Namen in der Akte, die Meldung nennt es. Dieser Fall prüft den unveränderten Client und schlägt deshalb mit 07ec481fe nicht fehl.

Build (Stand 066677331, gestartet 03.11.2026 22:07 UTC, Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9, Maven 3.9.11), jetzt mit dem Servermodul:

```
mvn -B -ntp -pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

Ergebnis: BUILD SUCCESS. 1119 Tests, 18 fehlgeschlagen, 16 übersprungen. Die 18 fehlgeschlagenen sind genau dieselben wie auf master 7b3b3dfa8 mit demselben Befehl (FtpTest, SftpTest und SmbTest ohne Server, LibreOfficeODFTest#setReplaceEmptyLine). DocumentBinRemovalTest 2 von 2 und DocumentReplacementTest 20 von 20 bestanden.

pr.md ist angepasst: Fix hat einen neuen Punkt zur Prüfung im Server, der Punkt zum Ersetzen einer Datei im Papierkorb nennt die Ablehnung. Verifiziert nennt 20 Fälle in DocumentReplacementTest mit dem neuen Fall und den neuen DocumentBinRemovalTest. Nicht abgedeckt nennt, dass die Prüfung nicht gegen einen laufenden WildFly und nicht mit zwei gleichzeitigen Clients ausprobiert ist, und dass AddressDocumentService unverändert ist. Der Build-Abschnitt gilt jetzt für j-lawyer-client und j-lawyer-server-ejb.

Antwort für den Thread:

- PRRT_kwDOCUQTLc6zSLXZ: Behoben in 066677331: removeDocumentFromBin() liest das Dokument im Server in seiner Transaktion erneut mit Schreibsperre und löscht es nur, wenn es noch im Papierkorb liegt. Hat ein anderer Client es zwischen getDocument() und dem Löschen wiederhergestellt, lehnt der Server mit einer Meldung ab und löscht nichts. DocumentReplacement behandelt das wie jeden anderen Fehler mit dem vorhandenen Zurücksetzen und der Meldung. Ein gleichzeitiges Wiederherstellen ist entweder vorher abgeschlossen oder wartet auf die Sperre und scheitert dann, weil das Dokument nicht mehr existiert. Die automatische Bereinigung des Papierkorbs überspringt solche Dokumente, die anderen Aufrufer löschen weiterhin nur Dokumente im Papierkorb. Keine neue Remote-Methode. Neuer Test DocumentBinRemovalTest (schlägt mit 07ec481fe fehl) und ein neuer Fall in DocumentReplacementTest.

## Antwort auf Copilot Runde 3 04.11.

Neue Commits: f84405b16 (Revert von 066677331) und c47d1e66b (neues Verhalten), auf 066677331, kein Force-Push.

Entscheidung umgesetzt: Das Ersetzen löscht nichts mehr endgültig. Damit entfallen die Befunde von Codex zu 066677331 (Sperr-Reihenfolge mit addCaseHistory, Umbenennung nach getDocument, Sperren während purgeDocumentBin, EntityNotFoundException bei refresh), weil der Server nicht mehr geändert wird.

1. Revert: f84405b16 macht 066677331 vollständig rückgängig. Der Baum ist danach identisch mit 07ec481fe: keine Änderung im Server, Javadoc von ArchiveFileServiceRemote wie vorher, kein findForUpdate, kein DocumentBinRemovalTest, purgeDocumentBin unverändert. Die Änderungen am Test-Fake aus 066677331 sind ebenfalls zurückgenommen und in c47d1e66b passend neu gemacht.
2. DocumentReplacement benennt eine Datei im Papierkorb mit dem Namen genauso um wie eine in der Akte (replacedName, Ersetzt_ mit Zeitstempel, höchstens 200 Zeichen wegen der Historie) und setzt danach ihre Etiketten zurück. Sie bleibt im Papierkorb, removeDocument wird nur für Dateien aus der Akte aufgerufen, removeDocumentFromBin gar nicht mehr. Der Zweig mit removeDocumentFromBin ist entfernt.
   Geprüft im Server (ArchiveFileService.renameDocument): Es gibt keine Prüfung auf den Papierkorb, umbenannt wird auch ein gelöschtes Dokument. Der neue Name wird gegen Akte und Papierkorb der Akte geprüft (findByArchiveFileKey, Meldung "existiert bereits in der Akte oder deren Papierkorb"), eine Sperre durch einen anderen Benutzer und die Gruppen der Akte werden wie bei aktiven Dokumenten geprüft. Nebenwirkungen auch im Papierkorb: Eintrag "Dokument umbenannt: alt (neuer Name: neu)" in der Historie der Akte (die Begrenzung auf 200 Zeichen hält ihn unter 500), applyAutomatedDocumentTags (die Etiketten setzt der Client danach auf den Stand davor zurück, live geprüft), Umbenennen einer vorhandenen Textvorschau, DocumentUpdatedEvent für externe Hooks und eine SearchIndexRequest ACTION_UPDATE. Letztere nimmt das Dokument über updateInIndex wieder in den Suchindex auf, anders als ACTION_UPDATE_METADATA prüft sie nicht auf gelöschte Dokumente, und purgeDocumentBin löscht keine Indexeinträge. Ein umbenanntes Dokument aus dem Papierkorb kann also in der Volltextsuche erscheinen und bleibt nach dem Ablauf der Aufbewahrungsfrist als Indexeintrag stehen. Live nicht nachprüfbar, die Suche liefert in der lokalen Installation auch für aktive Dokumente keine Treffer. Das steht in pr.md unter Nicht abgedeckt. Wenn das stören sollte, wäre ein erneutes removeDocument für die umbenannte Datei aus dem Papierkorb eine Möglichkeit (löscht den Indexeintrag, setzt aber die Aufbewahrungsfrist neu und widerspricht Punkt 4), deshalb nicht gemacht.
3. Rückgängig machen: unverändert der vorhandene Weg, die Datei aus dem Papierkorb bekommt bei einem späteren Fehler Namen und Etiketten zurück und bleibt im Papierkorb.
4. Inzwischen wiederhergestellt: Für eine Datei, die beim Lesen im Papierkorb lag, wird nach dem Ersetzen ihr Stand abgefragt. Liegt sie jetzt in der Akte (oder lässt sie sich nicht abfragen), liefert addReplacing über describe() eine Warnung "'Name' wurde ersetzt." mit "'Name': in der Akte als 'Ersetzt_…'", und sie steht in getActiveDocuments(), sodass die Dokumentliste sie zeigt. Scheitert das Umbenennen selbst (Name vergeben, Dokument weg, gesperrt), ist das wie bisher ein normaler Fehler mit Zurücksetzen.
5. Diff gegen master 7b3b3dfa8: jetzt +2468/-4 in 3 Dateien (vorher mit 066677331 +3305/-8 in 8 Dateien, mit 07ec481fe +2444/-4). DocumentReplacement +29/-22 gegenüber 07ec481fe, der Rest sind Tests. Tests in DocumentReplacementTest: testReplaceDocumentInBin prüft jetzt umbenannt und weiter im Papierkorb mit Inhalt, neu sind testDocumentInBinGetsItsNameBack (Zurücksetzen) und testDocumentRestoredFromTheBinWhileReplacing (wiederhergestellt), der Fake bricht den Test ab, wenn removeDocumentFromBin aufgerufen wird, der Test zum Fehler nach dem Löschen der Datei im Papierkorb entfällt. Mit 07ec481fe schlagen die drei Papierkorb-Tests fehl ("the replacement must not delete anything for good").
6. Live wiederholt mit WildFly 26.1.3 und MariaDB 10.11 (EAR mit dem unveränderten Servercode) und dem Client-Stand c47d1e66b: Szenario B (Ersetzen aus dem Papierkorb) prüft jetzt umbenannt, weiter im Papierkorb, Inhalt, Etiketten mit Datum und den Historieneintrag. Neu über einen Proxy um die echte Schnittstelle: H (ein anderer Client stellt die Datei direkt nach dem Lesen wieder her, sie bleibt umbenannt in der Akte, die Warnung nennt sie) und I (Fehler beim Umbenennen der neuen Datei, die Datei aus dem Papierkorb bekommt ihren Namen zurück und bleibt im Papierkorb). 34 von 34 Prüfungen bestehen.

Build (Stand c47d1e66b, gestartet 03.11.2026 23:26 UTC, Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9, Maven 3.9.11), wieder nur der Client wie vor 066677331:

```
mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

Ergebnis: BUILD SUCCESS. 288 Tests, 17 fehlgeschlagen, keine übersprungen. Die 17 fehlgeschlagenen sind genau dieselben wie auf master 7b3b3dfa8 (FtpTest, SftpTest und SmbTest ohne Server). DocumentReplacementTest 20 von 20.

pr.md ist angepasst: Fix sagt jetzt, dass das Ersetzen nichts endgültig löscht und die bisherige Datei aus dem Papierkorb unter einem Ersetzt_-Namen im Papierkorb bleibt, bis die Aufbewahrungsfrist sie entfernt, und was passiert, wenn sie inzwischen wiederhergestellt wurde. Der Punkt zur Prüfung im Server ist entfernt. Verifiziert nennt die geänderten und neuen Fälle und den wiederholten Live-Test mit 34 Prüfungen, der Punkt zu DocumentBinRemovalTest ist entfernt. Nicht abgedeckt nennt die Nebenwirkungen des Umbenennens im Papierkorb (Historie, Etikettenregeln, Hook-Ereignis, Suchindex), die Punkte zur Prüfung im Server und zum Scheitern des endgültigen Löschens sind entfernt. Der Build-Abschnitt gilt wieder für j-lawyer-client.

Antwort für den Thread:

- PRRT_kwDOCUQTLc6zSLXZ: Behoben in c47d1e66b, anders als zuerst gedacht: Das Ersetzen löscht nichts mehr endgültig, removeDocumentFromBin wird dabei nicht mehr aufgerufen. Liegt die Datei mit dem Namen im Papierkorb, wird sie wie eine Datei in der Akte in Ersetzt_ umbenannt (höchstens 200 Zeichen, Etiketten wie vorher) und bleibt im Papierkorb, bis die Aufbewahrungsfrist sie entfernt. Stellt ein anderer Client sie zwischen getDocument() und dem Umbenennen wieder her, bleibt sie umbenannt in der Akte, nichts geht verloren, und die Meldung sowie die neu geladene Dokumentliste zeigen sie dort. Scheitert ein späterer Schritt, bekommt sie Namen und Etiketten zurück. Die Serveränderung aus 066677331 ist mit f84405b16 zurückgenommen.
