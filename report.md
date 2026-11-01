# Report #2850

- Branch: `fix/2850-teilzahlung-als-bezahlt-angezeigt` (fork derspotter/j-lawyer-org)
- Commit: `d7fdb33f4302dcba9364ab39daa223ade7b86429`
- Subject: fix(finanzen): nach Buchung über den grünen Haken nur den tatsächlich gebuchten Betrag als bezahlt anzeigen. Refs #2850
- Author: derspotter <jayjag@posteo.de>
- Base: upstream/master `7b3b3dfa8`

## Files (+/-)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/InvoiceEntryPanel.java` | 7 | 2 |
| total | 7 | 2 |

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
- No GUI run. One extra server call (`getAccountEntriesForInvoice`) only after a confirmed booking.

## Open questions
- `Refs`, not `Closes`: point 1 of the issue is by design according to j-dimension (2025-03-11). The orchestrator may switch to `Closes` if point 2 is considered the whole bug.
- Independent of fix/2278 (different file). If both are merged the paid value is set twice with the same result. `git merge-tree` with fix/2278, fix/3371, fix/2169 is clean.
