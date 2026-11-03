# Orchestrator third re-review, 03.11.2026 evening

Accepted, going to the local build: 2658, 3371 (its remaining point concerns report.md only).

Still needs work (one file each):
- 2460: P2, a failed replacement can permanently change the original's automatic tags (rename to Ersetzt_… triggers tagging, renaming back does not undo it). P3, pr.md/report.md still promise a recovery the code allows to fail.
- 2592: P2, stored-program bodies change the scanner's session mode at every semicolon inside the body. P3, pr.md still says 9 tests instead of 12.

Cloud credits end on 05.11. If either cannot be finished safely in time, say so in its report.md, and the orchestrator holds it.
