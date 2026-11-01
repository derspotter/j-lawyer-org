# Report #2278

- Branch: `fix/2278-belege-nach-aktenkonto-aktualisieren` (fork derspotter/j-lawyer-org)
- Commit: `25b835b94dc7ffbe326cbb1a31ed060228306544`
- Subject: fix(finanzen): Belegübersicht nach Buchungen im Aktenkonto aktualisieren. Closes #2278
- Author: derspotter <jayjag@posteo.de>
- Base: upstream/master `7b3b3dfa8`

## Files (+/-)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/ArchiveFilePanel.java` | 29 | 25 |
| total | 29 | 25 |

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
- No GUI run. All four mutating handlers (add, edit, duplicate, remove) already end with `updateAccountTotals()`, the new call sits right after it. `loadAccountEntries()` is the only other place that set paid totals, its loop is replaced by the same helper.

## Open questions
- Found while reading: `InvoiceEntryPanel.setPaidTotal()` compares `paid != BigDecimal.valueOf(Float.MIN_VALUE)` by reference, so the "keep current value" sentinel from `cmdOpenActionPerformed()` never matches and the row shows 0,00 paid after editing an invoice. Separate one-line fix, not included here (one topic per PR). Could be a follow-up.
- `git merge-tree` with fix/2850 (InvoiceEntryPanel), fix/3371 and fix/2169 is clean. With both fix/2278 and fix/2850 merged, the paid value after the green check mark is computed twice (table and server), both give the booked amount.
