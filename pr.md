fix(finanzen): Belegübersicht nach Buchungen im Aktenkonto aktualisieren

## Problem
Wird im Aktenkonto eine Buchung zu einem Beleg erfasst, geändert, dupliziert oder gelöscht, zeigt die Belegübersicht weiter die alten Werte für „bezahlt“ und „offen“. Erst nach Schließen und erneutem Öffnen der Akte stimmen sie.

## Ursache
Die Beträge je Beleg werden nur in `ArchiveFilePanel.loadAccountEntries()` beim Laden des Aktenkontos berechnet. Die Aktionen Hinzufügen, Bearbeiten, Duplizieren und Löschen aktualisieren nur die Tabelle und die Summen des Aktenkontos (`updateAccountTotals()`), nicht aber die Belege.

## Fix
Die Berechnung aus `loadAccountEntries()` ist in die Methode `updateInvoicePaidTotals()` verschoben. Sie summiert die Buchungen je Beleg aus der Aktenkontotabelle und setzt „bezahlt“ und „offen“ aller Belege. Sie wird beim Laden und nach jeder der vier Aktionen aufgerufen. Die Berechnung selbst ist unverändert (`calculateEarningsTotal()` je Buchung, Brutto- oder Nettobetrag je nach Beleg wie bisher).

## Verifiziert
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Alle Module des Reactors bauen. Tests: 268 ausgeführt, 17 fehlgeschlagen. Fehlgeschlagen sind nur FtpTest, SftpTest und SmbTest in j-lawyer-server-common (sie brauchen echte FTP-, SFTP- und SMB-Server). Das sind dieselben Klassen und Testfälle wie auf master 7b3b3dfa8 mit demselben Setup.
- Kein Unit-Test: Die Berechnung liest Swing-Tabelle und Belegpanels der Akte.

## Nicht abgedeckt
- Nach dem Bearbeiten eines Belegs über „Öffnen“ zeigt die Zeile in `InvoiceEntryPanel.cmdOpenActionPerformed()` „bezahlt 0,00“, weil `setPaidTotal()` den Platzhalter `BigDecimal.valueOf(Float.MIN_VALUE)` per `!=` vergleicht. Das ist ein eigener kleiner Fehler und nicht Teil dieses PRs.
- Kein manueller Test gegen einen laufenden Server.

Closes #2278

🤖 Generated with [Claude Code](https://claude.com/claude-code)
