fix(finanzen): nach Buchung über den grünen Haken nur den tatsächlich gebuchten Betrag als bezahlt anzeigen

## Problem
Klickt man in der Belegübersicht auf den grünen Haken, öffnet sich der Dialog für die Buchung im Aktenkonto. Wird dort nur eine Teilzahlung gebucht (z.B. bei Rechtsschutzversicherungen), zeigt die Belegübersicht den Beleg trotzdem als vollständig bezahlt. Im Aktenkonto und im Beleg selbst ist die Teilzahlung richtig gebucht. Nach erneutem Öffnen der Akte stimmt auch die Anzeige.

## Ursache
`InvoiceEntryPanel.cmdMarkAsPayedActionPerformed()` setzt nach bestätigter Buchung „bezahlt“ fest auf den Gesamtbetrag des Belegs, unabhängig vom Betrag, der im Buchungsdialog tatsächlich gebucht wurde.

## Fix
Nach der bestätigten Buchung werden die Buchungen zum Beleg neu vom Server gelesen (`getAccountEntriesForInvoice`, wie schon vor dem Dialog für den Vorschlagsbetrag) und deren Summe als „bezahlt“ angezeigt. Die Summe wird wie beim Laden der Akte mit `calculateEarningsTotal()` gebildet.

## Verifiziert
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Alle Module des Reactors bauen. Tests: 268 ausgeführt, 17 fehlgeschlagen. Fehlgeschlagen sind nur FtpTest, SftpTest und SmbTest in j-lawyer-server-common (sie brauchen echte FTP-, SFTP- und SMB-Server). Das sind dieselben Klassen und Testfälle wie auf master 7b3b3dfa8 mit demselben Setup.
- Kein Unit-Test: Die Logik steckt im Swing-Panel mit Serveraufrufen.

## Nicht abgedeckt
- Punkt 1 des Issues (Beleg bleibt „bezahlt“, wenn die Buchung abgebrochen wird) ist laut j-dimension so gewollt und bleibt unverändert.
- Kein manueller Test gegen einen laufenden Server.

Refs #2850

🤖 Generated with [Claude Code](https://claude.com/claude-code)
