# Report #3165: MultiCalDialog fragt bei Alt+Tab nach Wochenende/Feiertag

- Branch: `fix/3165-datepicker-alt-tab` (Basis: upstream/master 7b3b3dfa8)
- Commit (Head): `c9427b9b95376f4f5e25c194f53f0c9d300a9213`
- Status: Code fertig. Offen: Autor auf derspotter umschreiben (Force-Push in der Cloud-Session abgelehnt), siehe Nachtrag.

## Nachtrag 01.11.2026 (Codex-Review, Autor)
- Autor auf `derspotter <jayjag@posteo.de>` umschreiben (amend + `git push --force-with-lease`): in der Cloud-Session vom Auto-Mode-Klassifikator als destruktive Git-Aktion abgelehnt. Nicht umgangen. Der Branch hat deshalb noch den Commit mit Autor Claude.
- Code unverändert, Head weiterhin `c9427b9` (Autor Claude). Die Commit-Nachricht enthält keine Build-Aussage, braucht also keine Korrektur.
- pr.md: `## Verifiziert` nennt jetzt den genauen Befehl inkl. `-Dmaven.test.failure.ignore=true` und Compiler-Overrides, das JDK-Setup und dass der Build nur wegen ignorierter Testfehler durchläuft (einzige fehlschlagende Klassen FtpTest/SftpTest/SmbTest wie auf master).

### Für das Umschreiben (lokal im Orchestrator)
Nur Autor ändern, Nachricht unverändert: `git commit --amend --no-edit --author="derspotter <jayjag@posteo.de>"` und `git push --force-with-lease`.

## Dateien
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/components/MultiCalDialog.java` +32 -0
- `j-lawyer-client/src/test/java/com/jdimension/jlawyer/client/components/MultiCalDialogTest.java` +722 -0

Gesamt: 2 files changed, 754 insertions(+). Davon sind 663 Zeilen der AGPL-Lizenzkopf des neuen Tests, der Test selbst hat 59 Zeilen.

## Ursache (nachgeprüft)
Per `javap -c` auf jcalendar-1.3.2.jar: `JDayChooser.keyPressed` setzt den Versatz nur für Keycodes 37-40 (Pfeiltasten), sonst 0, und ruft dann `setDay(getDay()+offset)` auf. `setDay` feuert mit `alwaysFireDayProperty` immer `firePropertyChange("day", 0, day)`. `focusGained/focusLost` sind leer. `setCalendar` ruft nur `drawDays()`, feuert also nichts (Monatsnavigation unberührt). Der neue Test bestätigt das Feuern bei VK_ALT mit einem echten JDayChooser.

## Build und Tests
Befehl: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`

- Baseline (upstream/master): BUILD SUCCESS. Fehlschlagende Testklassen: FtpTest, SftpTest, SmbTest. Client: 81 Tests, 0 Fehler.
- Mit Fix: BUILD SUCCESS. Gleiche fehlschlagende Klassen. Client: 85 Tests, 0 Fehler (neu: MultiCalDialogTest, 4 Tests).

JDK wie bei #2151 beschrieben (Ubuntu OpenJDK 17 plus OpenJFX 17.0.9 per jlink, `--release` durch source/target 17 ersetzt).

## Nicht verifiziert
- Kein Desktop, also kein echtes Alt+Tab unter Windows, macOS oder Linux. Annahme: der Fensterwechsel liefert zuerst ein KEY_PRESSED für Alt (bzw. Meta unter macOS) an den fokussierten Tages-Button. Das passt zum Issue (Rückfrage erscheint), ist aber nicht live beobachtet.
- Randfall: Hält der KeyboardFocusManager Tastenereignisse während eines Fokuswechsels zurück (Type-ahead) und stellt sie später zu, ist `EventQueue.getCurrentEvent()` dabei unter Umständen ein FocusEvent statt des KeyEvents. Dann würde die Taste weiter als Auswahl zählen. Für das Issue-Szenario (Dialog offen, später Alt+Tab) irrelevant.

## Offene Fragen
- Whitelist statt Blacklist: Ich lasse nur Pfeiltasten, Enter und Leertaste als Auswahl zu. Damit wählen Escape, Buchstaben usw. nicht mehr heimlich heute. Engere Alternative wäre, nur Modifier-Tasten (Alt, AltGr, Ctrl, Shift, Meta, Windows) zu ignorieren. Die Whitelist ist robuster, ändert aber etwas mehr Verhalten.
- Enter bleibt erlaubt, weil es bisher den markierten Tag gewählt hat und jemand das nutzen könnte (Dialog öffnen, Enter = heute).
