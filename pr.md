fix(addresses): "Adressen - neu" öffnet nach Speichern über den Etiketten-Reiter wieder eine leere Adresse

## Problem
Ablauf aus dem Issue: Adressen - neu, Grunddaten eingeben, Reiter Etiketten, ein Etikett anklicken. Die Rückfrage "Bevor Etiketten aktiviert werden können, muss die Adresse gespeichert werden. Jetzt speichern?" mit Ja beantworten. Danach in der Seitenleiste erneut Adressen - neu klicken. Erwartet wird ein leeres Formular, tatsächlich passiert nichts.

## Ursache
`AddressPanel.confirmSave()` legt die Adresse an und zeigt sie danach im `NewAddressPanel` an (`setAddressDTO`). Der Klick auf "neu" landet in `EditorsRegistry.setMainEditorsPaneView()`. Ist der gewählte Editor bereits angezeigt, kehrt die Methode sofort zurück, ohne `reset()` aufzurufen. Beim normalen Speichern fällt das nicht auf, weil `save()` das Formular nach dem Anlegen selbst leert. Nach dem Speichern über den Etiketten-Reiter bleibt die gespeicherte Adresse aber stehen.

## Fix
- `ResetOnDisplayEditor` bekommt die Default-Methode `needsResetOnReselect()` (Standard `false`). Alle anderen Editoren verhalten sich also wie bisher.
- `NewAddressPanel` liefert `true`, sobald die angezeigte Adresse schon auf dem Server gespeichert ist (`AddressPanel.isContactSaved()`, die Adresse hat eine ID).
- `EditorsRegistry.setMainEditorsPaneView()`: Wird der angezeigte Editor erneut gewählt und verlangt er das, fragt die Registry wie beim Verlassen eines Editors "Daten vor dem Verlassen speichern?", wenn er ungespeicherte Daten meldet. Ja speichert und leert danach das Formular. Nein leert es ohne erneutes Speichern, verworfen werden also nur Änderungen nach dem Speichern über den Etiketten-Reiter. Schließen des Dialogs bricht ab. Schlägt das Speichern fehl, bleibt alles stehen. Mit `skipReset` passiert wie bisher nichts.

`NewAddressPanel.isDirty()` meldet bei ausgefüllten Feldern immer ungespeicherte Daten. Nach dem Speichern über den Etiketten-Reiter kommt die Rückfrage deshalb immer. Das ist dieselbe Rückfrage, die schon heute beim Wechsel in ein anderes Modul erscheint.

## Verifiziert
- Neuer `EditorsRegistryReselectTest` mit 4 Tests: erneutes Wählen lässt einen Editor standardmäßig unverändert, setzt ihn zurück wenn er `needsResetOnReselect()` meldet, tut mit `skipReset` nichts und setzt einen unveränderten `SaveableEditor` ohne Rückfrage zurück.
- `mvn -pl j-lawyer-client -am clean package` mit Tests: BUILD SUCCESS. Es scheitern nur FtpTest, SftpTest und SmbTest wie auf master (brauchen externe Zugangsdaten).

## Nicht abgedeckt
- Nicht im laufenden Client gegen einen Server durchgeklickt. `NewAddressPanel` selbst lässt sich ohne Server nicht instanziieren, der Test prüft die Logik in `EditorsRegistry` mit Test-Editoren.
- Der Fall mit ungespeicherten Änderungen (Rückfrage) ist nicht automatisch getestet, weil der Dialog headless nicht angezeigt werden kann.
- Bei neuen Akten (`NewArchiveFilePanel`, `ArchiveFilePanel.confirmSave()`) gibt es vermutlich dasselbe Verhalten. Das ist hier bewusst nicht geändert, ließe sich aber mit derselben Methode nachziehen.

Closes #1283

🤖 Generated with [Claude Code](https://claude.com/claude-code)
