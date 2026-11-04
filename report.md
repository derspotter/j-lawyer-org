# Report 3402

- Branch: `fix/3402-signatur-abstand-normal` (fork derspotter/j-lawyer-org), base upstream/master 05f6b4350
- Commits (newest first):
  - 531129f1a derspotter <jayjag@posteo.de> | fix(e-mail): Zwischen Vorlagentext und Signatur steht im HTML-Editor eine normale Leerzeile statt eines Absatzes, auch in Antworten nur eine. Closes #3402

## Files (+/- against master)

| File | + | - |
|---|---|---|
| `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/mail/SendEmailFrame.java` | 22 | 3 |
| `j-lawyer-client/src/test/java/com/jdimension/jlawyer/client/mail/SignatureSpacingTest.java` | 698 | 0 |
| total | 720 | 3 |

## Build and tests

JDK: Ubuntu OpenJDK 17.0.20 + OpenJFX 17.0.9 from Maven Central, combined with jlink. Maven 3.9.11. Branch at commit 531129f1a built (started 04.11.2026 07:51 UTC) and master 05f6b4350 built (04.11.2026, in a separate worktree) with the identical command in the same container:

```
mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package
```

| Module | branch total / executed / skipped / failed | master total / executed / skipped / failed |
|---|---|---|
| j-lawyer-client | 117 / 117 / 0 / 0 | 113 / 113 / 0 / 0 |
| j-lawyer-cloud | 4 / 4 / 0 / 0 | 4 / 4 / 0 / 0 |
| j-lawyer-fax | 23 / 23 / 0 / 0 | 23 / 23 / 0 / 0 |
| j-lawyer-server-common | 118 / 118 / 0 / 17 | 118 / 118 / 0 / 17 |
| j-lawyer-server-entities | 56 / 56 / 0 / 0 | 56 / 56 / 0 / 0 |
| total | 318 / 318 / 0 / 17 | 314 / 314 / 0 / 17 |

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

- Base: the branch starts at upstream/master 05f6b4350, one commit after 2f81c1064 (05f6b4350 "HTML-Inhalte in E-Mail-Ansicht und Vorschau aufbereiten" was the current master when the branch was created). The comparison build is master 05f6b4350 with the same command.
- Predecessor: #3263, fixed by Jens in 0a2e62097. It reduced the gap to one `<br/>` (HTML) and one line separator (text) and stopped adding separators around an empty pre-signature in `setBody()` for text mails. The HTML separators stayed bare `<br/>`, which SunEditor turns into a `<p>`.
- Cause confirmed in the browser: SunEditor template and script from `j-lawyer-client/src/main/resources/resources/suneditor`, loaded in the preinstalled Chromium via Playwright, `window.editorAPI.setText()` with the HTML the frame builds, then `getText()` and the bounding boxes of the last template line and the first signature line.

```
new mail, old (<br/>):        <div>...</div><div>anbei der Entwurf.</div><p><br></p><div>Mit freundlichen Grüßen</div>...      gap 30px, line 20px
new mail, new (<div><br></div>): <div>...</div><div>anbei der Entwurf.</div><div><br></div><div>Mit freundlichen Grüßen</div>... gap 20px
reply, old (2x <br/>):        ...<p><br><br></p><div>Mit freundlichen Grüßen</div>...                                          gap 49px
reply, new:                   ...<div><br></div><div>Mit freundlichen Grüßen</div>...                                          gap 20px
```

- Callers of `setBody(pre, post, type)` for replies and forwards pass an empty `pre` (EmailInboxPanel and others), so in replies the text before the signature is normally empty.
- Merge check with open PRs (fetched as upstream pull/<n>/head): #3718 5c2b82169 (SendEmailFrame), #3721 9f35d5b8a (WebViewHtmlEditorPanel), #3724 fc9e24441 (EmailUtils, SendEmailFrame, test). `git merge-tree --write-tree HEAD <pr>` reports no conflict for each. #3718 and #3724 also merge with each other.
