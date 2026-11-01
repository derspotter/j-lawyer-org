# Report #3410

- Branch: `fix/3410-rechnertabelle-schriftgroesse-breite` (fork derspotter/j-lawyer-org)
- Commit: `f50ae1ca0438e5211dcc192d4c8570b3e729d575`
- Subject: fix(documents): Tabellen aus Rechnern beim Einfügen in voller Breite und mit eingestellter Schriftgröße. Closes #3410
- Author: derspotter <jayjag@posteo.de>
- Base: upstream/master `7b3b3dfa8`

## Files (+/-)

| File | + | - |
|---|---|---|
| `j-lawyer-server-common/src/main/java/org/jlawyer/plugins/calculation/StyledCalculationTable.java` | 11 | 30 |
| `j-lawyer-server-common/src/test/java/org/jlawyer/test/documents/StyledCalculationTableHtmlTest.java` | 728 | 0 |
| total | 739 | 30 |

## Build and tests vs. baseline

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink (`/opt/jdk17fx`). Maven 3.9.11.

```
mvn -B -ntp -pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

Reactor result: BUILD SUCCESS (all modules compiled and packaged)

Baseline = the same command on upstream/master 7b3b3dfa8 (`-pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb,j-lawyer-backupmgr -am`).

| Module | branch | master |
|---|---|---|
| j-lawyer-client | tests=81 failures=0 errors=0 skipped=0 | tests=81 failures=0 errors=0 skipped=0 |
| j-lawyer-cloud | tests=4 failures=0 errors=0 skipped=0 | tests=4 failures=0 errors=0 skipped=0 |
| j-lawyer-fax | tests=23 failures=0 errors=0 skipped=0 | tests=23 failures=0 errors=0 skipped=0 |
| j-lawyer-server-common | tests=108 failures=17 errors=0 skipped=0 | tests=104 failures=17 errors=0 skipped=0 |
| j-lawyer-server-entities | tests=56 failures=0 errors=0 skipped=0 | tests=56 failures=0 errors=0 skipped=0 |
| j-lawyer-server/j-lawyer-server-ejb | tests=829 failures=1 errors=0 skipped=16 | tests=829 failures=1 errors=0 skipped=16 |

```
TOTAL tests=1101 failures=18 errors=0 skipped=16
failing classes: ['j-lawyer-server-common FtpTest', 'j-lawyer-server-common SftpTest', 'j-lawyer-server-common SmbTest', 'j-lawyer-server/j-lawyer-server-ejb LibreOfficeODFTest']
new failures vs master: none
master failures not present: none
```

## Evidence
- LibreOffice 24.2 headless, `--infilter="HTML (StarWriter)" --convert-to odt` on a 2x2 table. Old markup (`<font size="3">`, as produced for setting 16): content.xml `fo:font-size="12pt"`, table width 85%. New markup (`style="font-size:16pt"`): `16pt`, 100%.
- `plugins.global.tableproperties.table.fontsize` is passed to `XWPFRun.setFontSize()` (points) in `MicrosoftOfficeAccess` for DOCX templates, so points is the intended unit.

## Unverified
- The calculation plugins live in the separate j-lawyer-calculations repo. Assumed (not verified here) that their "copy table" uses `StyledCalculationTable.toHtml()`, since `toString()` only returns "Tabelle NxM" and the callback puts `r.toString()` on the clipboard. In this repo, `ClaimLedgerDialog` uses `toHtml()` with the same setting.
- Paste into MS Word not tested (no Word here).

## Open questions
- Width 85% -> 100% is a visible change for everyone who pastes plugin tables. Requested in the issue ("Tabellenbreite zu schmal"), and the ODT/DOCX template path already uses full width.
- Closing-tag fix (`</b></i></u>`) included because it is in the same lines of the same method.
