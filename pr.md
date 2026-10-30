fix(epost): Überschrift "Einschreiben:" über der Auswahl der Einschreiben-Art

## Problem
Im E-POST-Assistenten steht im Schritt "Empfänger und Versandart" unter den Druckoptionen (Vorderseite/Rückseite, farbig/schwarz-weiß, Deckblatt) eine Auswahlliste ohne Beschriftung. Erst beim Aufklappen sieht man, dass es um die Einschreiben-Art geht. Die leere Vorauswahl bedeutet "kein Einschreiben".

## Ursache
Im Formular `EpostLetterSendStep` gibt es zur Auswahl `cmbRegisteredLetter` kein Label.

## Fix
Neues `JLabel` `lblRegisteredLetter` mit dem Text "Einschreiben:" zwischen `jSeparator3` und `cmbRegisteredLetter`. Schrift und Doppelpunkt wie bei den übrigen Feldbeschriftungen des Schritts (z.B. "PLZ:").

`.form` und `.java` sind gleich geändert, so dass der NetBeans GUI Builder weiter funktioniert: Komponente in `SubComponents` von `jPanel1`, im horizontalen und im vertikalen `DimensionLayout`. Der bisherige feste Abstand von 39 px über der Auswahlliste wird zu Standardabstand "separate" (18 px), Label und Standardabstand "related". Der generierte Code entspricht dem, was NetBeans für dieselben Einträge in dieser Datei erzeugt (`addGap(18, 18, 18)`, `addPreferredGap(RELATED)`, Variable alphabetisch einsortiert).

## Verifiziert
- `mvn -pl j-lawyer-client -am clean package` mit Tests: BUILD SUCCESS. Es scheitern nur FtpTest, SftpTest und SmbTest wie auf master (brauchen externe Zugangsdaten). `GroupLayoutCompletenessTest` ist grün, das Label steckt also in beiden Gruppen des Layouts.
- `.form` ist wohlgeformtes XML.
- Headless-Layoutprüfung mit einer wörtlichen Kopie von `initComponents()` (der echte Konstruktor braucht einen Server): Trennlinie endet bei y=266, Label bei y=284, Auswahlliste bei y=305. Die Auswahlliste steht damit an derselben Stelle wie vorher (266 + 39), die Höhe des Panels bleibt gleich.

## Nicht abgedeckt
- Nicht im laufenden Client und nicht im NetBeans GUI Builder geöffnet. Screenshot im Issue nicht einsehbar, die Stelle ergibt sich aus dem Issue-Titel und der einzigen Einschreiben-Auswahl im Assistenten.

Closes #2544

🤖 Generated with [Claude Code](https://claude.com/claude-code)
