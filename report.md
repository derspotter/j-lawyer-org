# Report #bea-loeschen-fehler-anzeigen

- Branch: `fix/bea-loeschen-fehler-anzeigen` (fork derspotter/j-lawyer-org)
- Commit: `9d1c5b159ae5e73356ab2ec17644d59b676ebf1e`
- Subject: fix(bea): Hinweis, wenn markierte beA-Nachrichten nicht gelöscht werden können. Refs #3396
- Author: derspotter <jayjag@posteo.de>
- Base: upstream/master `7b3b3dfa8`

## Files (+/-)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/bea/BeaInboxPanel.java` | 9 | 0 |
| total | 9 | 0 |

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
- `BeaInboxPanel.cmdDeleteActionPerformed()` on master: per message `catch (Throwable ex) { log.error(ex) }` and an ignored `false` result. `mnuEmptyTrashActionPerformed()` in the same class already counts failures and shows "N Vorgänge fehlgeschlagen, letzte Meldung: ...".

## Unverified
- Not run against beA/beAstie (not allowed). No unit test, the message depends on the server calls inside a SwingWorker.

## Open questions
- `Refs #3396` (found while triaging it). Does not fix why deleting fails, only makes it visible.
- Files touched: `BeaInboxPanel` only. Not touched: SendBeaMessageFrame, SendBeaMessageAction, BeaMessageContentUI, BeaAccess. Same file as fix/2658 (different method), merge checked before push.
