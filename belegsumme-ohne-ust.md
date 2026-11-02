# Review of fork/fix/belegsumme-ohne-ust (notes/belegsumme-ohne-ust), 02.11.2026

Codex gpt-6-astra, read-only, against origin/master 7b3b3dfa8. Fix each point as a new commit on the same branch, or answer it in report.md.

1. **Existing-data gap — `InvoiceEntryPanel.java:1056`, `report.md:56`.** The green-check handler discards the invoice returned by `updateInvoice()`. For an existing VAT-free invoice stored as net 150.00 / gross 172.50, the server now corrects gross to 150.00, but line 1071 still proposes a payment using the stale 172.50. This existing bug remains despite the report claiming that this consumer is corrected. **Fix:** retain the returned invoice before calculating the payment and add a regression test, or explicitly document this remaining limitation.

2. **Missing regression coverage — `InvoiceTotalTest.java:703`, `ArchiveFileService.java:7139`.** Both tests invoke the private calculation directly. Neither exercises the newly added recalculation after a pool change, so deleting that entire addition would leave both tests passing. **Fix:** test `updateInvoiceType()` switching between pools with opposite VAT settings and assert the returned totals.

3. **Incorrect test evidence — `pr.md:17`.** “1018 ausgeführt” includes the 16 skipped tests recorded in `report.md:38`. According to that report, 1002 tests executed, with 18 failures, and 16 were skipped. **Fix:** report those counts explicitly, including the corresponding baseline counts.

4. **Inconsistent baseline command — `report.md:27`.** “The same command” conflicts with the different `-pl` selection shown immediately afterward. **Fix:** provide both complete commands and identify the common modules used for the failure comparison.

Otherwise, the calculation, flag semantics and transaction placement look consistent. No GUI, Remote-interface, migration or unrelated refactoring changes appear. Author and co-author metadata are present. Commit and PR text contain no semicolons.

Read-only review completed. No files changed and no builds or tests executed independently.

VERDICT: NEEDS-CHANGES
