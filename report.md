# Report #2544: E-POST Überschrift Einschreiben

- Branch: `fix/2544-epost-einschreiben-label` (Basis: upstream/master 7b3b3dfa8)
- Commit: `d33d0b118f239b208a6f1f7e593555e9aae14cf4`
- Status: fertig, bereit für Codex-Review

## Dateien
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/voip/EpostLetterSendStep.form` +9 -1
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/voip/EpostLetterSendStep.java` +8 -1

Gesamt: 2 files changed, 17 insertions(+), 2 deletions(-).

## Build und Tests
Befehl: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`

- Baseline (upstream/master): BUILD SUCCESS. Fehlschlagende Testklassen: FtpTest, SftpTest, SmbTest. Client: 81 Tests, 0 Fehler.
- Mit Fix: BUILD SUCCESS. Gleiche fehlschlagende Klassen. Client: 81 Tests, 0 Fehler (kein neuer Test, `GroupLayoutCompletenessTest` deckt die Layout-Vollständigkeit ab).
- Zusätzlich (nicht committet): wörtliche Kopie von `initComponents()` und den Feldern in eine Scratch-Klasse, headless gelayoutet. Werte: JCheckBox y=192, JSeparator y=256 h=10, JLabel y=284 h=15 "Einschreiben:", JComboBox y=305 h=24, jPanel1 bevorzugt 175x341. Der echte Konstruktor scheitert ohne Server (UserSettings/ServerSettings), daher die Kopie.

JDK wie bei #2151 beschrieben (Ubuntu OpenJDK 17 plus OpenJFX 17.0.9 per jlink, `--release` durch source/target 17 ersetzt).

## Nicht verifiziert
- Nicht im NetBeans GUI Builder geöffnet. Die .form-Einträge sind nach dem Muster der bestehenden Einträge derselben Datei geschrieben.
- Screenshot im Issue nicht einsehbar (Bild-Host im Proxy gesperrt). `cmbRegisteredLetter` ist die einzige Einschreiben-Auswahl im E-POST-Assistenten (Werte aus `EpostLetter.REGISTEREDLETTER_TYPES` plus leerer Eintrag).

## Offene Fragen
- Text "Einschreiben:" (normal, mit Doppelpunkt wie die Feldbeschriftungen) statt einer fetten Überschrift wie "Empfänger:"/"Absender:". Falls eine echte Überschrift gewünscht ist, nur die Schrift ändern.
- Kein `labelFor` gesetzt, um nah am bestehenden Stil zu bleiben.
- Commit-Autor ist die Git-Identität der Cloud-Session (Claude), mit Co-Authored-By-Zeile.
