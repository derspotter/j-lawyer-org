# Report #2203

- Branch: `fix/2203-belegnummer-konflikt-bei-nummernkreiswechsel` (fork derspotter/j-lawyer-org)
- Commit: `4bcf4af2e4106730bc40d2ebf4039f3bb38c2714`
- Subject: fix(finanzen): klare Fehlermeldung bei bereits vergebener Belegnummer beim Wechsel des Nummernkreises. Refs #2203
- Author: derspotter <jayjag@posteo.de>
- Base: upstream/master `7b3b3dfa8`

## Files (+/-)

| File | + | - |
|---|---|---|
| `j-lawyer-server/j-lawyer-server-ejb/src/main/java/com/jdimension/jlawyer/services/ArchiveFileService.java` | 15 | 8 |
| `j-lawyer-server/j-lawyer-server-ejb/src/test/java/org/jlawyer/test/server/ejb/InvoiceTypeChangeConflictTest.java` | 768 | 0 |
| total | 783 | 8 |

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
| j-lawyer-server/j-lawyer-server-ejb | tests=830 failures=1 errors=0 skipped=16 | tests=829 failures=1 errors=0 skipped=16 |

```
TOTAL tests=1017 failures=18 errors=0 skipped=16
failing classes: ['j-lawyer-server-common FtpTest', 'j-lawyer-server-common SftpTest', 'j-lawyer-server-common SmbTest', 'j-lawyer-server/j-lawyer-server-ejb LibreOfficeODFTest']
new failures vs master: none
master failures not present: none
```

## Evidence
- Unit test with java.lang.reflect.Proxy fakes for SessionContext, ArchiveFileBeanFacadeLocal, InvoicePoolFacadeLocal, InvoiceServiceLocal, InvoiceFacadeLocal (no Mockito in the module). Asserts the exception message, unchanged invoice number/pool and that `edit()` is never called.
- Unique index `IDX_INVOICES_INVOICENO_UQ` on `invoices.invoice_no` (migration V2_3_0_14).

## Unverified
- Transaction behaviour reasoned from EJB rules, not observed on WildFly: before, the duplicate number hit the unique index at commit -> rollback of everything incl. the pool counter, so a retry got the same number. Now a checked `Exception` is thrown before the entity is touched -> application exception, no rollback, the pool counter increment from `nextInvoiceNumber()` is committed (same as `addInvoice()` since 80ab1c597), a retry gets the next number.
- The success path of `updateInvoiceType()` is not unit-tested (it ends in `addCaseHistory()`, which needs JDBC).

## Open questions
- `Refs`, not `Closes`: the issue is also a question about resetting pools. j-dimension prefers cancelling wrong invoices over deleting them.
- Reordering: pool change now runs before type change inside the same `if`. Same result on success, but a failing pool change no longer leaves a type change in the managed entity.
