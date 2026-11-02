# Report platzhalter-sonderzeichen

- Branch: `fix/platzhalter-sonderzeichen` (fork derspotter/j-lawyer-org), base upstream/master 7b3b3dfa8
- Commits (newest first):
  - 416b6a3fb derspotter <jayjag@posteo.de> | fix(documents): Platzhalterwerte mit $ oder \ in E-Mail- und beA-Vorlagen, Dateinamen und Prompts unverändert einsetzen

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-server-common/src/main/java/com/jdimension/jlawyer/documents/CommonTemplatesUtil.java` | 3 | 1 |
| `j-lawyer-server-common/src/test/java/org/jlawyer/test/documents/ReplacePlaceHoldersTest.java` | 701 | 0 |
| total | 704 | 1 |

## Build and tests (02.11.2026)

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch and master 7b3b3dfa8 built with the identical command in the same container (master in a separate worktree):

```
mvn -B -ntp -pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| j-lawyer-client | 81 / 81 / 0 / 0 | 81 / 81 / 0 / 0 |
| j-lawyer-cloud | 4 / 4 / 0 / 0 | 4 / 4 / 0 / 0 |
| j-lawyer-fax | 23 / 23 / 0 / 0 | 23 / 23 / 0 / 0 |
| j-lawyer-server-common | 107 / 107 / 0 / 17 | 104 / 104 / 0 / 17 |
| j-lawyer-server-entities | 56 / 56 / 0 / 0 | 56 / 56 / 0 / 0 |
| j-lawyer-server/j-lawyer-server-ejb | 829 / 813 / 16 / 1 | 829 / 813 / 16 / 1 |
| total | 1100 / 1084 / 16 / 18 | 1097 / 1081 / 16 / 18 |

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
1. Test counts and GUI limitation: fixed, total / executed / skipped / failed, missing GUI run stated under "Nicht abgedeckt". No code change.
2. Baseline command: fixed. Branch and master were rebuilt on 02.11. with the identical command `-pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb -am`.

## Evidence
- Red/green (01.11.): `ReplacePlaceHoldersTest` against master's `CommonTemplatesUtil` fails in `testDollarSign` (IllegalArgumentException "Illegal group reference: group index is missing") and `testBackslash` ("Pfad: C:Akten2026"), the third case passes. With the change all 3 pass. Code unchanged since then, build repeated on 02.11.
