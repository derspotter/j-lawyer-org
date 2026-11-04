# Report 3234

- Branch: `fix/3234-dateiname-bei-beteiligtenwechsel` (fork derspotter/j-lawyer-org), base upstream/master 05f6b4350
- Commits (newest first):
  - 575f6d278 derspotter <jayjag@posteo.de> | fix(documents): eingegebener Dateiname bleibt auch bei mehreren Änderungen der Beteiligten nacheinander erhalten. Refs #3234
  - f1c580a40 derspotter <jayjag@posteo.de> | fix(documents): Dokument aus Vorlage behält einen eingegebenen Dateinamen, wenn danach Beteiligte oder der Briefkopf geändert werden. Refs #3234

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/AddDocumentFromTemplateDialog.java` | 46 | 3 |
| `j-lawyer-client/src/test/java/com/jdimension/jlawyer/client/editors/files/DocumentFileNameTest.java` | 717 | 0 |
| total | 763 | 3 |

## Build and tests

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch at commit 575f6d278 built (started 04.11.2026 08:07 UTC) and master 05f6b4350 built (04.11.2026, in a separate worktree) with the identical command in the same container:

```
mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| j-lawyer-client | 118 / 118 / 0 / 0 | 113 / 113 / 0 / 0 |
| j-lawyer-cloud | 4 / 4 / 0 / 0 | 4 / 4 / 0 / 0 |
| j-lawyer-fax | 23 / 23 / 0 / 0 | 23 / 23 / 0 / 0 |
| j-lawyer-server-common | 118 / 118 / 0 / 17 | 118 / 118 / 0 / 17 |
| j-lawyer-server-entities | 56 / 56 / 0 / 0 | 56 / 56 / 0 / 0 |
| total | 319 / 319 / 0 / 17 | 314 / 314 / 0 / 17 |

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

## Antwort auf Review 04.11.
1. [P2] Several party changes overwrite an edited name: agreed, fixed in 575f6d278. In f1c580a40 the remembered generated name advanced on every evaluation, also when the typed name was kept. With Brief_Mueller generated and Brief_Schmidt typed, selecting Schmidt made the remembered name Brief_Schmidt, so selecting Meier no longer saw an edit and showed Brief_Meier.
   - The decision and the remembered name now live together in the small nested class `AddDocumentFromTemplateDialog.FileNameSuggestion` (`nameToShow(currentName, generatedName, keepEditedName)`). The remembered name only changes when the generated name is actually shown. While a typed name is kept, it stays the same, so later party or letterhead changes keep the typed name too. A new template or name template (`keepEditedName` false) shows the generated name and resets it. The dialog calls `nameToShow()` in `templateSelection()` instead of the static helper plus a separate field, so the state update is in the tested code.
   - The nested `templateSelection(true)` call that setting the letterhead combo triggers comes after the file name was set, so it sees either the kept typed name (kept again) or the name just generated (generated again, the same name). No change there.
   - Regression test `DocumentFileNameTest#testSeveralPartyChangesKeepTypedName` plays exactly the reported sequence (generated Brief_Mueller, typed Brief_Schmidt, select Schmidt, select Meier, expect Brief_Schmidt) and then checks that another template resets the name and later party changes regenerate it again. The four existing tests now use the same class and set up their state through it.
   - Red check: a copy of 575f6d278 where only `nameToShow()` was changed back to the f1c580a40 logic (remembered name always advances) fails exactly `testSeveralPartyChangesKeepTypedName` with `expected:<Brief_[Schmidt]> but was:<Brief_[Meier]>` (5 run, 1 failure), the other four pass. Command: the build command below with `-Dtest=DocumentFileNameTest -Dsurefire.failIfNoSpecifiedTests=false`.
   - Upstream master is now ee21b88d3. `git merge-tree` of 575f6d278 with it is clean, and master changed neither `AddDocumentFromTemplateDialog` nor `PartiesPanel` since 05f6b4350. Not rebased.
