# Report belegsumme-ohne-ust

- Branch: `fix/belegsumme-ohne-ust` (fork derspotter/j-lawyer-org), base upstream/master 7b3b3dfa8
- Commits (newest first):
  - fb6e7dfeb derspotter <jayjag@posteo.de> | fix(finanzen): grüner Haken rechnet mit den vom Server neu berechneten Belegsummen, Test für den Nummernkreiswechsel
  - 019662b63 derspotter <jayjag@posteo.de> | fix(finanzen): Bruttosumme eines Belegs ohne ausgewiesene USt entspricht der Nettosumme

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/InvoiceEntryPanel.java` | 2 | 1 |
| `j-lawyer-server/j-lawyer-server-ejb/src/main/java/com/jdimension/jlawyer/services/ArchiveFileService.java` | 15 | 7 |
| `j-lawyer-server/j-lawyer-server-ejb/src/test/java/org/jlawyer/test/server/ejb/InvoiceTotalTest.java` | 784 | 0 |
| total | 801 | 8 |

## Build and tests (02.11.2026)

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch and master 7b3b3dfa8 built with the identical command in the same container (master in a separate worktree):

```
mvn -B -ntp -pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| j-lawyer-client | 81 / 81 / 0 / 0 | 81 / 81 / 0 / 0 |
| j-lawyer-cloud | 4 / 4 / 0 / 0 | 4 / 4 / 0 / 0 |
| j-lawyer-fax | 23 / 23 / 0 / 0 | 23 / 23 / 0 / 0 |
| j-lawyer-server-common | 104 / 104 / 0 / 17 | 104 / 104 / 0 / 17 |
| j-lawyer-server-entities | 56 / 56 / 0 / 0 | 56 / 56 / 0 / 0 |
| j-lawyer-server/j-lawyer-server-ejb | 832 / 816 / 16 / 1 | 829 / 813 / 16 / 1 |
| total | 1100 / 1084 / 16 / 18 | 1097 / 1081 / 16 / 18 |

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
- j-lawyer-server/j-lawyer-server-ejb LibreOfficeODFTest#setReplaceEmptyLine

## Antwort auf Review 02.11.
1. Stale gross in the green check mark: agreed, fixed in fb6e7dfeb. `cmdMarkAsPayedActionPerformed()` now keeps the invoice returned by `updateInvoice()` (`this.invoice = ...updateInvoice(...)`), so the proposed amount uses the recalculated gross. No automatic test (Swing panel with server calls), answered here.
2. Pool change not covered: agreed, added `testPoolChangeRecalculatesGross` in fb6e7dfeb. It calls `updateInvoiceType()` with a pool without VAT for an invoice stored with VAT and asserts net 100.00 and gross 100.00 afterwards. The call ends with an exception from `addCaseHistory()` (JDBC, no data source in a unit test), the recalculation happens before. Without the recalculation in `updateInvoiceType()` the gross stays 119.00 (see mutation check below).
3. Test counts: fixed, total / executed / skipped / failed.
4. Baseline command: fixed. The branch now changes the client as well, so branch and master were built with `-pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb -am`.

## Unverified
- No run against a database, no GUI run.

## Mutation check (02.11.)
- Temporary worktree of this branch without the recalculation in `updateInvoiceType()`, `mvn -pl j-lawyer-server/j-lawyer-server-ejb -am -Dtest=InvoiceTotalTest package`: `testPoolChangeRecalculatesGross` fails with `expected:<100.00> but was:<119.00>`. Red check of the calculation itself (01.11.): against master `testGrossWithoutTaxes` fails with `expected:<150.00> but was:<172.50>`. The worktree was removed afterwards, nothing committed.
