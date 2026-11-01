# Report #3371

- Branch: `fix/3371-belegdialog-eingaben-behalten` (fork derspotter/j-lawyer-org)
- Commit: `12f93c1fd83c13792232e071de0f127cca7949ad`
- Subject: fix(finanzen): Belegdialog verwirft beim erneuten Bestätigen von Belegart/Nummernkreis keine Eingaben mehr. Closes #3371
- Author: derspotter <jayjag@posteo.de>
- Base: upstream/master `7b3b3dfa8`

## Files (+/-)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/InvoiceDialog.java` | 8 | 2 |
| total | 8 | 2 |

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
- No GUI run against a server. The change is in a modal Swing dialog. Reviewed by reading the code paths: `save()` persists positions (`updateInvoicePosition`) and the invoice (`updateInvoice`, copies all form fields incl. status, contact, sender, payment type), then `updateInvoiceType()` only changes type/pool/number on the stored entity and returns it, `setEntry()` reloads from the server.

## Open questions
- Behaviour change: input entered before re-confirming the pool is now persisted even if the user cancels the dialog afterwards. Same pattern the dialog already uses before creating/uploading the invoice document.
- `save()` signature changed from `void` to `boolean` (private, other callers ignore the value).
- Merge check: `git merge-tree` against fix/2169 (same file), fix/2278, fix/2850 is clean.
