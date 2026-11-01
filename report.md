# Report #3507

- Branch: `fix/3507-pdf-teile-ohne-verweise-auf-original` (fork derspotter/j-lawyer-org)
- Commit: `17ccae1e02d6e63e624ce1eadc34d4198da18269`
- Subject: fix(documents): PDF aufteilen schreibt nur noch die Seiten des jeweiligen Teils in die Teil-PDFs. Closes #3507
- Author: derspotter <jayjag@posteo.de>
- Base: upstream/master `7b3b3dfa8`

## Files (+/-)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/ArchiveFilePanel.java` | 3 | 6 |
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/utils/pdf/PdfSplitUtils.java` | 702 | 0 |
| `j-lawyer-client/src/test/java/org/jlawyer/test/client/utils/PdfSplitUtilsTest.java` | 749 | 0 |
| total | 1454 | 6 |

## Build and tests vs. baseline

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink (`/opt/jdk17fx`). Maven 3.9.11.

```
mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

Reactor result: BUILD SUCCESS (all modules compiled and packaged)

Baseline = the same command on upstream/master 7b3b3dfa8 (`-pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb,j-lawyer-backupmgr -am`).

| Module | branch | master |
|---|---|---|
| j-lawyer-client | tests=84 failures=0 errors=0 skipped=0 | tests=81 failures=0 errors=0 skipped=0 |
| j-lawyer-cloud | tests=4 failures=0 errors=0 skipped=0 | tests=4 failures=0 errors=0 skipped=0 |
| j-lawyer-fax | tests=23 failures=0 errors=0 skipped=0 | tests=23 failures=0 errors=0 skipped=0 |
| j-lawyer-server-common | tests=104 failures=17 errors=0 skipped=0 | tests=104 failures=17 errors=0 skipped=0 |
| j-lawyer-server-entities | tests=56 failures=0 errors=0 skipped=0 | tests=56 failures=0 errors=0 skipped=0 |

```
TOTAL tests=271 failures=17 errors=0 skipped=0
failing classes: ['j-lawyer-server-common FtpTest', 'j-lawyer-server-common SftpTest', 'j-lawyer-server-common SmbTest']
new failures vs master: none
master failures not present: none
```

## Evidence
- Probe (outside the repo, PDFBox 2.0.24): 5-page PDF with one GoTo link page 1 -> page 5, source 3604 bytes. Old importPage loop, part pages 1-2: 4760 bytes, 7 page objects. Splitter: 1978 bytes, 2 page objects.
- 82-page PDF exported by LibreOffice 24.2 from HTML with a linked TOC (80 link annotations), 234767 bytes. Part 1-3: importPage 130251 bytes / 85 page objects, Splitter 36391 bytes / 3. Part 4-82: importPage 210524 / 161, Splitter 117297 / 79.
- javap on pdfbox-2.0.24 `Splitter.processAnnotations`: sets `PDPageDestination.setPage(null)` for link annotations (direct /Dest or GoTo action) and `annotation.setPage(null)` for all annotations. `Splitter` with start/end page and `splitAtPage = end - start` yields exactly one document (checked for 1-2 and 3-5).

## Unverified
- The reporter's BGB sample (GitHub user-attachments) returned 403 from this sandbox, not tested with it.
- No GUI run of the split dialog. The surrounding code (save, close order, naming, replace dialog) is unchanged.

## Open questions
- Links to pages inside the same part are dropped too (same as the scanner split, PDFBox TODO). Keeping them would need a custom import loop instead of `Splitter`. Chosen: library behaviour, consistent with `PDFSplitAction`.
- New files carry the full AGPL header like the other recent files, hence the large +lines count.
