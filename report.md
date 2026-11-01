# Report #belegsumme-ohne-ust

- Branch: `fix/belegsumme-ohne-ust` (fork derspotter/j-lawyer-org)
- Commit: `019662b63c85e7cb812e0c9f15c4ea073dbc5bd2`
- Subject: fix(finanzen): Bruttosumme eines Belegs ohne ausgewiesene USt entspricht der Nettosumme
- Author: derspotter <jayjag@posteo.de>
- Base: upstream/master `7b3b3dfa8`

## Files (+/-)

| File | + | - |
|---|---|---|
| `j-lawyer-server/j-lawyer-server-ejb/src/main/java/com/jdimension/jlawyer/services/ArchiveFileService.java` | 15 | 7 |
| `j-lawyer-server/j-lawyer-server-ejb/src/test/java/org/jlawyer/test/server/ejb/InvoiceTotalTest.java` | 730 | 0 |
| total | 745 | 7 |

## Build and tests vs. baseline

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink (`/opt/jdk17fx`). Maven 3.9.11.

```
mvn -B -ntp -pl j-lawyer-server/j-lawyer-server-ejb -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

Reactor result: BUILD SUCCESS (all modules compiled and packaged)

Baseline = the same command on upstream/master 7b3b3dfa8 (`-pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb,j-lawyer-backupmgr -am`).

| Module | branch | master |
|---|---|---|
| j-lawyer-cloud | tests=4 failures=0 errors=0 skipped=0 | tests=4 failures=0 errors=0 skipped=0 |
| j-lawyer-fax | tests=23 failures=0 errors=0 skipped=0 | tests=23 failures=0 errors=0 skipped=0 |
| j-lawyer-server-common | tests=104 failures=17 errors=0 skipped=0 | tests=104 failures=17 errors=0 skipped=0 |
| j-lawyer-server-entities | tests=56 failures=0 errors=0 skipped=0 | tests=56 failures=0 errors=0 skipped=0 |
| j-lawyer-server/j-lawyer-server-ejb | tests=831 failures=1 errors=0 skipped=16 | tests=829 failures=1 errors=0 skipped=16 |

```
TOTAL tests=1018 failures=18 errors=0 skipped=16
failing classes: ['j-lawyer-server-common FtpTest', 'j-lawyer-server-common SftpTest', 'j-lawyer-server-common SmbTest', 'j-lawyer-server/j-lawyer-server-ejb LibreOfficeODFTest']
new failures vs master: none
master failures not present: none
```

## Evidence
- Red/green: `InvoiceTotalTest` against master's `updateInvoiceTotal()` (run with `-Dtest=InvoiceTotalTest`): `testGrossWithoutTaxes` fails with `expected:<150.00> but was:<172.50>`. With the change both tests pass. The test calls the private method via reflection with proxy facades (no Mockito in the module).
- Semantics of the flag confirmed by j-dimension in #3170: `smallBusiness=true` means "USt ausweisen" (name kept for REST compatibility).
- Client already treats invoices without VAT as net: `InvoiceDialog.updateTotals()` (gross = net if `chkTaxes` unchecked), `InvoiceEntryPanel` / `ArchiveFilePanel.loadAccountEntries()` (`isSmallBusiness() ? getTotalGross() : getTotal()`).
- Consumers of the stored gross that were wrong for such invoices: `BankTransactionProposal` / `BankTransactionEntryPanel` (bank statement matching), `InvoicesOpenTimerTask` (desktop), `ReportService` (Bruttobetrag), `AddressPanel` (invoice sum of a contact), `InvoiceService` due-invoice mails, `GlobalSearchResultItem`, `ToolRegistry` (Ingo), REST.

## Unverified
- No run against a database. Existing invoices keep the stored value until they are saved again (no data migration, schema/migrations are off limits).

## Open questions
- No issue of its own (found while triaging #3170 / #3488).
- The recalculation after a pool change in `updateInvoiceType()` is in the same method as fix/2203 (different lines), `git merge-tree` is checked before push.
- The proposed amount of the green check mark (`InvoiceEntryPanel.cmdMarkAsPayedActionPerformed`) still uses `getTotalGross()`, which is now correct for invoices without VAT as well.
