fix(finanzen): Belegübersicht nach Buchungen im Aktenkonto aktualisieren

## Problem
Wird im Aktenkonto eine Buchung zu einem Beleg erfasst, geändert, dupliziert oder gelöscht, zeigt die Belegübersicht weiter die alten Werte für „bezahlt“ und „offen“. Erst nach Schließen und erneutem Öffnen der Akte stimmen sie.

## Ursache
Die Beträge je Beleg werden nur in `ArchiveFilePanel.loadAccountEntries()` beim Laden des Aktenkontos berechnet. Die Aktionen Hinzufügen, Bearbeiten, Duplizieren und Löschen aktualisieren nur die Tabelle und die Summen des Aktenkontos (`updateAccountTotals()`), nicht aber die Belege.

## Fix
- Die Berechnung aus `loadAccountEntries()` ist in die Methode `updateInvoicePaidTotals()` verschoben. Sie summiert die Buchungen je Beleg aus der Aktenkontotabelle und setzt „bezahlt“ und „offen“ aller Belege. Sie wird beim Laden und nach jeder der vier Aktionen aufgerufen. Die Berechnung selbst ist unverändert (`calculateEarningsTotal()` je Buchung, Brutto- oder Nettobetrag je nach Beleg wie bisher).
- Beim Bearbeiten einer Buchung bekommt der Dialog eine Kopie. Bisher schrieb der Dialog die Eingaben direkt in das Objekt der Tabelle, schon bevor gespeichert wurde. Schlug das Speichern fehl, standen in der Tabelle und damit in den Summen ungespeicherte Werte. Jetzt wird die Zeile erst nach erfolgreichem Speichern ersetzt.

## Verifiziert
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches und Vergleichsbuild von master 7b3b3dfa8 mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 268 gesamt, davon 268 ausgeführt und 0 übersprungen, 17 fehlgeschlagen. Auf master: 268 gesamt, 268 ausgeführt, 0 übersprungen, 17 fehlgeschlagen.
- Die fehlgeschlagenen Tests sind genau dieselben wie auf master: FtpTest, SftpTest und SmbTest in j-lawyer-server-common brauchen echte FTP-, SFTP- und SMB-Server mit Zugangsdaten aus Umgebungsvariablen.
- Kein Unit-Test: Berechnung und Bearbeitung lesen die Swing-Tabelle und die Belegpanels der Akte.

## Nicht abgedeckt
- Nicht in einem laufenden Client gegen einen Server ausprobiert.
- Die Anzeige „bezahlt 0,00“ nach dem Bearbeiten eines Belegs über „Öffnen“ ist ein eigener Fehler in `InvoiceEntryPanel.setPaidTotal()` und liegt im Branch `fix/belege-bezahlt-nach-bearbeiten`.

Closes #2278

🤖 Generated with [Claude Code](https://claude.com/claude-code)
