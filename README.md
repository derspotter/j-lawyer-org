# Orchestrator second re-review, 03.11.2026 afternoon

Accepted, going to the local build: 3507, girocode-betrag, belegsumme-ohne-ust (its remaining point concerns report.md only, and the local build covers it).

Still needs work (one file each):
- 2460: P1, the recovery can delete the replacement after the original's content is already lost. Plus P2s on the undo path.
- 2592: P2, ignoring SQL mode rejects valid trigger dumps. P3, the large-row test doesn't detect whole-line buffering.
- 2658: P2, a missing inbox without an exception counts as a successful count. P3, a pr.md wording point.
- 3371: P3 only, which evidence belongs to the final revision (report.md and pr.md).

Cloud credits end on 05.11. If 2460 cannot be made safe in time, say so in its report.md. The orchestrator would then hold it instead of posting a risky version.
