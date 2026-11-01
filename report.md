# Report #platzhalter-sonderzeichen

- Branch: `fix/platzhalter-sonderzeichen` (fork derspotter/j-lawyer-org)
- Commit: `416b6a3fbf0b726260162ad9d4088aa74485587e`
- Subject: fix(documents): Platzhalterwerte mit $ oder \ in E-Mail- und beA-Vorlagen, Dateinamen und Prompts unverändert einsetzen
- Author: derspotter <jayjag@posteo.de>
- Base: upstream/master `7b3b3dfa8`

## Files (+/-)

| File | + | - |
|---|---|---|
| `j-lawyer-server-common/src/main/java/com/jdimension/jlawyer/documents/CommonTemplatesUtil.java` | 3 | 1 |
| `j-lawyer-server-common/src/test/java/org/jlawyer/test/documents/ReplacePlaceHoldersTest.java` | 701 | 0 |
| total | 704 | 1 |

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
| j-lawyer-server-common | tests=107 failures=17 errors=0 skipped=0 | tests=104 failures=17 errors=0 skipped=0 |
| j-lawyer-server-entities | tests=56 failures=0 errors=0 skipped=0 | tests=56 failures=0 errors=0 skipped=0 |
| j-lawyer-server/j-lawyer-server-ejb | tests=829 failures=1 errors=0 skipped=16 | tests=829 failures=1 errors=0 skipped=16 |

```
TOTAL tests=1100 failures=18 errors=0 skipped=16
failing classes: ['j-lawyer-server-common FtpTest', 'j-lawyer-server-common SftpTest', 'j-lawyer-server-common SmbTest', 'j-lawyer-server/j-lawyer-server-ejb LibreOfficeODFTest']
new failures vs master: none
master failures not present: none
```

## Evidence
- Red/green: `ReplacePlaceHoldersTest` run against master's `CommonTemplatesUtil` (`-pl j-lawyer-server-common`): `testDollarSign` -> IllegalArgumentException "Illegal group reference: group index is missing", `testBackslash` -> "Pfad: C:Akten2026". With the change all 3 pass.
- Callers (client): mail templates (`SendEmailFrame`), beA templates (`SendBeaMessageFrame`, only calls the shared method, file not modified), document names (`AddDocumentFromTemplateDialog`, `BulkSaveEntry`), Ingo prompts (`Assistant*`, `ToolRegistry`).

## Unverified
- No GUI run. The change is one call in the shared method, behaviour for values without `$`/`\` is identical (`quoteReplacement` returns the string unchanged).

## Open questions
- No issue of its own. `AdvancedHtmlExport.writeAsset()` uses the same `replaceAll` pattern, but only with color codes and numbers, so it is not affected and not changed.
- `LibreOfficeAccess.evaluateScript()` has the same pattern for script placeholders, left to #1875 (needs Groovy string escaping as well).
