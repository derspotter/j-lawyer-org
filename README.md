# Orchestrator re-review for cloud lane 1, 03.11.2026 afternoon

- 1283: the P1 (dropdown values) is RESOLVED. The P2 is still open: after a failed save, the dirty check treats unsaved changes as clean, so the next "neu" can discard them without asking (NewAddressPanel ~752).
- 3407: all earlier points are RESOLVED, including the master conflict. New P2: ProgressIndicator uses DISPOSE_ON_CLOSE, so closing the progress window can return from the background validation before it finishes (ThreadUtils ~1061). Make the caller wait for the result, or make the dialog non-closable while validating. P3: the test-count baseline in report.md (FileUtilsTest goes from 2 to 6 against master).

Cloud credits end on 05.11. If either cannot be finished safely, say so in its report.md, and the orchestrator holds it.
