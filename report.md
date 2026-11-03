# Report 2460

- Branch: `fix/2460-ablegen-als-datei-ersetzen` (fork derspotter/j-lawyer-org), base upstream/master 7b3b3dfa8
- Commits (newest first):
  - fba8fc68f derspotter <jayjag@posteo.de> | fix(documents): Ersetzen bei „ablegen als“ ermittelt die zu ersetzende Datei beim Server und prüft ihren Namen vor dem Löschen, Etiketten richten sich nach dem endgültigen Namen, kurzer Zwischenname für lange Dateinamen, Teilfehler werden angezeigt und rückgängig gemacht. Refs #2460
  - a3d57f3ca derspotter <jayjag@posteo.de> | fix(documents): Ersetzen bei „ablegen als“ löscht die vorhandene Datei erst nach erfolgreicher Ablage, beachtet keine Groß- und Kleinschreibung und bricht beim Schließen der Rückfrage ab. Refs #2460
  - 3fca54c11 derspotter <jayjag@posteo.de> | fix(documents): „ablegen als“ fragt bei vorhandener Datei nach Ersetzen oder neuem Namen, auch wenn sie im Papierkorb liegt. Refs #2460

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/ArchiveFilePanel.java` | 40 | 1 |
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/DocumentReplacement.java` | 930 | 0 |
| `j-lawyer-client/src/test/java/com/jdimension/jlawyer/client/editors/files/DocumentReplacementTest.java` | 1040 | 0 |
| total | 2010 | 1 |

## Build and tests (03.11.2026)

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch and master 7b3b3dfa8 built with the identical command in the same container (master in a separate worktree):

```
mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| j-lawyer-client | 95 / 95 / 0 / 0 | 81 / 81 / 0 / 0 |
| j-lawyer-cloud | 4 / 4 / 0 / 0 | 4 / 4 / 0 / 0 |
| j-lawyer-fax | 23 / 23 / 0 / 0 | 23 / 23 / 0 / 0 |
| j-lawyer-server-common | 104 / 104 / 0 / 17 | 104 / 104 / 0 / 17 |
| j-lawyer-server-entities | 56 / 56 / 0 / 0 | 56 / 56 / 0 / 0 |
| total | 282 / 282 / 0 / 17 | 268 / 268 / 0 / 17 |

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
3. [P2] Long names: agreed, fixed. The staging name is "Ersetzen_" + timestamp + the extension of the final name (at most 16 characters), so at most about 40 characters. `testLongestPossibleName` replaces a document with a 250-character name. With the 02.11. code the fake server refuses the 263-character staging name.
4. [P2] Partial failures: agreed, fixed. Failures before anything is deleted for good are undone (the staged document is removed, a target already moved to the bin is restored with `restoreDocumentFromBin()`), and the original error is shown. If something cannot be undone or the final rename fails, a `DocumentReplacement.ReplacementException` carries the stored document (with its temporary name) and the deleted documents. The handler removes the deleted documents from the list, adds the stored one under its temporary name and shows a message naming both. Tests for failures when moving to the bin, when deleting from the case, when deleting from the bin and when renaming after deleting from the case or from the bin.
5. [P3] SMB test prerequisites: agreed. FtpTest and SftpTest read their credentials from environment variables, SmbTest uses the hard-coded share smb://j-lawyer-box/j-lawyer-share/. pr.md now says this.

## Evidence 03.11.
- New tests: plain javac + JUnit 4.12 against the module classes of this worktree, 14 of 14 pass.
- Old behaviour: a probe with the same fake server and `DocumentReplacement` from a3d57f3ca prints `stale cache: document A still exists: false, B still exists: true` (after the error "Dokument Brief.pdf existiert bereits ..."), `tags of the replacement named Brief.pdf: [Sonstiges]` and `250 characters: error 'Data too long for column 'name''`.
