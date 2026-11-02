# Orchestrator review of the cloud fixes, 02.11.2026

One read-only Codex review (gpt-6-astra) per branch, against origin/master 7b3b3dfa8: commit message, diff, `notes/<x>/pr.md` and `report.md`. Every branch merges cleanly with master and with the open PRs that touch the same files.

## OK (local client build on the desktop running)

- fix/2111-address-header-right-gap
- fix/2169-warnung-unbestaetigter-nummernkreis
- fix/bea-loeschen-fehler-anzeigen
- fix/belege-bezahlt-nach-bearbeiten

Nothing to do for these unless the build fails. The orchestrator will tell you if it does.

## Needs changes (one file per branch in this directory)

1705, 1779, 2203, 2278, 2460, 2592, 2658, 2850, 3371, 3407, 3410, 3507, belegsumme-ohne-ust, girocode-betrag, imap-timeouts-postfachscanner, platzhalter-sonderzeichen, restore-sql-fehler-erkennen, smtp-timeouts

How to handle them:
- Fix each point as a NEW commit on the same `fix/*` branch. No amend and no force-push. Author: `derspotter <jayjag@posteo.de>`, plus the line `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`.
- If you think a point is wrong, don't change the code. Explain why in `report.md` under the heading "Antwort auf Review 02.11.".
- Then update `notes/<x>/pr.md` and `report.md`:
  - no semicolons
  - the exact build command
  - executed, skipped and failed test counts given separately
  - what was not tried in a running client or against a server
- Push both the `fix/*` branch and the `notes/*` branch. The orchestrator picks them up at the next morning triage.

## Dropped

- fix/aktenvorschlag-grossbuchstaben: its diff is identical to the local beA lane's `fix/aktenvorschlag-grossschreibung`, which is already queued for posting. Stop working on it.

## Author rewrite, done by the orchestrator

- fix/2151 (commit b6cbd7e), fix/1283, fix/2544, fix/3165 and fix/folderlistcell-reject-drop still have commits authored by `Claude <noreply@anthropic.com>`. The cloud session can't force-push, so the orchestrator rewrites the author locally before posting. Nothing to do for this. Any text corrections from the 01.11. review still apply.
