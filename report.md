# Report #girocode-betrag

- Branch: `fix/girocode-betrag` (fork derspotter/j-lawyer-org)
- Commit: `213b2e15aef6a95092c8c915a415557086370f96`
- Subject: fix(finanzen): Girocode enthält auch bei Beträgen ab rund 131.000 EUR den exakten Rechnungsbetrag
- Author: derspotter <jayjag@posteo.de>
- Base: upstream/master `7b3b3dfa8`

## Files (+/-)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/InvoiceDialog.java` | 4 | 1 |
| total | 4 | 1 |

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
- Probe (outside the repo): every amount from 100.000,00 to 300.000,00 in steps of 0,07 formatted with `#,##0.00` (German symbols), parsed back the old way (`BigDecimal.valueOf(cf.parse(x).floatValue())`) and formatted like the server does (`new DecimalFormat("0.00", Locale.US)`): 1.041.827 of 2.857.143 differ by a cent, first one 131.072,37 -> 131072.38. Example 150.000,01 -> 150000.02. With a clone of the format and `setParseBigDecimal(true)` all 2.857.143 parse back exactly.
- `InvoiceService.getGiroCode()` formats the amount with `0.00` (HALF_EVEN), so below about 131.072 the float error is smaller than half a cent and invisible.

## Unverified
- No GUI run. The parse uses a clone of `cf` so the shared formatter of the dialog keeps returning `Double`/`Long` for other callers.

## Open questions
- No issue of its own (found while triaging #2747).
- Same float pattern for the timesheet budget limit (`TimesheetDialog.fillCurrentEntry`), not changed (stored at 2 decimals, limits above 131k EUR are rare).
