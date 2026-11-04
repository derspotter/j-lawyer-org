# Report 3234

- Branch: `fix/3234-dateiname-bei-beteiligtenwechsel` (fork derspotter/j-lawyer-org), base upstream/master 05f6b4350
- Commits (newest first):
  - f1c580a40 derspotter <jayjag@posteo.de> | fix(documents): Dokument aus Vorlage behält einen eingegebenen Dateinamen, wenn danach Beteiligte oder der Briefkopf geändert werden. Refs #3234

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/AddDocumentFromTemplateDialog.java` | 26 | 3 |
| `j-lawyer-client/src/test/java/com/jdimension/jlawyer/client/editors/files/DocumentFileNameTest.java` | 695 | 0 |
| total | 721 | 3 |

## Build and tests

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch at commit f1c580a40 built (started 04.11.2026 07:55 UTC) and master 05f6b4350 built (04.11.2026, in a separate worktree) with the identical command in the same container:

```
mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| j-lawyer-client | 117 / 117 / 0 / 0 | 113 / 113 / 0 / 0 |
| j-lawyer-cloud | 4 / 4 / 0 / 0 | 4 / 4 / 0 / 0 |
| j-lawyer-fax | 23 / 23 / 0 / 0 | 23 / 23 / 0 / 0 |
| j-lawyer-server-common | 118 / 118 / 0 / 17 | 118 / 118 / 0 / 17 |
| j-lawyer-server-entities | 56 / 56 / 0 / 0 | 56 / 56 / 0 / 0 |
| total | 318 / 318 / 0 / 17 | 314 / 314 / 0 / 17 |

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

## Notes

- Base: upstream/master 05f6b4350 (one commit after 2f81c1064), comparison build of master 05f6b4350 with the same command.
- Cause as in the backlog: PartiesPanel's TableModelListener calls `listener.selectedPartiesUpdated()` on every change, `AddDocumentFromTemplateDialog.selectedPartiesUpdated()` calls `templateSelection()`, which always calls `txtFileName.setText(docName)`.
- Callers of `templateSelection()`: template list click and key (new name, unchanged), name template popup (new name, unchanged), letterhead combo (now keeps a typed name), `selectedPartiesUpdated()` (now keeps a typed name). `templateSelection()` itself sets the letterhead combo, which calls it again nested - at that point the name was just generated, so it is regenerated, no change in behaviour.
- The issue has a second symptom ("benennt man das Dokument um, verschwindet automatisch die Auswahl der zugewiesenen Beteiligung"). No code path from the file name field changes the parties (only an Enter key listener). `templateSelection()` calls `PartiesPanel.expandParties()`, which collapses the columns of party types the template does not use, so a role set in such a column is no longer visible. This needs a reproduction in the running client and is a design question (which columns to show), not changed here. That is why the commit and the PR say "Refs #3234", not "Closes".
- The same overwrite exists in `AddAddressDocumentFromTemplateDialog.selectedPartiesUpdated()` (contact documents), not changed to keep the diff to the reported dialog.
