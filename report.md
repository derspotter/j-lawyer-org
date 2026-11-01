# Report #2111: Kontakt-Editor rechts am Fensterrand

- Branch: `fix/2111-address-header-right-gap` (Basis: upstream/master 7b3b3dfa8)
- Commit: `2a43665867cdce63bc98a302c0c94d666c69248e`
- Autor: derspotter <jayjag@posteo.de>, Co-Authored-By Claude
- Status: fertig, bereit für Codex-Review und lokalen Liberica-Build

## Dateien
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/addresses/AddressPanel.form` +1 -0
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/editors/addresses/AddressPanel.java` +2 -1

Gesamt: 2 files changed, 3 insertions(+), 1 deletion(-).

## Build und Tests
Befehl: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`

- Baseline (upstream/master): Build läuft durch (Testfehler ignoriert). Fehlschlagend nur FtpTest, SftpTest, SmbTest (17 von 104 in j-lawyer-server-common). Client: 81 Tests, 0 Fehler.
- Mit Fix: gleich. Client: 81 Tests, 0 Fehler. `GroupLayoutCompletenessTest` grün.
- JDK: Ubuntu OpenJDK 17.0.20 plus OpenJFX 17.0.9 per jlink (BellSoft-Hosts im Proxy gesperrt), `--release` durch source/target 17 ersetzt.
- Scratch-Layoutprüfung (`org.jdesktop.layout.GroupLayout`, Platzhalter-Komponenten, 800 px breit, headless): ohne Fix links 12 px, rechts 0 px. Mit Fix links und rechts 12 px, auch für die Reiter.

## Merge-Prüfung
`git merge-tree --write-tree` gegen `origin/fix/1283-new-contact-after-tag-save` (fd36c46) und `origin/fix/3511-double-right-click` (PR #3693): beide Exit 0, keine Konflikte. Die anderen Branches ändern andere Stellen in AddressPanel.java (isContactSaved bei setAddressDTO bzw. Kontextmenü-Code), nicht initComponents.

## Nicht verifiziert
- Kein laufender Client, kein Windows mit 105 Prozent, kein NetBeans GUI Builder.
- Screenshot im Issue nicht einsehbar.

## Offene Fragen
- Die Reiter (`jTabbedPane1`) bekommen rechts ebenfalls 12 px Abstand. Das ist gewollt (symmetrisch wie in ArchiveFilePanel), sieht aber etwas anders aus als bisher.
