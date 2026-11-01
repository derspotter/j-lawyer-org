# Report #1705

- Branch: `fix/1705-bea-symbol-desktop-grau` (fork derspotter/j-lawyer-org)
- Commit: `fe78a93c95e8150cf7208910ccf7db80c0f719ed`
- Subject: fix(bea): beA-Symbol auf dem Desktop grau, wenn keine ungelesenen Nachrichten vorliegen. Closes #1705
- Author: derspotter <jayjag@posteo.de>
- Base: upstream/master `7b3b3dfa8`

## Files (+/-)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/desktop/DesktopPanel.java` | 3 | 1 |
| total | 3 | 1 |

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
- No GUI run. The label starts disabled in `initComponents` ("?" / "noch nicht eingeloggt"). Same enable/disable pattern as `lblScans`, `lblMailingStatus` and `lblUnreadInstantMessages` in the same `onEvent()`.

## Open questions
- Behaviour change: the grey beA label is no longer clickable (`configureStatusLabel()` returns early for disabled labels). Same as the other grey status labels, but before this change the beA label was always clickable after login. If clicking should stay possible, the alternative is a grey icon on an enabled label, which needs more code.
- The mail label (`lblUnreadMail`) is also always enabled. Not touched, the issue is about beA only.
