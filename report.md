# Report 2460

- Branch: `fix/2460-ablegen-als-datei-ersetzen` (fork derspotter/j-lawyer-org), base upstream/master 4e721956a
- Commits (newest first):
  - 0361eb863 derspotter <jayjag@posteo.de> | fix(documents): Ablegen als, Als PDF zur Akte speichern und Duplizieren eines PDF klären den Namen mit derselben Hilfsmethode und ersetzen auf dieselbe Weise. Das zu ersetzende Dokument wird ohne Rücksicht auf Groß- und Kleinschreibung gefunden. DocumentReplacement entfällt, die beiden Server-Korrekturen bleiben. Refs #2460
  - 171c5bbb5 derspotter <jayjag@posteo.de> | Merge upstream master 4e721956a in fix/2460-ablegen-als-datei-ersetzen
  - 5a13f3300 derspotter <jayjag@posteo.de> | fix(documents): Umbenennen benennt die Datei eines noch unter seinem Namen gespeicherten Dokuments vorher wie beim Lesen in seine ID um, sonst ist der Inhalt danach nicht mehr zu finden. Der Suchindex nimmt bei einer Aktualisierung nur noch vorhandene Dokumente außerhalb des Papierkorbs auf, wie schon bei Metadaten. Refs #2460
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
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/ArchiveFilePanel.java` | 92 | 85 |
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/DocumentTargetName.java` | 764 | 0 |
| `j-lawyer-client/src/test/java/com/jdimension/jlawyer/client/editors/files/DocumentTargetNameTest.java` | 773 | 0 |
| `j-lawyer-server/j-lawyer-server-ejb/src/main/java/com/jdimension/jlawyer/services/ArchiveFileService.java` | 5 | 0 |
| `j-lawyer-server/j-lawyer-server-ejb/src/main/java/org/jlawyer/async/SearchIndexProcessor.java` | 4 | 1 |
| `j-lawyer-server/j-lawyer-server-ejb/src/test/java/com/jdimension/jlawyer/services/RenameDocumentTest.java` | 794 | 0 |
| total | 2432 | 86 |

## Build and tests

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch at commit 0361eb863 built (started 06.11.2026 11:35 UTC) and master 4e721956a built (06.11.2026, in a separate worktree) with the identical command in the same container:

```
mvn -B -ntp -pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| j-lawyer-client | 141 / 141 / 0 / 0 | 135 / 135 / 0 / 0 |
| j-lawyer-cloud | 4 / 4 / 0 / 0 | 4 / 4 / 0 / 0 |
| j-lawyer-fax | 26 / 26 / 0 / 0 | 26 / 26 / 0 / 0 |
| j-lawyer-server-common | 133 / 133 / 0 / 17 | 133 / 133 / 0 / 17 |
| j-lawyer-server-entities | 56 / 56 / 0 / 0 | 56 / 56 / 0 / 0 |
| j-lawyer-server/j-lawyer-server-ejb | 852 / 836 / 16 / 0 | 851 / 835 / 16 / 0 |
| total | 1212 / 1196 / 16 / 17 | 1205 / 1189 / 16 / 17 |

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

## Antwort auf Copilot Runde 4 04.11.

Neuer Commit: 5a13f3300 (auf c47d1e66b).

1. Suchindex: SearchIndexProcessor ruft bei ACTION_UPDATE updateInIndex nur noch auf, wenn das Dokument existiert und nicht im Papierkorb liegt (md != null && !md.isDeleted()), genau wie im Zweig für ACTION_UPDATE_METADATA. Das deckt das Umbenennen im Papierkorb, das Zurückbenennen und verspätete Anfragen ab. ACTION_ADD ist unverändert: es wird nur beim Hinzufügen und beim Wiederherstellen geschickt, also für aktive Dokumente, und ein späteres Löschen schickt danach ACTION_DELETE. Kein removeDocument nach dem Umbenennen. In pr.md steht unter Nicht abgedeckt nur noch, dass schon vorhandene Indexeinträge für Dokumente im Papierkorb nicht bereinigt werden.
2. Alte Dateien: ArchiveFileService.renameDocument ruft vor dem Ändern des Namens this.migrateDocument(dst, id, db.getName()) auf, dst ist das Verzeichnis archivefiles der Akte, gebildet wie in getDocumentContentImpl. Keine neue Remote-Methode. Das behebt die Lücke auch für aktive Dokumente, steht so in pr.md. Neuer Test RenameDocumentTest (j-lawyer-server-ejb): ArchiveFileService mit nachgebildeten Fassaden, einer nachgebildeten JNDI-Datenquelle für den Historieneintrag und einer echten Datei unter dem Namen des Dokuments. Nach renameDocument liefert getDocumentContent den Inhalt. Mit c47d1e66b schlägt er fehl ("Dokument Ersetzt_1_Brief.pdf existiert nicht!").
3. Unter Nicht abgedeckt ergänzt, ohne Code: ein gleichzeitiges Wiederherstellen durch einen anderen Client im selben Moment wie das Umbenennen (die Zeile wird ohne Versionsprüfung zurückgeschrieben, die Datei kann wieder im Papierkorb liegen und dort bis zum Ablauf der Frist wiederhergestellt werden, so verhalten sich alle schreibenden Methoden des Dienstes) und ein ausgeführtes Umbenennen mit verlorener Antwort (die Datei behält den Namen Ersetzt_ und die zurückgesetzten Etiketten, es geht nichts verloren).
4. Formulierung: pr.md sagt beim Fehlerfall jetzt, dass die neue Datei normalerweise ihren vorläufigen Namen behält, bei einem ausgeführten Umbenennen mit verlorener Antwort aber schon den endgültigen Namen trägt. Dass Name und Etiketten der bisherigen Datei zurückgegeben werden, stand schon als Versuch da ("wird versucht", Fehler werden gemeldet), die Antwortzeile unten sagt es jetzt ebenso.

Build (Stand 5a13f3300, gestartet 03.11.2026 23:59 UTC, Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9, Maven 3.9.11), mit Client und Servermodul. Ein erster Lauf um 23:43 UTC hatte dasselbe Ergebnis, danach hat das Bauen des EAR für den Live-Test die Testberichte der Servermodule gelöscht, deshalb wurde der Build für diese Notizen wiederholt:

```
mvn -B -ntp -pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

Ergebnis: BUILD SUCCESS. 1118 Tests, 18 fehlgeschlagen, 16 übersprungen. Die 18 fehlgeschlagenen sind genau dieselben wie auf master 7b3b3dfa8 mit demselben Befehl (FtpTest, SftpTest und SmbTest ohne Server, LibreOfficeODFTest#setReplaceEmptyLine). RenameDocumentTest 1 von 1, DocumentReplacementTest 20 von 20.

Live: WildFly 26.1.3 und MariaDB 10.11, EAR aus 5a13f3300 gebaut und neu eingespielt, Client aus 5a13f3300. Neues Szenario J: Eine Datei wird in den Papierkorb verschoben, im Datenverzeichnis von ihrer ID auf ihren Namen umbenannt (wie in alten Installationen) und dann ersetzt. Mit dem bisherigen EAR scheitert J ("Dokument Ersetzt_1793750198159_Altbestand.pdf existiert nicht!", 37 von 39 Prüfungen), mit dem neuen EAR ist der Inhalt über die ID lesbar und die Datei liegt unter der ID, 39 von 39 Prüfungen bestehen (A bis I wie zuvor). Die Suche im Index lässt sich in dieser Installation weiterhin nicht prüfen.

pr.md ist angepasst: Fix hat zwei neue Punkte zum Server (migrateDocument beim Umbenennen, auch für aktive Dokumente, und die Prüfung im Suchindex), der Punkt zum Fehlerfall nennt den Fall mit verlorener Antwort. Verifiziert nennt RenameDocumentTest und den Live-Test mit 39 Prüfungen und dem Fehlschlag ohne die Änderung. Nicht abgedeckt: Der Punkt zum Suchindex sagt nur noch, dass vorhandene Einträge nicht bereinigt werden, neu sind die beiden Punkte zum gleichzeitigen Wiederherstellen und zur verlorenen Antwort. Der Build-Abschnitt gilt für j-lawyer-client und j-lawyer-server-ejb.

Endgültige Antwort für den Thread:

- PRRT_kwDOCUQTLc6zSLXZ: Behoben in c47d1e66b und 5a13f3300, anders als zuerst gedacht: Das Ersetzen löscht nichts mehr endgültig, removeDocumentFromBin wird dabei nicht mehr aufgerufen. Liegt die Datei mit dem Namen im Papierkorb, wird sie wie eine Datei in der Akte in Ersetzt_ umbenannt (höchstens 200 Zeichen) und bleibt im Papierkorb, bis die Aufbewahrungsfrist sie entfernt. Stellt ein anderer Client sie zwischen getDocument() und dem Umbenennen wieder her, bleibt sie umbenannt in der Akte, nichts geht verloren, und die Meldung sowie die neu geladene Dokumentliste zeigen sie dort. Scheitert ein späterer Schritt, wird versucht, ihr Namen und Etiketten zurückzugeben, ein Fehler dabei wird gemeldet. Im Server nimmt der Suchindex umbenannte Dateien im Papierkorb nicht mehr auf, und renameDocument verschiebt den Inhalt einer noch unter ihrem Namen gespeicherten Datei vorher auf ihre ID, damit er auffindbar bleibt. Die Serveränderung aus 066677331 ist mit f84405b16 zurückgenommen.

## Antwort auf Jens 05.11.
Request: replace the client side by the existing logic of "Als PDF zur Akte speichern", one shared helper for the three ways, fix upper and lower case in the lookup, keep `getDocumentsBin()` while `getDocuments(caseId, true)` is not on the remote interface, remove `DocumentReplacement`, `showDocumentsAfterReplacement()` and `DocumentReplacementTest`, keep both server fixes. Done in 0361eb863, after merging upstream master 4e721956a into the branch (171c5bbb5, a merge without conflicts, no rebase, no force-push).

- New small class `DocumentTargetName` (client, `editors.files`): `resolve(name, NameCheck, Questions)` is the former loop of `convertDocumentToPdf()`: as long as `doesDocumentExist()` reports the name, ask whether to replace, on "no" ask for a new name (`FileUtils.getNewFileName()`), check again. It returns the name and whether to replace, null if the question was closed or no new name was entered. `findDocument(documents, name)` looks up the document to replace with `equalsIgnoreCase`, like the server's check.
- `ArchiveFilePanel.resolveDocumentName(remote, name, title)` plugs in the existing dialogs ("Eine Datei mit dem Namen '…' existiert bereits, soll diese ersetzt werden?", Ja/Nein, closing = cancel). `ArchiveFilePanel.removeDocumentToReplace(remote, name)` is the former removal block: the document among the active documents of the case (`removeDocument()`, `removeDocumentFromBin()`, removed from the list), otherwise among the documents of this case in `getDocumentsBin()` (`removeDocumentFromBin()`), both via `findDocument()`.
- Callers: `convertDocumentToPdf()` for an existing PDF (resolve, remove, then `addDocument()` with the metadata, same order as before) and for the conversion (resolve, convert, remove right before `addDocument()`, as before), and `mnuDuplicateDocumentAsActionPerformed()` (resolve, convert, remove right before `addDocument()`). The two copies in `convertDocumentToPdf()` are gone.
- Behaviour changes, now the same for all three ways: closing the replace question cancels (in `convertDocumentToPdf()` it used to open the dialog for a new name), and `Brief.PDF` is found and replaced for `Brief.pdf` (it used to stay, and `addDocument()` failed with the server error).
- Kept from the earlier menu code: closing the question cancels, a failing move into the folder of the source document only warns (the document is stored and shown). The folder move in `convertDocumentToPdf()` is unchanged.
- Removed: `DocumentReplacement` (temporary name, renaming the old document, tag reset, roll back, status queries), `DocumentReplacementTest`, `showDocumentsAfterReplacement()`. The replaced document is deleted for good again, as before in "Als PDF zur Akte speichern" and as Jens accepts.
- `getDocuments(caseId, true)` is not on `ArchiveFileServiceRemote`, so `getDocumentsBin()` stays, filtered by the case as before. The remote interface is not changed.
- Server fixes unchanged: `git diff` of `j-lawyer-server` between master 4e721956a and the branch has exactly the lines that 5a13f3300 had against its base 7b3b3dfa8 (`renameDocument()` calls `migrateDocument()`, `SearchIndexProcessor` checks `ACTION_UPDATE`), `RenameDocumentTest` is unchanged and passes.
- No `.form` file is affected.
- Net diff against master: `ArchiveFilePanel` +92/-85, `DocumentTargetName` 102 lines and `DocumentTargetNameTest` 111 lines (each plus the 662-line license header of the project), the server part as before.
- Test: `DocumentTargetNameTest` with 6 cases without a server (free name, replace, new names until one is free, replace under the new name, cancel by closing the question or without a new name, case-insensitive lookup).
- Red check: a copy of 0361eb863 with `equals` instead of `equalsIgnoreCase` in `findDocument()` and a closed question counted as "no" (the former behaviour of `convertDocumentToPdf()`). `-Dtest=DocumentTargetNameTest`: exactly `testFindDocumentIgnoresCase` (no document found for `Brief.pdf`) and `testCancel` (`NoSuchElementException`, the closed question led to the dialog for a new name) fail, the other 4 pass.
- Not run in a client: the three menu entries. The real-server run of 04.11. covered the server fix in `renameDocument()`, the removed `DocumentReplacement` client code is not part of the PR any more.
