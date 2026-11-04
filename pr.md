fix(documents): eingegebener Dateiname bleibt beim Ändern der Beteiligten erhalten

## Problem
In „Dokument aus Vorlage“ überschreibt jede Änderung bei den Beteiligten, z.B. das Zuweisen einer Rolle, den Dateinamen, den man eingegeben hat, wieder mit dem aus der Vorlage erzeugten Namen. Wer zuerst umbenennt und dann eine Beteiligung zuweist, verliert den eigenen Namen (#3234).

## Ursache
Jede Änderung in der Tabelle der Beteiligten ruft über `PartiesPanel` `AddDocumentFromTemplateDialog.selectedPartiesUpdated()` auf. Das wertet die Vorlage mit `templateSelection()` neu aus und setzt dabei immer den neu erzeugten Namen in das Feld (`txtFileName.setText()`). Dasselbe passiert beim Wechsel des Briefkopfs.

## Fix
- Der Dialog merkt sich den zuletzt erzeugten Dateinamen. Ändern sich nur die Beteiligten oder der Briefkopf, bleibt ein Name stehen, den der Benutzer seitdem geändert hat.
- Solange der Name nicht geändert wurde, wird er wie bisher neu erzeugt, z.B. wenn die Dateinamensvorschrift den Namen eines Beteiligten enthält.
- Die Auswahl einer anderen Vorlage oder Dateinamensvorschrift erzeugt den Namen weiterhin neu.
- Die Entscheidung steht in der kleinen statischen Methode `fileNameAfterEvaluation()`. Die Oberfläche ist nicht geändert, die .form-Datei bleibt unverändert.

## Verifiziert
- Neuer Test `DocumentFileNameTest` (j-lawyer-client) mit 4 Fällen: erster Name, eingegebener Name bleibt bei einer Änderung der Beteiligten, erzeugter Name wird bei einer Änderung der Beteiligten neu erzeugt, eine andere Vorlage ergibt einen neuen Namen.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches (Stand f1c580a40, gestartet 04.11.2026 07:55 UTC) und Vergleichsbuild von master 05f6b4350 (04.11.2026) mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 318 gesamt, davon 318 ausgeführt und 0 übersprungen, 17 fehlgeschlagen. Auf master: 314 gesamt, 314 ausgeführt, 0 übersprungen, 17 fehlgeschlagen. Neu sind 4 Tests.
- Die fehlgeschlagenen Tests sind genau dieselben wie auf master: FtpTest und SftpTest in j-lawyer-server-common brauchen einen echten FTP- bzw. SFTP-Server mit Zugangsdaten aus Umgebungsvariablen. SmbTest braucht die im Test fest eingetragene SMB-Freigabe smb://j-lawyer-box/j-lawyer-share/.

## Nicht abgedeckt
- Im laufenden Client nicht ausprobiert.
- Das Issue beschreibt außerdem, dass nach dem Umbenennen die zugewiesene Beteiligung verschwindet. Dafür gibt es im Code keine Ursache: Das Feld für den Dateinamen löst außer mit Enter nichts aus. `templateSelection()` klappt allerdings die Spalten der Beteiligtentypen ein, die die Vorlage nicht nutzt (`PartiesPanel.expandParties()`), eine dort gesetzte Auswahl ist dann nicht mehr zu sehen. Das müsste im laufenden Client nachgestellt werden und ist hier nicht geändert.
- „Dokument aus Vorlage“ bei Kontakten (`AddAddressDocumentFromTemplateDialog`) überschreibt den Dateinamen auf dieselbe Weise und ist nicht geändert.

Refs #3234

🤖 Generated with [Claude Code](https://claude.com/claude-code)
