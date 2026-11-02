# Report 2203

- Branch: `fix/2203-belegnummer-konflikt-bei-nummernkreiswechsel` (fork derspotter/j-lawyer-org), base upstream/master 7b3b3dfa8
- Commits (newest first):
  - 3655a2b9e derspotter <jayjag@posteo.de> | test(finanzen): Konflikt beim Nummernkreiswechsel lässt auch eine angefragte neue Belegart unverändert. Refs #2203
  - 4bcf4af2e derspotter <jayjag@posteo.de> | fix(finanzen): klare Fehlermeldung bei bereits vergebener Belegnummer beim Wechsel des Nummernkreises. Refs #2203

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-server/j-lawyer-server-ejb/src/main/java/com/jdimension/jlawyer/services/ArchiveFileService.java` | 15 | 8 |
| `j-lawyer-server/j-lawyer-server-ejb/src/test/java/org/jlawyer/test/server/ejb/InvoiceTypeChangeConflictTest.java` | 822 | 0 |
| total | 837 | 8 |

## Build and tests (02.11.2026)

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch and master 7b3b3dfa8 built with the identical command in the same container (master in a separate worktree):

```
mvn -B -ntp -pl j-lawyer-server/j-lawyer-server-ejb -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| j-lawyer-cloud | 4 / 4 / 0 / 0 | 4 / 4 / 0 / 0 |
| j-lawyer-fax | 23 / 23 / 0 / 0 | 23 / 23 / 0 / 0 |
| j-lawyer-server-common | 104 / 104 / 0 / 17 | 104 / 104 / 0 / 17 |
| j-lawyer-server-entities | 56 / 56 / 0 / 0 | 56 / 56 / 0 / 0 |
| j-lawyer-server/j-lawyer-server-ejb | 831 / 815 / 16 / 1 | 829 / 813 / 16 / 1 |
| total | 1018 / 1002 / 16 / 18 | 1016 / 1000 / 16 / 18 |

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
1. Missing coverage for a type change: agreed, added in 3655a2b9e. `typeAndPoolChangeToTakenNumberKeepsTheType` requests a different type with a stubbed `invoicesTypesFacade` and asserts that type, number and pool of the managed invoice stay unchanged. A mutation check (type block moved back before the conflict check) is described below.
2. Test counts: fixed, now total / executed / skipped / failed, branch and master rebuilt on 02.11. with the identical command `-pl j-lawyer-server/j-lawyer-server-ejb -am`.

## Unverified
- Transaction behaviour reasoned from EJB rules (checked exception = application exception, no rollback), not observed on WildFly.

## Mutation check (02.11.)
- Temporary worktree of this branch with the type block moved back before the pool block (as on master, conflict check kept), `mvn -pl j-lawyer-server/j-lawyer-server-ejb -am -Dtest=InvoiceTypeChangeConflictTest package`: `typeAndPoolChangeToTakenNumberKeepsTheType` fails with `expected:<type1> but was:<type2>`, the first test still passes. So the new test catches the order the review was concerned about. The worktree was removed afterwards, nothing committed.
