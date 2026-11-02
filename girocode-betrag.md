# Review of fork/fix/girocode-betrag (notes/girocode-betrag), 02.11.2026

Codex gpt-6-astra, read-only, against origin/master 7b3b3dfa8. Fix each point as a new commit on the same branch, or answer it in report.md.

1. `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/files/InvoiceDialog.java:3097` — Es fehlt ein dauerhafter Regressionstest. Die externe Zahlenprobe prüft nachgebildete Logik, nicht den Produktionspfad. Vorschlag: Das Einlesen in eine kleine GUI-unabhängige Methode auslagern, die der Handler verwendet. Mindestens `150.000,01 → 150000.01`, ungültige Eingaben und das unveränderte ursprüngliche Zahlenformat testen.

2. `fork/notes/girocode-betrag:report.md:26` und `pr.md:19` — Die Testbeschreibung ist widersprüchlich beziehungsweise unvollständig. Der Baseline-Build wird als „same command“ bezeichnet, verwendet aber eine andere `-pl`-Auswahl. Außerdem fehlt im PR die im Bericht ausdrücklich genannte Einschränkung „No GUI run“. Vorschlag: Beide tatsächlich verwendeten Befehle vollständig angeben, den Vergleich auf gemeinsame Module begrenzen und den fehlenden GUI-Test im PR nennen.

Keine funktionale Regression gefunden. Die unabhängige Java-17-Zahlenprobe bestätigt sämtliche angegebenen Fehlerzahlen und null Abweichungen nach dem Fix. `.form` bleibt konsistent, Remote-Interfaces, Transaktionen und Datenbankschema sind unverändert. Commit-Autor und Co-Author sind ausgewiesen, Commit und PR enthalten keine Semikolons. Maven und GUI habe ich nicht ausgeführt. Keine Dateien geändert.

VERDICT: NEEDS-CHANGES
