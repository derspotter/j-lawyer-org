# Report girocode-betrag

- Branch: `fix/girocode-betrag` (fork derspotter/j-lawyer-org), base upstream/master 7b3b3dfa8
- Commits (newest first):
  - 0a50831a3 derspotter <jayjag@posteo.de> | docs(finanzen): JavaDoc von AmountUtils.parseExact nennt die Grenze von etwa 131.000 nur für float
  - a2ad1f0eb derspotter <jayjag@posteo.de> | fix(finanzen): Einlesen des Girocode-Betrags in eine testbare Hilfsmethode ausgelagert
  - 213b2e15a derspotter <jayjag@posteo.de> | fix(finanzen): Girocode enthält auch bei Beträgen ab rund 131.000 EUR den exakten Rechnungsbetrag

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/InvoiceDialog.java` | 3 | 1 |
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/utils/AmountUtils.java` | 694 | 0 |
| `j-lawyer-client/src/test/java/org/jlawyer/test/client/utils/AmountUtilsTest.java` | 714 | 0 |
| total | 1411 | 1 |

## Build and tests (03.11.2026)

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch and master 7b3b3dfa8 built with the identical command in the same container (master in a separate worktree):

```
mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| j-lawyer-client | 85 / 85 / 0 / 0 | 81 / 81 / 0 / 0 |
| j-lawyer-cloud | 4 / 4 / 0 / 0 | 4 / 4 / 0 / 0 |
| j-lawyer-fax | 23 / 23 / 0 / 0 | 23 / 23 / 0 / 0 |
| j-lawyer-server-common | 104 / 104 / 0 / 17 | 104 / 104 / 0 / 17 |
| j-lawyer-server-entities | 56 / 56 / 0 / 0 | 56 / 56 / 0 / 0 |
| total | 272 / 272 / 0 / 17 | 268 / 268 / 0 / 17 |

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
1. Missing regression test: agreed, fixed in a2ad1f0eb. The parsing moved to `AmountUtils.parseExact()` (used by the Girocode handler), `AmountUtilsTest` covers 150.000,01, 131.072,37, a round trip of 7.693 amounts around the 2^17 limit, invalid input and that the passed format is unchanged (still parses to `Double`).
2. Test description: fixed, identical command for branch and master (`-pl j-lawyer-client -am`), counts as total / executed / skipped / failed, missing GUI run stated in the PR.

## Evidence
- Probe (01.11., outside the repo): every amount from 100.000,00 to 300.000,00 in steps of 0,07, formatted with `#,##0.00` (German symbols), parsed back the old way and formatted like the server (`0.00`, Locale.US): 1.041.827 of 2.857.143 off by a cent, first 131.072,37 -> 131072.38. Parsed with a clone of the format and `setParseBigDecimal(true)` all exact.
- `AmountUtilsTest.testLargeAmountsAreExact` also asserts that the old float based parsing of 150.000,01 does not give 150000.01.

## Antwort auf Review 03.11.
1. [P3] JavaDoc of `AmountUtils.parseExact()`: agreed, fixed in 0a50831a3. The limit of roughly 131.000 is now attributed to float only. For double the JavaDoc says that it has enough digits for usual amounts but can not represent most cent values exactly, and that parsing into BigDecimal avoids the binary rounding.
2. [P3] SMB test prerequisites: agreed. FtpTest and SftpTest read their credentials from environment variables, SmbTest uses the hard-coded share smb://j-lawyer-box/j-lawyer-share/. pr.md now says this.
