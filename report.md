# Report #aktenvorschlag-grossbuchstaben

- Branch: `fix/aktenvorschlag-grossbuchstaben` (fork derspotter/j-lawyer-org)
- Commit: `d0768c3509cae267019e949f0f66c68eabe7d26f`
- Subject: fix(documents): Aktenvorschlag beim Zuordnen erkennt auch Aktenzeichen mit Großbuchstaben
- Author: derspotter <jayjag@posteo.de>
- Base: upstream/master `7b3b3dfa8`

## Files (+/-)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/documents/SearchAndAssignDialog.java` | 3 | 2 |
| total | 3 | 2 |

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

## Evidence
- `CaseNumberGenerator` supports the pattern character `C`, filled from `"ABCDEFGHIJKLMNOPQRSTUVWXYZ"` (tests use e.g. "CCCCC/MM/YYYY"), so file numbers with capital letters are produced by the product itself.
- `SearchAndAssignDialog` is used by mail, beA, scans, bank transactions, calendar, Ingo chat history and more (14 callers).

## Unverified
- No GUI run, no unit test (logic inside the dialog's initialisation between server calls). The change only adds `toLowerCase()` to the file number side of two `contains()` checks, the other side is already lower case.

## Open questions
- DUPLICATE: another lane pushed origin/fix/aktenvorschlag-grossschreibung (2927c0a4c, committed 2026-11-01 14:57 UTC, before this branch) with the same two `toLowerCase()` changes in `SearchAndAssignDialog`, only the comment differs. Post only one of the two PRs, the other branch can be dropped.
- No issue of its own (found while triaging #3230).
- The second check (`fbc.getFileNumber().contains(searchContext)`) tests whether the file number contains the whole context, which only matches for short contexts. Kept as is apart from the case fix.
