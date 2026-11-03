fix(addresses): "Adressen - neu" öffnet nach Speichern über den Etiketten-Reiter wieder eine leere Adresse

## Problem
Ablauf aus dem Issue: Adressen - neu, Grunddaten eingeben, Reiter Etiketten, ein Etikett anklicken. Die Rückfrage "Bevor Etiketten aktiviert werden können, muss die Adresse gespeichert werden. Jetzt speichern?" mit Ja beantworten. Danach in der Seitenleiste erneut Adressen - neu klicken. Erwartet wird ein leeres Formular, tatsächlich passiert nichts.

## Ursache
`AddressPanel.confirmSave()` legt die Adresse an und zeigt sie danach im `NewAddressPanel` an (`setAddressDTO`). Der Klick auf "neu" landet in `EditorsRegistry.setMainEditorsPaneView()`. Ist der gewählte Editor bereits angezeigt, kehrt die Methode sofort zurück, ohne `reset()` aufzurufen. Beim normalen Speichern fällt das nicht auf, weil `save()` das Formular nach dem Anlegen selbst leert. Nach dem Speichern über den Etiketten-Reiter bleibt die gespeicherte Adresse aber stehen.

## Fix
- `ResetOnDisplayEditor` bekommt die Default-Methode `needsResetOnReselect()` (Standard `false`). Alle anderen Editoren verhalten sich also wie bisher.
- `NewAddressPanel` liefert `true`, sobald die angezeigte Adresse schon auf dem Server gespeichert ist (`AddressPanel.isContactSaved()`, die Adresse hat eine ID).
- `EditorsRegistry.setMainEditorsPaneView()`: Wird der angezeigte Editor erneut gewählt und verlangt er das, fragt die Registry wie beim Verlassen eines Editors "Daten vor dem Verlassen speichern?", wenn er ungespeicherte Daten meldet. Ja speichert und leert danach das Formular. Nein leert es ohne erneutes Speichern, verworfen werden also nur Änderungen nach dem Speichern über den Etiketten-Reiter. Schließen des Dialogs bricht ab. Schlägt das Speichern fehl, bleibt alles stehen. Mit `skipReset` passiert wie bisher nichts. Die Rückfrage läuft über das package-private Feld `reselectSaveQuestion`, damit Tests sie beantworten können.
- `NewAddressPanel.isDirty()` vergleicht bei einer gespeicherten Adresse wie `AddressPanel.isDirty()` mit dem gespeicherten Kontakt. Die Rückfrage erscheint also nur, wenn seit dem Speichern etwas geändert wurde, und auch Änderungen an Feldern, die die bisherige Prüfung auf nicht leere Felder nicht kennt (z.B. Notiz), führen zur Rückfrage. Für eine noch nicht gespeicherte Adresse bleibt die bisherige Prüfung.
- `AddressPanel.save()` ersetzt das DTO schon vor dem Serveraufruf durch die aktuellen Eingaben. Schlägt das Speichern fehl, wird jetzt der zuletzt gespeicherte Stand wiederhergestellt. Sonst würde `isDirty()` danach gegen die ungespeicherten Eingaben vergleichen, keine Änderungen melden, und ein erneutes "Adressen - neu" würde sie ohne Rückfrage verwerfen. Das gilt für alle Adress-Editoren, nicht nur für die neue Adresse.
- `NewAddressPanel.populateOptions()` füllt die Auswahllisten nicht neu, solange eine gespeicherte Adresse angezeigt wird. Der Modul-Knopf ruft `populateOptions()` vor der Registry auf, das hätte Land, Anrede, Titel usw. geleert, und "Ja" hätte diese leeren Werte über den gespeicherten Kontakt geschrieben.

## Verifiziert
- Neuer `EditorsRegistryReselectTest` mit 8 Tests: erneutes Wählen lässt einen Editor standardmäßig unverändert, setzt ihn zurück wenn er `needsResetOnReselect()` meldet, tut mit `skipReset` nichts, setzt einen unveränderten `SaveableEditor` ohne Rückfrage zurück. Bei ungespeicherten Änderungen: Ja speichert und setzt zurück, Ja mit fehlschlagendem Speichern lässt alles stehen und fragt beim nächsten Wählen wieder nach, Nein setzt ohne Speichern zurück, Schließen lässt alles stehen.
- Build: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- JDK: Ubuntu OpenJDK 17.0.20 und OpenJFX 17.0.9 aus Maven Central, per jlink zu einem JDK zusammengefügt, weil die BellSoft-Hosts im Cloud-Proxy gesperrt waren. Mit diesem JDK sieht `--release 17` die JavaFX-Module nicht, deshalb `source`/`target` 17 statt `release`.
- Ergebnis: Der Build läuft durch, weil Testfehler mit `-Dmaven.test.failure.ignore=true` ignoriert werden. Fehlschlagende Testklassen sind nur FtpTest, SftpTest und SmbTest in j-lawyer-server-common (sie brauchen FTP-, SFTP- und SMB-Server), genau wie auf master. Ausgeführt 276 Tests, fehlgeschlagen 17, Fehler 0, übersprungen 0. Alle 17 Fehlschläge sind FtpTest (6), SftpTest (6) und SmbTest (5). Client: 89 ausgeführt, 0 fehlgeschlagen, 0 übersprungen (master: 81).

## Nicht abgedeckt
- Nicht im laufenden Client gegen einen Server durchgeklickt, also auch nicht der Ablauf Seitenleiste, Rückfrage, Speichern mit einem echten Kontakt.
- `NewAddressPanel` lässt sich ohne Server nicht instanziieren (der Konstruktor scheitert an `ServerSettings`, ausprobiert). Die beiden Prüfungen in `NewAddressPanel` (`populateOptions()` und `isDirty()` bei gespeicherter Adresse) und das Wiederherstellen des gespeicherten Stands in `AddressPanel.save()` sind deshalb nicht automatisch getestet, nur die Logik in `EditorsRegistry` mit Test-Editoren.
- Bei neuen Akten (`NewArchiveFilePanel`, `ArchiveFilePanel.confirmSave()`) gibt es vermutlich dasselbe Verhalten. Das ist hier bewusst nicht geändert, ließe sich aber mit derselben Methode nachziehen.

Closes #1283

🤖 Generated with [Claude Code](https://claude.com/claude-code)
