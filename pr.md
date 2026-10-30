fix(calendar): Datumsauswahl wählt bei Alt+Tab nicht mehr ungefragt den markierten Tag

## Problem
Ist die Datumsauswahl mit den drei Monaten (`MultiCalDialog`) offen und wechselt man per Alt+Tab das Fenster, erscheint an Wochenenden und Feiertagen sofort die Rückfrage, ob der Tag trotzdem verwendet werden soll. `c1PropertyChange` usw. werden dabei ohne Klick aufgerufen.

## Ursache
`MultiCalDialog` reagiert auf die Eigenschaft `day` der drei `JDayChooser` (JCalendar 1.3.2) und setzt dafür `setAlwaysFireDayProperty(true)`. `JDayChooser` ist selbst KeyListener aller Tages-Buttons. In `keyPressed()` verschieben nur die Pfeiltasten den Tag. Für jede andere Taste ist der Versatz 0, und es wird trotzdem `setDay(getDay())` aufgerufen. Mit `alwaysFireDayProperty` feuert das immer ein `day`-Event (per Bytecode von `keyPressed` und `setDay` in jcalendar-1.3.2 nachgeprüft).

Das Drücken von Alt für Alt+Tab erreicht den fokussierten Tages-Button. Dadurch wird der markierte Tag (beim Öffnen in der Regel heute) "ausgewählt". Ist heute Wochenende oder Feiertag, kommt die Rückfrage. An einem Werktag schließt sich der Dialog stattdessen ohne Rückfrage und übernimmt dieses Datum. Dasselbe passiert mit jeder anderen Taste, z.B. Escape oder Shift.

## Fix
`MultiCalDialog.selectDate()` prüft zuerst das gerade verarbeitete AWT-Event (`EventQueue.getCurrentEvent()`). Stammt die Änderung von einem Tastendruck, wählen nur noch die Tasten einen Tag aus, die dafür gedacht sind: Pfeiltasten, Enter und Leertaste. Andere Tasten (Alt, Cmd, Shift, Tab, Escape, Buchstaben usw.) werden ignoriert. Mausklicks und alle anderen Auslöser bleiben unverändert. Die Monats- und Jahresnavigation ist nicht betroffen, `setCalendar()` feuert kein `day`-Event.

Keine Änderung an der `.form`-Datei, nur Code außerhalb der generierten Bereiche.

## Verifiziert
- Neuer `MultiCalDialogTest` mit 4 Tests: ein echter `JDayChooser` feuert bei `keyPressed(VK_ALT)` ein `day`-Event (die Ursache), Alt/Meta/Tab/Escape/Shift gelten nicht als Auswahl, Pfeiltasten/Enter/Leertaste weiterhin schon, ein Mausklick und `null` (kein Event in Verarbeitung) ebenfalls.
- `mvn -pl j-lawyer-client -am clean package` mit Tests: BUILD SUCCESS. Es scheitern nur FtpTest, SftpTest und SmbTest wie auf master (brauchen externe Zugangsdaten).

## Nicht abgedeckt
- Nicht im laufenden Client mit echtem Alt+Tab ausprobiert (keine Desktop-Umgebung). Dass beim Fensterwechsel genau das Alt-Event (bzw. Cmd unter macOS) beim Tages-Button ankommt, ist aus dem Verhalten im Issue und dem Code von `JDayChooser` abgeleitet, nicht unter Windows, macOS oder Linux live beobachtet.
- Verhaltensänderung am Rande: Escape, Buchstaben und andere Tasten wählten bisher den markierten Tag und schlossen den Dialog. Jetzt passiert dabei nichts. Pfeiltasten, Enter und Leertaste verhalten sich wie vorher.

Closes #3165

🤖 Generated with [Claude Code](https://claude.com/claude-code)
