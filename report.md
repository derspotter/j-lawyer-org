# Report 3507

- Branch: `fix/3507-pdf-teile-ohne-verweise-auf-original` (fork derspotter/j-lawyer-org), base upstream/master 7b3b3dfa8
- Commits (newest first):
  - 4a23eb664 derspotter <jayjag@posteo.de> | fix(documents): PDF aufteilen entfernt auch Verweise über Formularfelder, Folgeaktionen, Antworten und Popups, doppelte Seitenzahlen ergeben keinen leeren Teil. Refs #3507
  - 17ccae1e0 derspotter <jayjag@posteo.de> | fix(documents): PDF aufteilen schreibt nur noch die Seiten des jeweiligen Teils in die Teil-PDFs. Closes #3507

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/ArchiveFilePanel.java` | 11 | 18 |
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/utils/pdf/PdfSplitUtils.java` | 799 | 0 |
| `j-lawyer-client/src/test/java/org/jlawyer/test/client/utils/PdfSplitUtilsTest.java` | 831 | 0 |
| total | 1641 | 18 |

## Build and tests (02.11.2026)

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch and master 7b3b3dfa8 built with the identical command in the same container (master in a separate worktree):

```
mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| j-lawyer-client | 87 / 87 / 0 / 0 | 81 / 81 / 0 / 0 |
| j-lawyer-cloud | 4 / 4 / 0 / 0 | 4 / 4 / 0 / 0 |
| j-lawyer-fax | 23 / 23 / 0 / 0 | 23 / 23 / 0 / 0 |
| j-lawyer-server-common | 104 / 104 / 0 / 17 | 104 / 104 / 0 / 17 |
| j-lawyer-server-entities | 56 / 56 / 0 / 0 | 56 / 56 / 0 / 0 |
| total | 274 / 274 / 0 / 17 | 268 / 268 / 0 / 17 |

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
1. [P1] Remaining references: agreed, reproduced (form field with widgets on pages 1 and 5, and a GoTo with a /Next GoTo to page 5: 7 page objects each with the Splitter alone) and fixed in 4a23eb664. `extractPages()` collects the page dictionaries outside of the range and the annotation dictionaries on them, then walks the annotations (and /B beads) of the kept pages: dictionary entries pointing to one of them are removed, array elements are removed (annotations, e.g. /Kids) or set to null (pages, keeps destination arrays intact). Page dictionaries of the part are not descended into. Both scenarios are new tests and now give 2 page objects. The PR text no longer claims a blanket guarantee, it lists what is removed and what is not touched.
2. [P2] Duplicate split points: agreed, fixed in 4a23eb664. `PdfSplitUtils.toPartRanges()` deduplicates and sorts the split points before any document is created (tested with "2,2" and "4,1,4"), the handler uses it. Source and part are closed with try-with-resources. A test of the Swing handler itself is not feasible, the range logic it uses is tested.

## Evidence
- Red check (plain javac + JUnit, PDFBox 2.0.24): with the 01.11. version of `extractPages()` (Splitter only) `testFormFieldWithWidgetsOnOtherPages` and `testChainedActionToOtherPage` fail with `expected:<2> but was:<7>`. With this branch all 6 tests pass.
- 82-page LibreOffice PDF with linked TOC (235 KB): parts 1-3 / 4-82 / 11-11 / 1-82 give 3 / 79 / 1 / 82 page objects (36 / 117 / 25 / 130 KB). With the old importPage loop part 1-3 had 85 page objects (130 KB).
