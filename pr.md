fix(epost): Überschrift "Einschreiben:" über der Auswahl der Einschreiben-Art

## Problem
Im E-POST-Assistenten steht im Schritt "Empfänger und Versandart" unter den Druckoptionen (Vorderseite/Rückseite, farbig/schwarz-weiß, Deckblatt) eine Auswahlliste ohne Beschriftung. Erst beim Aufklappen sieht man, dass es um die Einschreiben-Art geht. Die leere Vorauswahl bedeutet "kein Einschreiben".

## Ursache
Im Formular `EpostLetterSendStep` gibt es zur Auswahl `cmbRegisteredLetter` kein Label.

## Fix
Neues `JLabel` `lblRegisteredLetter` mit dem Text "Einschreiben:" zwischen `jSeparator3` und `cmbRegisteredLetter`. Schrift und Doppelpunkt wie bei den übrigen Feldbeschriftungen des Schritts (z.B. "PLZ:").

`.form` und `.java` sind gleich geändert, so dass der NetBeans GUI Builder weiter funktioniert: Komponente in `SubComponents` von `jPanel1`, im horizontalen und im vertikalen `DimensionLayout`. Der bisherige feste Abstand von 39 px über der Auswahlliste wird zu Standardabstand "separate", Label und Standardabstand "related". Der generierte Code entspricht dem, was NetBeans für dieselben Einträge in dieser Datei erzeugt (`addGap(18, 18, 18)`, `addPreferredGap(RELATED)`, Variable alphabetisch einsortiert).

## Verifiziert
- `GroupLayoutCompletenessTest` ist grün, das Label steckt also in beiden Gruppen des Layouts. `.form` ist wohlgeformtes XML.
- Headless-Layouttest mit einer wörtlichen Kopie von `initComponents()` (der echte Konstruktor braucht einen Server), Standardschrift und Standard-Look-and-Feel des headless JDK: Trennlinie endet bei y=266, Label bei y=284, Auswahlliste bei y=305. In diesem Test steht die Auswahlliste an derselben Stelle wie vorher (266 + 39) und die Höhe des Panels ist gleich. Mit anderer Schrift oder anderem Look and Feel (z.B. FlatLaf im Client) kann sich die Position um einige Pixel verschieben, weil Labelhöhe und Standardabstände davon abhängen.
- Build: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- JDK: Ubuntu OpenJDK 17.0.20 und OpenJFX 17.0.9 aus Maven Central, per jlink zu einem JDK zusammengefügt, weil die BellSoft-Hosts im Cloud-Proxy gesperrt waren. Mit diesem JDK sieht `--release 17` die JavaFX-Module nicht, deshalb `source`/`target` 17 statt `release`.
- Ergebnis: Der Build läuft durch, weil Testfehler mit `-Dmaven.test.failure.ignore=true` ignoriert werden. Fehlschlagende Testklassen sind nur FtpTest, SftpTest und SmbTest in j-lawyer-server-common (sie brauchen FTP-, SFTP- und SMB-Server), genau wie auf master. Client: 81 Tests, 0 Fehler (master: 81).

## Nicht abgedeckt
- Nicht im laufenden Client und nicht im NetBeans GUI Builder geöffnet. Screenshot im Issue nicht einsehbar, die Stelle ergibt sich aus dem Issue-Titel und der einzigen Einschreiben-Auswahl im Assistenten.

Closes #2544

🤖 Generated with [Claude Code](https://claude.com/claude-code)
