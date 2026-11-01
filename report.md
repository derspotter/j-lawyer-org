# Report #2460

- Branch: `fix/2460-ablegen-als-datei-ersetzen` (fork derspotter/j-lawyer-org)
- Commit: `3fca54c119703a8f48dfbf11010fe72a283290ec`
- Subject: fix(documents): „ablegen als“ fragt bei vorhandener Datei nach Ersetzen oder neuem Namen, auch wenn sie im Papierkorb liegt. Refs #2460
- Author: derspotter <jayjag@posteo.de>
- Base: upstream/master `7b3b3dfa8`

## Files (+/-)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/ArchiveFilePanel.java` | 38 | 0 |
| total | 38 | 0 |

## Build and tests vs. baseline

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink (`/opt/jdk17fx`). Maven 3.9.11.

```
mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

Reactor result: BUILD SUCCESS (all modules compiled and packaged)

Baseline = the same command on upstream/master 7b3b3dfa8 (`-pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb,j-lawyer-backupmgr -am`).

| Module | branch | master |
|---|---|---|
| j-lawyer-client | tests=81 failures=0 errors=0 skipped=0 | tests=81 failures=0 errors=0 skipped=0 |
| j-lawyer-cloud | tests=4 failures=0 errors=0 skipped=0 | tests=4 failures=0 errors=0 skipped=0 |
| j-lawyer-fax | tests=23 failures=0 errors=0 skipped=0 | tests=23 failures=0 errors=0 skipped=0 |
| j-lawyer-server-common | tests=104 failures=17 errors=0 skipped=0 | tests=104 failures=17 errors=0 skipped=0 |
| j-lawyer-server-entities | tests=56 failures=0 errors=0 skipped=0 | tests=56 failures=0 errors=0 skipped=0 |

```
TOTAL tests=268 failures=17 errors=0 skipped=0
failing classes: ['j-lawyer-server-common FtpTest', 'j-lawyer-server-common SftpTest', 'j-lawyer-server-common SmbTest']
new failures vs master: none
master failures not present: none
```

## Unverified
- No GUI run. The added block is a copy of the existing check/replace logic in `convertDocumentToPdf()` (non-PDF branch): existence check before conversion, removal of the old document (case or bin) only after a successful conversion. Variable `replaceResponse` instead of `response`, since the enclosing method already has a local `response`.

## Open questions
- `Refs`, not `Closes`: part 1 of the issue (asking about open documents instead of waiting) is left out as a design point. #1205 has the same topic for "als PDF senden".
- The replace logic now exists three times in ArchiveFilePanel. Extracting a helper would be a refactoring of `convertDocumentToPdf()` too, deliberately not done to keep the diff to the one menu action.
