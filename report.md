# Report #2169

- Branch: `fix/2169-warnung-unbestaetigter-nummernkreis` (fork derspotter/j-lawyer-org)
- Commit: `c5c6af6f8ef1e6719f7a76f29ff90badc197fbbc`
- Subject: fix(finanzen): Warnung beim Speichern eines Belegs mit unbestätigter Belegart/Nummernkreis. Closes #2169
- Author: derspotter <jayjag@posteo.de>
- Base: upstream/master `7b3b3dfa8`

## Files (+/-)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/InvoiceDialog.java` | 8 | 0 |
| total | 8 | 0 |

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
- No GUI run. `cmbInvoicePool.isEnabled()` is the edit-mode flag used by `cmdConfirmInvoiceNumberActionPerformed()` itself. `cmdSave` is only enabled for an existing invoice (`setEntry`), so the warning cannot appear for a new invoice before the first confirmation.

## Open questions
- The reporter wanted save to apply the pending pool change automatically. j-dimension (2023-10-27) said it must be confirmed explicitly and suggested a warning instead, which is what this does.
- Same file as fix/3371 (different method). `git merge-tree` with fix/3371, fix/2278 and fix/2850 is clean.
