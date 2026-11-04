# Report 2888

- Branch: `fix/2888-text-vorlagen-leere-platzhalter` (fork derspotter/j-lawyer-org), base upstream/master 05f6b4350
- Commits (newest first):
  - 0a7ae6a4f derspotter <jayjag@posteo.de> | fix(e-mail): In Textvorlagen hinterlässt ein leerer Platzhalter zwischen zwei Leerzeichen nur ein Leerzeichen, HTML- und Dokumentvorlagen bleiben unverändert. Closes #2888

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/mail/SendEmailFrame.java` | 1 | 1 |
| `j-lawyer-server-common/src/main/java/com/jdimension/jlawyer/documents/CommonTemplatesUtil.java` | 20 | 0 |
| `j-lawyer-server-common/src/test/java/org/jlawyer/test/documents/ReplacePlaceHoldersTest.java` | 30 | 0 |
| total | 51 | 1 |

## Build and tests

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch at commit 0a7ae6a4f built (started 04.11.2026 07:53 UTC) and master 05f6b4350 built (04.11.2026, in a separate worktree) with the identical command in the same container:

```
mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| j-lawyer-client | 113 / 113 / 0 / 0 | 113 / 113 / 0 / 0 |
| j-lawyer-cloud | 4 / 4 / 0 / 0 | 4 / 4 / 0 / 0 |
| j-lawyer-fax | 23 / 23 / 0 / 0 | 23 / 23 / 0 / 0 |
| j-lawyer-server-common | 120 / 120 / 0 / 17 | 118 / 118 / 0 / 17 |
| j-lawyer-server-entities | 56 / 56 / 0 / 0 | 56 / 56 / 0 / 0 |
| total | 316 / 316 / 0 / 17 | 314 / 314 / 0 / 17 |

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

## Notes

- Base: upstream/master 05f6b4350 (one commit after 2f81c1064), comparison build of master 05f6b4350 with the same command.
- #3750 (merged as 416b6a3fb, `Matcher.quoteReplacement`) is part of the base. The new method delegates to `replacePlaceHolders()`, so values are still inserted literally. Its own removal only runs for empty values and replaces with an empty string.
- Callers that fill template bodies: `SendEmailFrame` text template branch (changed), `SendEmailFrame` HTML template branch (unchanged), `SendBeaMessageFrame` (text templates, unchanged), `TemplatesEndpointV6` (REST, unchanged).
- On master the new test methods do not compile (the method is new). The behaviour of the text branch on master is that of `replacePlaceHolders()`, which the unchanged test `testRegularValues` pins down as "a  b".
