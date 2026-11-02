# Report 2278

- Branch: `fix/2278-belege-nach-aktenkonto-aktualisieren` (fork derspotter/j-lawyer-org), base upstream/master 7b3b3dfa8
- Commits (newest first):
  - 0a0d10282 derspotter <jayjag@posteo.de> | fix(finanzen): Buchung im Aktenkonto als Kopie bearbeiten, damit ein fehlgeschlagenes Speichern die Belegbeträge nicht verfälscht. Refs #2278
  - 25b835b94 derspotter <jayjag@posteo.de> | fix(finanzen): Belegübersicht nach Buchungen im Aktenkonto aktualisieren. Closes #2278

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/ArchiveFilePanel.java` | 46 | 26 |
| total | 46 | 26 |

## Build and tests (02.11.2026)

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch and master 7b3b3dfa8 built with the identical command in the same container (master in a separate worktree):

```
mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| j-lawyer-client | 81 / 81 / 0 / 0 | 81 / 81 / 0 / 0 |
| j-lawyer-cloud | 4 / 4 / 0 / 0 | 4 / 4 / 0 / 0 |
| j-lawyer-fax | 23 / 23 / 0 / 0 | 23 / 23 / 0 / 0 |
| j-lawyer-server-common | 104 / 104 / 0 / 17 | 104 / 104 / 0 / 17 |
| j-lawyer-server-entities | 56 / 56 / 0 / 0 | 56 / 56 / 0 / 0 |
| total | 268 / 268 / 0 / 17 | 268 / 268 / 0 / 17 |

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
1. Unsaved edits leaking into invoice amounts: agreed, fixed in 0a0d10282. `cmdEditAccountEntryActionPerformed()` passes a copy (all twelve fields incl. id) to `CaseAccountEntryDialog`, the table keeps the stored entry until `updateAccountEntry()` succeeds and the row is replaced by the server's result. The same staleness affected the account totals on master already (`updateAccountTotals()` reads the table too), the copy fixes both. A regression test is not feasible without refactoring the panel (Swing table, dialog, server call), answered here.
2. Baseline command: fixed, branch and master rebuilt on 02.11. with the identical command `-pl j-lawyer-client -am`.

## Unverified
- No GUI run against a server.
