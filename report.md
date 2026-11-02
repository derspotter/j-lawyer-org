# Report 3410

- Branch: `fix/3410-rechnertabelle-schriftgroesse-breite` (fork derspotter/j-lawyer-org), base upstream/master 7b3b3dfa8
- Commits (newest first):
  - f50ae1ca0 derspotter <jayjag@posteo.de> | fix(documents): Tabellen aus Rechnern beim Einfügen in voller Breite und mit eingestellter Schriftgröße. Closes #3410

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-server-common/src/main/java/org/jlawyer/plugins/calculation/StyledCalculationTable.java` | 11 | 30 |
| `j-lawyer-server-common/src/test/java/org/jlawyer/test/documents/StyledCalculationTableHtmlTest.java` | 728 | 0 |
| total | 739 | 30 |

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
| j-lawyer-server-common | 108 / 108 / 0 / 17 | 104 / 104 / 0 / 17 |
| j-lawyer-server-entities | 56 / 56 / 0 / 0 | 56 / 56 / 0 / 0 |
| j-lawyer-server/j-lawyer-server-ejb | 829 / 813 / 16 / 1 | 829 / 813 / 16 / 1 |
| total | 1101 / 1085 / 16 / 18 | 1097 / 1081 / 16 / 18 |

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
1. Test counts: fixed, now total / executed / skipped / failed for branch and master. No code change.
2. Baseline command: fixed. Branch and master were rebuilt on 02.11. with the identical command `-pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb -am`.

## Evidence
- LibreOffice 24.2 headless, `--infilter="HTML (StarWriter)" --convert-to odt` on a 2x2 table: old markup (`<font size="3">`, as produced for setting 16) gives `fo:font-size="12pt"` and 85% width, new markup gives `16pt` and 100%.
