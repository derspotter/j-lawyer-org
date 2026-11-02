# Report 1705

- Branch: `fix/1705-bea-symbol-desktop-grau` (fork derspotter/j-lawyer-org), base upstream/master 7b3b3dfa8
- Commits (newest first):
  - b1c5b976d derspotter <jayjag@posteo.de> | fix(bea): beA-Symbol auf dem Desktop bleibt ohne ungelesene Nachrichten anklickbar, nur das Symbol wird grau. Refs #1705
  - fe78a93c9 derspotter <jayjag@posteo.de> | fix(bea): beA-Symbol auf dem Desktop grau, wenn keine ungelesenen Nachrichten vorliegen. Closes #1705

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/desktop/DesktopPanel.java` | 8 | 0 |
| total | 8 | 0 |

## Build and tests (02.11.2026)

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch and master 7b3b3dfa8 built with the identical command in the same container (master in a separate worktree):

```
mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| j-lawyer-client | 81 / 81 / 0 / 0 | 81 / 81 / 0 / 0 |
| j-lawyer-cloud | 4 / 4 / 0 / 0 | 4 / 4 / 0 / 0 |
| j-lawyer-fax | 23 / 23 / 0 / 0 | 23 / 23 / 0 / 0 |
| j-lawyer-server-common | 104 / 104 / 0 / 17 | 104 / 104 / 0 / 17 |
| j-lawyer-server-entities | 56 / 56 / 0 / 0 | 56 / 56 / 0 / 0 |
| total | 268 / 268 / 0 / 17 | 268 / 268 / 0 / 17 |

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

## Antwort auf Review 02.11.
1. [P2] Navigation lost: agreed, fixed in b1c5b976d. The label stays enabled after login, only its icon switches between the original and `UIManager.getLookAndFeel().getDisabledIcon(label, icon)` (the look and feel's own disabled rendering, so it matches the other grey icons). `configureStatusLabel()` keeps accepting clicks.
2. [P3] GUI limitation: fixed, the PR states that appearance, transitions and clicking were not tested interactively.

## Unverified
- No GUI run (no display in this environment).
