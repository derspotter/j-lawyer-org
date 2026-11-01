fix(finanzen): Belegübersicht zeigt nach dem Bearbeiten eines Belegs wieder den bereits bezahlten Betrag

## Problem
Öffnet man in der Belegübersicht einer Akte einen Beleg und schließt den Dialog mit „Speichern“, zeigt die Zeile des Belegs danach „bezahlt 0,00“ und als offen den vollen Betrag, auch wenn schon Zahlungen gebucht sind. Erst nach erneutem Öffnen der Akte stimmen die Werte wieder.

## Ursache
`InvoiceEntryPanel.cmdOpenActionPerformed()` ruft nach dem Dialog `setPaidTotal(gesamt, BigDecimal.valueOf(Float.MIN_VALUE), währung)` auf. Der Platzhalter bedeutet „bisher bezahlten Betrag behalten, nur den Gesamtbetrag aktualisieren“. `setPaidTotal()` prüft ihn aber mit `!=`, also auf dasselbe Objekt. Da `BigDecimal.valueOf()` jedes Mal ein neues Objekt liefert, schlägt die Prüfung immer an und der Platzhalter (1,4E-45, angezeigt als 0,00) wird als bezahlter Betrag übernommen. Mit `float` funktionierte der Vergleich. Der Fehler kam mit der Umstellung auf `BigDecimal` in d1498e4b0 (#2681).

## Fix
Der Platzhalter wird per `compareTo()` erkannt, also nach Wert statt nach Objekt.

## Verifiziert
- Neuer Test `InvoiceEntryPanelPaidTotalTest`: Nach dem Aufruf mit Platzhalter bleibt der bezahlte Betrag erhalten und der offene Betrag passt zum neuen Gesamtbetrag. Ohne die Änderung schlägt der Test fehl („expected 40.00 EUR but was 0.00 EUR“).
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Alle Module des Reactors bauen. Tests: 270 ausgeführt (268 auf master plus 2 neue), 17 fehlgeschlagen. Fehlgeschlagen sind nur FtpTest, SftpTest und SmbTest in j-lawyer-server-common (sie brauchen echte FTP-, SFTP- und SMB-Server). Das sind dieselben Klassen und Testfälle wie auf master 7b3b3dfa8 mit demselben Setup.

## Nicht abgedeckt
- Kein manueller Test gegen einen laufenden Server.

Refs #2278

🤖 Generated with [Claude Code](https://claude.com/claude-code)
