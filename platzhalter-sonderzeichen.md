# Review of fork/fix/platzhalter-sonderzeichen (notes/platzhalter-sonderzeichen), 02.11.2026

Codex gpt-6-astra, read-only, against origin/master 7b3b3dfa8. Fix each point as a new commit on the same branch, or answer it in report.md.

1. **`fork/notes/platzhalter-sonderzeichen:pr.md:16` — Test coverage is overstated.** The report records 1100 tests **including 16 skipped**, so 1084 actually ran. The PR also omits the report’s explicit “No GUI run” limitation. State “1100 tests reported, 16 skipped, 18 failed” and add the missing GUI limitation under “Nicht abgedeckt”.

2. **`fork/notes/platzhalter-sonderzeichen:report.md:27` — Baseline command is described inaccurately.** “The same command” contradicts the documented addition of `j-lawyer-backupmgr` to the baseline’s `-pl` selection. Describe this as identical options with a different module selection, and give both exact commands.

No code defects or scope violations found. Null handling, trimming, threading and transaction boundaries remain unchanged. No GUI classes, Remote interfaces or database definitions changed. Commit authorship and co-authorship match the report, and commit/PR text contain no semicolons.

Independently ran the actual three new JUnit tests entirely in memory: **3 passed with the fix**, versus **2 failures on `origin/master`**. Full Maven reactor and GUI tests were not rerun. No files modified.

VERDICT: NEEDS-CHANGES
