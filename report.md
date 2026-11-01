# Report #belege-bezahlt-nach-bearbeiten

- Branch: `fix/belege-bezahlt-nach-bearbeiten` (fork derspotter/j-lawyer-org)
- Commit: `549d76e8119ce17bff6b7e2921b71f72f5d957a3`
- Subject: fix(finanzen): Belegübersicht zeigt nach dem Bearbeiten eines Belegs wieder den bereits bezahlten Betrag. Refs #2278
- Author: derspotter <jayjag@posteo.de>
- Base: upstream/master `7b3b3dfa8`

## Files (+/-)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/InvoiceEntryPanel.java` | 2 | 1 |
| `j-lawyer-client/src/test/java/com/jdimension/jlawyer/client/editors/files/InvoiceEntryPanelPaidTotalTest.java` | 707 | 0 |
| total | 709 | 1 |

## Build and tests vs. baseline

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink (`/opt/jdk17fx`). Maven 3.9.11.

```
mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

Reactor result: BUILD SUCCESS (all modules compiled and packaged)

Baseline = the same command on upstream/master 7b3b3dfa8 (`-pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb,j-lawyer-backupmgr -am`).

| Module | branch | master |
|---|---|---|
| j-lawyer-client | tests=83 failures=0 errors=0 skipped=0 | tests=81 failures=0 errors=0 skipped=0 |
| j-lawyer-cloud | tests=4 failures=0 errors=0 skipped=0 | tests=4 failures=0 errors=0 skipped=0 |
| j-lawyer-fax | tests=23 failures=0 errors=0 skipped=0 | tests=23 failures=0 errors=0 skipped=0 |
| j-lawyer-server-common | tests=104 failures=17 errors=0 skipped=0 | tests=104 failures=17 errors=0 skipped=0 |
| j-lawyer-server-entities | tests=56 failures=0 errors=0 skipped=0 | tests=56 failures=0 errors=0 skipped=0 |

```
TOTAL tests=270 failures=17 errors=0 skipped=0
failing classes: ['j-lawyer-server-common FtpTest', 'j-lawyer-server-common SftpTest', 'j-lawyer-server-common SmbTest']
new failures vs master: none
master failures not present: none
```

## Evidence
- Red/green: the same branch built once without the one-line change (test only): `testKeepCurrentPaidValue` fails with `expected:<40.00 EUR> but was:<0.00 EUR>`. With the change: 2/2 pass. The test creates `InvoiceEntryPanel` headless and reads `lblPaidTotal` / `lblOpen` via reflection (private fields).
- Regression source: d1498e4b0 (2024-11-22, #2681, floats to BigDecimal) turned `paid != Float.MIN_VALUE` (primitive) into `paid != BigDecimal.valueOf(Float.MIN_VALUE)` (reference).

## Unverified
- No GUI run. The only callers passing the placeholder are the two lines in `InvoiceEntryPanel.cmdOpenActionPerformed()`, all other callers pass real amounts and are unaffected.

## Open questions
- No issue of its own (found while fixing #2278, same overview), hence `Refs #2278`. The orchestrator may want a separate issue first.
- Alternative would be to drop the placeholder and pass the current value from `cmdOpenActionPerformed()`. Kept the existing contract to stay at one line.
- `git merge-tree` against all other fix/* branches (incl. fix/2278 and fix/2850, which touch the same overview) is clean.
