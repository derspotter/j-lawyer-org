# Re-review of notes/girocode-betrag, 03.11.2026

Codex gpt-6-astra, read-only, against origin/master 4dc03980f. Same rules as notes/review-2026-11-02/README.md: new commits only, no amend or force-push, author derspotter, answer points you disagree with in report.md under "Antwort auf Review 03.11.".

1. **RESOLVED – Regressionstest:** `AmountUtilsTest.java:683` testet die vom Handler verwendete Produktionsmethode, einschließlich `150.000,01 → 150000.01`, ungültiger Eingabe und unverändertem Format. Alle vier Tests bestehen. Mit der alten Float-Logik schlagen beide Genauigkeitstests fehl.

2. **RESOLVED – Testbeschreibung:** `fork/notes/girocode-betrag:pr.md:16` und `report.md:19` nennen jetzt denselben vollständigen Maven-Befehl und den konkreten Vergleichsstand. Testzahlen und Modulumfang sind konsistent. Der fehlende GUI-Test steht ausdrücklich in `pr.md:21`. Die Maven-Läufe habe ich nicht selbst wiederholt.

Zwei neue sachliche Dokumentationsfehler bleiben:

1. **[P3] `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/utils/AmountUtils.java:679`** — Die JavaDoc ordnet sowohl `float` als auch `double` den Verlust der Cent-Genauigkeit ab ungefähr 131.000 zu. Diese Größenordnung betrifft `float`. **Vorschlag:** Die Grenze ausdrücklich nur auf `float` beziehen und `BigDecimal` als Vermeidung binärer Rundungsfehler beschreiben.

2. **[P3] `fork/notes/girocode-betrag:pr.md:18`** — Die Aussage über Zugangsdaten aus Umgebungsvariablen trifft auf FTP/SFTP zu, nicht auf `SmbTest`. Dieser verwendet fest hinterlegte Pfade unter `smb://j-lawyer-box/j-lawyer-share/`, beispielsweise in `SmbTest.java:715`. **Vorschlag:** FTP/SFTP-Konfiguration und die erforderliche SMB-Testfreigabe getrennt nennen.

Keine funktionale Regression gefunden. Null-/Fehlerbehandlung und EDT-Verhalten bleiben erhalten. Das Anzeigeformat wird geklont. `.form` und Java bleiben konsistent. Remote-Interfaces, Transaktionen und Datenbankschema sind unverändert, der Umfang bleibt auf ein Thema begrenzt. Beide Commits weisen Autor und Co-Author aus. Commit-Nachrichten und PR enthalten keine Semikolons.

Eigene Prüfung: vier unveränderte Branch-Tests unter Java 17/JUnit 4.12 im Arbeitsspeicher ausgeführt, zusätzlich Gegenprobe mit alter Float-Logik. Kein Maven-Reactor-Build oder GUI-Lauf. Keine Dateien geändert.

VERDICT: NEEDS-CHANGES
