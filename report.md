# Report 3507

- Branch: `fix/3507-pdf-teile-ohne-verweise-auf-original` (fork derspotter/j-lawyer-org), base upstream/master 7b3b3dfa8
- Commits (newest first):
  - 89a7dde52 derspotter <jayjag@posteo.de> | fix(documents): PDF aufteilen bestimmt behaltene Formularfelder aus den Widgets der behaltenen Seiten und durchläuft Feldbäume ohne Rekursion, auch bei Zyklen und sehr tiefen Formularen. Refs #3507
  - fb22fb03f derspotter <jayjag@posteo.de> | fix(documents): PDF aufteilen schreibt keine Formularfelder anderer Seiten mehr in den Teil, auch wenn sie über ein gemeinsames übergeordnetes Feld erreichbar sind. Refs #3507
  - 08a6997f4 derspotter <jayjag@posteo.de> | fix(documents): PDF aufteilen prüft alle gespeicherten Objekte des Teils, Verweise auf behaltene Seiten zeigen auf deren Kopie, Seitenaktionen und Artikel ziehen keine anderen Seiten mehr mit. Refs #3507
  - 4a23eb664 derspotter <jayjag@posteo.de> | fix(documents): PDF aufteilen entfernt auch Verweise über Formularfelder, Folgeaktionen, Antworten und Popups, doppelte Seitenzahlen ergeben keinen leeren Teil. Refs #3507
  - 17ccae1e0 derspotter <jayjag@posteo.de> | fix(documents): PDF aufteilen schreibt nur noch die Seiten des jeweiligen Teils in die Teil-PDFs. Closes #3507

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/ArchiveFilePanel.java` | 11 | 18 |
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/utils/pdf/PdfSplitUtils.java` | 994 | 0 |
| `j-lawyer-client/src/test/java/org/jlawyer/test/client/utils/PdfSplitUtilsTest.java` | 1189 | 0 |
| total | 2194 | 18 |

## Build and tests

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch at commit 89a7dde52 built (started 03.11.2026 19:14 UTC) and master 7b3b3dfa8 built (02.11.2026, in a separate worktree) with the identical command in the same container:

```
mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| j-lawyer-client | 95 / 95 / 0 / 0 | 81 / 81 / 0 / 0 |
| j-lawyer-cloud | 4 / 4 / 0 / 0 | 4 / 4 / 0 / 0 |
| j-lawyer-fax | 23 / 23 / 0 / 0 | 23 / 23 / 0 / 0 |
| j-lawyer-server-common | 104 / 104 / 0 / 17 | 104 / 104 / 0 / 17 |
| j-lawyer-server-entities | 56 / 56 / 0 / 0 | 56 / 56 / 0 / 0 |
| total | 282 / 282 / 0 / 17 | 268 / 268 / 0 / 17 |

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

## Antwort auf Review 03.11.
1. [P1] Retained source pages: agreed, reproduced (GoTo with /Next to the kept page 2, and an article bead on page 1, both 7 page objects with 4a23eb664) and fixed in 08a6997f4. `extractPages()` no longer skips page dictionaries. It maps every kept source page to its copy in the part (identity map source page to part page) and replaces references to it, so these actions and beads now point to the part's own pages. The tests `testChainedActionToKeptPageUsesTheCopy` and `testArticleThreadAcrossParts` assert both the page objects and the redirected target.
2. [P1] Page actions and other paths: agreed, fixed in 08a6997f4. Instead of walking only /Annots and /B, the cleaner now walks everything reachable from the part's trailer, which is exactly what `PDDocument.save()` writes (catalog, page tree, all entries of the copied page dictionaries including /AA, /B, /Annots, /Resources, and the document info). References to excluded pages, to their annotations and to all nodes of the source page tree are removed. As a safety net, any other dictionary of type /Page or /Pages that does not belong to the part's own page tree is removed too. Regressions: `testPageActionsToOtherPages` (open action of page 1 to page 5, close action of page 2 to page 1), `testSecondPartOfTheSameSource`, and `testAllRangesKeepExactlyTheirPages` (all 15 ranges of a 5-page document that has a link, a form field on pages 1 and 5, a chained action, an article thread from page 1 to page 5 and page actions, checking page count, page objects, page order and that no stream contains the text of another page). The traversal is iterative so long bead or action chains cannot overflow the stack. pr.md no longer says that references outside annotations are left alone.
3. PR title and the original commit summary: the title "schreibt nur noch die Seiten des jeweiligen Teils" is now backed by the trailer-wide check, and pr.md says what it covers and what is left (JavaScript actions that address pages by number, resources a kept page shares with other pages). The first commit 17ccae1e0 cannot be reworded without rewriting history, which the rules exclude. Its summary is superseded by the later commits and the PR text.
4. [P3] SMB test prerequisites: agreed. FtpTest and SftpTest read their credentials from environment variables, SmbTest uses the hard-coded share smb://j-lawyer-box/j-lawyer-share/. pr.md now says this.

## Evidence 03.11.
- Red check (plain javac + JUnit 4.12, PDFBox 2.0.24): with `PdfSplitUtils` from 4a23eb664 and the new test class, 5 of 11 tests fail (`testAllRangesKeepExactlyTheirPages` with `pages 1 - 1 expected:<1> but was:<6>`, `testPageActionsToOtherPages`, `testArticleThreadAcrossParts` and `testChainedActionToKeptPageUsesTheCopy` with `expected:<2> but was:<7>`, `testSecondPartOfTheSameSource` with `expected:<3> but was:<8>`). With 08a6997f4 all 11 pass.
- Probe with real PDFs (same harness): the 8 forms in `j-lawyer-server-ejb/src/main/resources/zvfv/2024-09-01` and `doc/forderungskonto-und-mahnverfahren.pdf`, split at every page boundary into a front and a back part, 76 parts in total. For each part: page count, text of every page (PDFTextStripper, compared with the source page) and number of page objects are correct. For each file the largest part has exactly the same size in bytes as with the 02.11. version, so for these documents the new traversal changes nothing.

## Antwort auf Copilot-Review 03.11.

Neue Commits: fb22fb03f.

Build (Stand fb22fb03f, gestartet 03.11.2026 18:34 UTC, Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9, Maven 3.9.11):

```
mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

Ergebnis: BUILD SUCCESS. 280 Tests gesamt, 280 ausgeführt, 0 übersprungen, 17 fehlgeschlagen. Die fehlgeschlagenen Tests sind dieselben wie auf master 7b3b3dfa8 (siehe Build and tests), die neuen Tests bestehen.

Der Commit fb22fb03f übernimmt den ungetesteten Entwurf 45f0935fb der lokalen Lane (wip/3507-acroform-local) mit neuer Commit-Nachricht. Geprüft wie eigene Arbeit: 12 von 12 Tests bestehen, mit 08a6997f4 schlägt der neue Test fehl (Wert Seite 5 im Teil). Die 9 echten PDFs (zvfv-Formulare und doc/forderungskonto-und-mahnverfahren.pdf) ergeben an jeder Seitengrenze weiterhin genau ihre Seiten mit gleichem Text. pr.md ist ergänzt.

Antworten je Thread:

- PRRT_kwDOCUQTLc6zRA4O: Behoben in fb22fb03f: Formularfelder ohne Widget auf einer Seite des Teils werden samt /V und /DV entfernt, auch wenn ein behaltenes Widget sie über /Parent und /Kids erreicht. Behalten wird ein Feld, wenn es selbst oder ein Nachfahre ein Widget auf einer behaltenen Seite ist. Neuer Test mit drei Nachbarfeldern mit unterschiedlichen Werten, er prüft Datenströme und Zeichenketten in allen Objekten und schlägt mit dem vorherigen Stand fehl.

## Antwort auf Codex 03.11. (Runde 2)

Neuer Commit: 89a7dde52.

Build (Stand 89a7dde52, gestartet 03.11.2026 19:14 UTC, Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9, Maven 3.9.11):

```
mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

Ergebnis: BUILD SUCCESS. 282 Tests gesamt, 282 ausgeführt, 0 übersprungen, 17 fehlgeschlagen. Die fehlgeschlagenen Tests sind dieselben wie auf master 7b3b3dfa8 (siehe Build and tests), die neuen Tests bestehen und schlagen mit dem vorherigen Stand fehl.

Punkte: Behaltene Felder werden jetzt aus den Widgets der behaltenen Seiten und deren /Parent-Kette bestimmt, nicht mehr aus einem rekursiven Durchlauf. Ein Zyklus in /Kids hält damit kein Feld mehr fest, und die Durchläufe sind iterativ. testCyclicFieldHierarchy schlägt mit fb22fb03f fehl (Wert von Seite 5 im Teil), testDeepFieldHierarchy mit StackOverflowError. pr.md nennt die richtigen Zahlen: Mit 4a23eb664 schlagen 7 der 14 Tests fehl, mit 08a6997f4 die beiden Tests mit Formularwerten anderer Seiten, und der Bereichstest enthält die Nachbarfelder nicht.

pr.md ist angepasst (siehe die Punkte oben).

Antworten je Thread:

- PRRT_kwDOCUQTLc6zRA4O: Behoben in fb22fb03f und 89a7dde52: Formularfelder ohne Widget auf einer Seite des Teils werden samt /V und /DV entfernt, auch wenn ein behaltenes Widget sie über /Parent und /Kids erreicht. Behalten werden nur die Widgets der behaltenen Seiten und ihre Felder entlang /Parent. Die Feldbäume werden ohne Rekursion durchlaufen, so werden auch fehlerhafte Formulare mit Zyklen und sehr tiefe Feldbäume verarbeitet. Tests mit Nachbarfeldern mit unterschiedlichen Werten, mit einem Zyklus und mit 20000 Ebenen prüfen Datenströme und Zeichenketten aller Objekte.
