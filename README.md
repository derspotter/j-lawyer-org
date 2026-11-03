# Orchestrator re-review of the cloud fixes, 03.11.2026

One read-only Codex re-review per branch, against origin/master 4dc03980f (Jens pushed 4 commits on 02./03.11.). Each re-review checked yesterday's points and any new commits.

## Accepted (going to the local build, then the ready queue)

2151, 3165, platzhalter-sonderzeichen, 3410, smtp-timeouts, 1705, 1779, 2203, 2278, 2850, imap-timeouts-postfachscanner, restore-sql-fehler-erkennen

Nothing to do for these. Many of their pr.md files say that SmbTest needs credentials from environment variables. That's wrong: SmbTest only needs a reachable SMB server. The orchestrator corrects this sentence when posting.

## Still needs work (one file per branch in this directory)

- Cloud fix lane: 2460, 2592, 2658, 3371, 3507, belegsumme-ohne-ust, girocode-betrag
- Cloud lane 1: 1283, 3407. 3407 also conflicts with master (FileUtils.java, Jens's ee897221b). Merge origin/master in, without rebase or force-push.

The rules are the same as in notes/review-2026-11-02/README.md:
- new commits on the same fix/* branch
- author derspotter <jayjag@posteo.de> plus Co-Authored-By Claude
- answer any point you disagree with in report.md under "Antwort auf Review 03.11."
- update pr.md and report.md (no semicolons, exact build command, honest test counts)
- push both branches

Cloud credits end on 05.11. Prioritise the P1 points: 3507 (excluded pages still reachable) and 2460 (wrong document can be deleted). Then the P2 points.

## Handled by the orchestrator

- 2544 and folderlistcell-reject-drop: only their commit messages still contain a wrong claim. The cloud can't amend them, so the orchestrator rewords them locally. Nothing to do for this.
