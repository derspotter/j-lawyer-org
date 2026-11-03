# Re-review of notes/belegsumme-ohne-ust, 03.11.2026

Codex gpt-6-astra, read-only, against origin/master 4dc03980f. Same rules as notes/review-2026-11-02/README.md: new commits only, no amend or force-push, author derspotter, answer points you disagree with in report.md under "Antwort auf Review 03.11.".

1. **RESOLVED — Existing-data gap.** `InvoiceEntryPanel.java:1057` now retains the invoice returned by `updateInvoice()`, so the payment proposal uses recalculated totals. Missing automated Swing coverage is explicitly disclosed in `pr.md:22`.

2. **NOT RESOLVED — Pool-change regression coverage.** The new test detects removal of the recalculation, but swallows all exceptions and checks the mutated fixture instead of the returned invoice. Successful completion remains untested. See finding below.

3. **RESOLVED — Test counts.** `pr.md:18` and `report.md:33` consistently report branch totals of 1100 total, 1084 executed, 16 skipped, 18 failed, versus baseline totals of 1097, 1081, 16, 18.

4. **RESOLVED — Baseline command.** `pr.md:17` and `report.md:22` now give the same complete build command for both revisions, selecting client and EJB modules with reactor dependencies.

Two concrete findings remain:

1. **[P2] `InvoiceTotalTest.java:751–760` — The pool-change test accepts a failed service call.** `catch (Throwable)` suppresses every failure, including unexpected failures after recalculation. Assertions inspect the same object returned by the facade stub, so the test can pass even when `updateInvoiceType()` never returns successfully or returns incorrect data. **Suggested fix:** stub the public `addCaseHistory()` method in the test fixture, remove the blanket catch, capture the returned invoice, and assert its totals when switching between VAT settings.

2. **[P2] `report.md:66` — The mutation-test command is incomplete.** With the repository’s Surefire 3.2.5 configuration, `-am -Dtest=InvoiceTotalTest` fails in dependency modules without that test before reaching the EJB module. Surefire defaults `failIfNoSpecifiedTests` to `true`, and the repository provides no override. **Suggested fix:** document the exact invocation and environment overrides actually used, including `-Dsurefire.failIfNoSpecifiedTests=false` if applicable. Include any compiler overrides required by the stated JDK setup.

Otherwise, the production changes remain focused and consistent with the callers, null handling, existing-data limitation, Swing threading, and server transactions. The unchanged `.form` remains consistent because only an existing handler body changed. No Remote interfaces, migrations, or unrelated refactoring are included. Both commits contain author and co-author metadata. Commit messages and PR text match the changes and contain no semicolons.

Read-only review completed. No files modified and no builds or tests executed independently.

VERDICT: NEEDS-CHANGES
