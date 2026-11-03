# Report 3371

- Branch: `fix/3371-belegdialog-eingaben-behalten` (fork derspotter/j-lawyer-org), base upstream/master 7b3b3dfa8
- Commits (newest first):
  - 853000ac8 derspotter <jayjag@posteo.de> | test(finanzen): Test für Belege ohne Belegart prüft das erfolgreiche Speichern und das anschließende Zuweisen einer Belegart ohne pauschales Abfangen von Fehlern. Refs #3371
  - cd71f8590 derspotter <jayjag@posteo.de> | fix(finanzen): Bestätigen von Belegart/Nummernkreis bricht auch bei einem Fehler beim Speichern einer Position ab, Belege ohne Belegart lassen sich speichern. Refs #3371
  - 12f93c1fd derspotter <jayjag@posteo.de> | fix(finanzen): Belegdialog verwirft beim erneuten Bestätigen von Belegart/Nummernkreis keine Eingaben mehr. Closes #3371

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/InvoiceDialog.java` | 10 | 2 |
| `j-lawyer-server/j-lawyer-server-ejb/src/main/java/com/jdimension/jlawyer/services/ArchiveFileService.java` | 3 | 1 |
| `j-lawyer-server/j-lawyer-server-ejb/src/test/java/org/jlawyer/test/server/ejb/UpdateInvoiceWithoutTypeTest.java` | 779 | 0 |
| total | 792 | 3 |

## Build and tests (03.11.2026)

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch and master 7b3b3dfa8 built with the identical command in the same container (master in a separate worktree):

```
mvn -B -ntp -pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| j-lawyer-client | 81 / 81 / 0 / 0 | 81 / 81 / 0 / 0 |
| j-lawyer-cloud | 4 / 4 / 0 / 0 | 4 / 4 / 0 / 0 |
| j-lawyer-fax | 23 / 23 / 0 / 0 | 23 / 23 / 0 / 0 |
| j-lawyer-server-common | 104 / 104 / 0 / 17 | 104 / 104 / 0 / 17 |
| j-lawyer-server-entities | 56 / 56 / 0 / 0 | 56 / 56 / 0 / 0 |
| j-lawyer-server/j-lawyer-server-ejb | 830 / 814 / 16 / 1 | 829 / 813 / 16 / 1 |
| total | 1098 / 1082 / 16 / 18 | 1097 / 1081 / 16 / 18 |

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
- j-lawyer-server/j-lawyer-server-ejb LibreOfficeODFTest#setReplaceEmptyLine

## Antwort auf Review 02.11.
1. Position failures: agreed and fixed in cd71f8590. `save()` now returns false if any `updateInvoicePosition()` call failed, so the confirmation stops before `updateInvoiceType()` and before `setEntry()` reloads. A regression test for the dialog itself is not feasible without refactoring it (modal Swing dialog, services from `JLawyerServiceLocator`), answered here instead.
2. Untyped invoices: agreed, fixed in cd71f8590. The history text in `updateInvoice()` is null-safe ("ohne Belegart"). Covered by the new `UpdateInvoiceWithoutTypeTest`, red on master (no history entry, the NPE happens while building the text), green on this branch. Note: saving untyped invoices was already broken on master, independent of this PR. The build now includes j-lawyer-server-ejb because of this server change.
3. Baseline command: fixed. Branch and master were rebuilt on 02.11. with the identical command (`-pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb -am`), master in a separate worktree. Counts are given as total / executed / skipped / failed.

## Unverified
- No GUI run against a server.

## Antwort auf Review 03.11.
1. [P2] The regression test accepted a failed save: agreed, fixed in 853000ac8. The blanket `catch (Throwable)` is gone. The test uses an anonymous subclass of `ArchiveFileService` that overrides the public `addCaseHistory(String, ArchiveFileBean, String)` and records the description, so the JDBC update of the case change date (no data source in a unit test) is not reached. It asserts that `updateInvoice()` returns normally, that the returned invoice carries the edits (status, name) and the history text "Beleg geändert (R-2019-007, ..., ohne Belegart)". Then it calls `updateInvoiceType()` for the same invoice with a number range and a type (fakes for `invoicesPoolsFacade`, `invoicesTypesFacade` and `invoiceService.nextInvoiceNumber()`) and asserts type, pool id, the new number "RE-2026-001", the kept name and the second history entry "Belegart oder Nummernkreis geändert (RE-2026-001, Rechnung)". pr.md now describes exactly this.
2. [P3] SMB test prerequisites: agreed. FtpTest and SftpTest read their credentials from environment variables, SmbTest uses the hard-coded share smb://j-lawyer-box/j-lawyer-share/. pr.md now says this.

## Evidence 03.11.
- Test run with plain javac + JUnit against the compiled EJB module of this worktree (classpath taken from the surefire report of the previous build): 1 of 1 passes. With `ArchiveFileService` from master 7b3b3dfa8 compiled in front of it, the test fails with `NullPointerException: Cannot invoke "...InvoiceType.getDisplayName()" because the return value of "...Invoice.getInvoiceType()" is null` at `ArchiveFileService.updateInvoice`.
