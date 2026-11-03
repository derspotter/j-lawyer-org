# Report belegsumme-ohne-ust

- Branch: `fix/belegsumme-ohne-ust` (fork derspotter/j-lawyer-org), base upstream/master 7b3b3dfa8
- Commits (newest first):
  - 01e8fee26 derspotter <jayjag@posteo.de> | test(finanzen): Test für den Wechsel des Nummernkreises prüft den zurückgegebenen Beleg in beide Richtungen ohne pauschales Abfangen von Fehlern
  - fb6e7dfeb derspotter <jayjag@posteo.de> | fix(finanzen): grüner Haken rechnet mit den vom Server neu berechneten Belegsummen, Test für den Nummernkreiswechsel
  - 019662b63 derspotter <jayjag@posteo.de> | fix(finanzen): Bruttosumme eines Belegs ohne ausgewiesene USt entspricht der Nettosumme

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/InvoiceEntryPanel.java` | 2 | 1 |
| `j-lawyer-server/j-lawyer-server-ejb/src/main/java/com/jdimension/jlawyer/services/ArchiveFileService.java` | 15 | 7 |
| `j-lawyer-server/j-lawyer-server-ejb/src/test/java/org/jlawyer/test/server/ejb/InvoiceTotalTest.java` | 836 | 0 |
| total | 853 | 8 |

## Build and tests (03.11.2026)

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
- Temporary worktree of this branch without the recalculation in `updateInvoiceType()`, `JAVA_HOME=/opt/jdk17fx mvn -B -ntp -q -pl j-lawyer-server/j-lawyer-server-ejb -am -Dtest=InvoiceTotalTest -Dsurefire.failIfNoSpecifiedTests=false -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 package` (the exact command of the script that ran it, the 02.11. report had shortened it): `testPoolChangeRecalculatesGross` fails with `expected:<100.00> but was:<119.00>`. Red check of the calculation itself (01.11.): against master `testGrossWithoutTaxes` fails with `expected:<150.00> but was:<172.50>`. The worktree was removed afterwards, nothing committed.

## Antwort auf Review 03.11.
1. [P2] The pool-change test accepted a failed call: agreed, fixed in 01e8fee26. The blanket `catch (Throwable)` is gone. The test uses an anonymous subclass of `ArchiveFileService` that overrides the public `addCaseHistory(String, ArchiveFileBean, String)` and records the description, so the JDBC update of the case is not reached. The invoice facade is backed by a small store that returns a copy on `find` and stores a copy on `edit`, so the assertions only see what the service actually stored and returned. The test switches from the pool with VAT to the one without (returned invoice: number R-2, net 100.00, gross 100.00) and back (R-3, gross 119.00) and checks both history entries.
2. [P2] Mutation command: agreed, the command in the 02.11. report was shortened. The script that ran it used `-Dsurefire.failIfNoSpecifiedTests=false`, `-Dmaven.test.failure.ignore=true` and the compiler overrides of the build command, the full command is now given above. The 03.11. mutation check was run without Maven, see Evidence 03.11.
3. [P3, from the README] SMB test prerequisites: agreed. FtpTest and SftpTest read their credentials from environment variables, SmbTest uses the hard-coded share smb://j-lawyer-box/j-lawyer-share/. pr.md now says this.

## Evidence 03.11.
- `InvoiceTotalTest` with plain javac + JUnit against the compiled EJB module of this worktree (classpath taken from the surefire report of the previous build): 3 of 3 pass.
- Mutation: the branch's `ArchiveFileService` without the recalculation in `updateInvoiceType()`, compiled in front of the module classes: `testPoolChangeRecalculatesGross` fails with `expected:<100.00> but was:<119.00>`. With `ArchiveFileService` from master 7b3b3dfa8: `testGrossWithoutTaxes` fails with `expected:<150.00> but was:<172.50>` and `testPoolChangeRecalculatesGross` with `expected:<100.00> but was:<119.00>`.
