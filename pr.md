fix(documents): eingegebener Dateiname bleibt beim Ändern der Beteiligten erhalten

## Problem
In „Dokument aus Vorlage“ überschreibt jede Änderung bei den Beteiligten, z.B. das Zuweisen einer Rolle, den Dateinamen, den man eingegeben hat, wieder mit dem aus der Vorlage erzeugten Namen. Wer zuerst umbenennt und dann eine Beteiligung zuweist, verliert den eigenen Namen (#3234).

## Ursache
Jede Änderung in der Tabelle der Beteiligten ruft über `PartiesPanel` `AddDocumentFromTemplateDialog.selectedPartiesUpdated()` auf. Das wertet die Vorlage mit `templateSelection()` neu aus und setzt dabei immer den neu erzeugten Namen in das Feld (`txtFileName.setText()`). Dasselbe passiert beim Wechsel des Briefkopfs.

## Fix
- Der Dialog merkt sich den erzeugten Dateinamen, der zuletzt im Feld angezeigt wurde. Ändern sich nur die Beteiligten oder der Briefkopf, bleibt ein Name stehen, den der Benutzer seitdem geändert hat.
- Das gilt auch bei mehreren Änderungen nacheinander: Solange der eingegebene Name stehen bleibt, bleibt auch der gemerkte Name gleich. Erst eine andere Vorlage oder Dateinamensvorschrift setzt ihn neu.
- Solange der Name nicht geändert wurde, wird er wie bisher neu erzeugt, z.B. wenn die Dateinamensvorschrift den Namen eines Beteiligten enthält.
- Die Auswahl einer anderen Vorlage oder Dateinamensvorschrift erzeugt den Namen weiterhin neu.
- Die Entscheidung und der gemerkte Name stehen zusammen in der kleinen Klasse `AddDocumentFromTemplateDialog.FileNameSuggestion`, damit der Test auch die Abfolge mehrerer Änderungen prüft. Die Oberfläche ist nicht geändert, die .form-Datei bleibt unverändert.

## Verifiziert
- Neuer Test `DocumentFileNameTest` (j-lawyer-client) mit 5 Fällen: erster Name, eingegebener Name bleibt bei einer Änderung der Beteiligten, erzeugter Name wird bei Änderungen der Beteiligten neu erzeugt, eine andere Vorlage ergibt einen neuen Namen, eingegebener Name bleibt bei mehreren Änderungen nacheinander.
- Der letzte Fall spielt genau diese Abfolge durch: erzeugt wird Brief_Mueller, eingegeben wird Brief_Schmidt, dann wird Schmidt und danach Meier gewählt. Der Name bleibt Brief_Schmidt. Danach ergibt eine andere Vorlage wieder den erzeugten Namen, und Änderungen der Beteiligten erzeugen ihn von da an wieder neu. Mit der Logik des vorigen Stands, bei der der gemerkte Name immer weiterwandert, schlägt dieser Fall fehl (Brief_Meier statt Brief_Schmidt).
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches (Stand 575f6d278, gestartet 04.11.2026 08:07 UTC) und Vergleichsbuild von master 05f6b4350 (04.11.2026) mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 319 gesamt, davon 319 ausgeführt und 0 übersprungen, 17 fehlgeschlagen. Auf master: 314 gesamt, 314 ausgeführt, 0 übersprungen, 17 fehlgeschlagen. Neu sind 5 Tests.
- Die fehlgeschlagenen Tests sind genau dieselben wie auf master: FtpTest und SftpTest in j-lawyer-server-common brauchen einen echten FTP- bzw. SFTP-Server mit Zugangsdaten aus Umgebungsvariablen. SmbTest braucht die im Test fest eingetragene SMB-Freigabe smb://j-lawyer-box/j-lawyer-share/.

## Nicht abgedeckt
- Im laufenden Client nicht ausprobiert.
- Das Issue beschreibt außerdem, dass nach dem Umbenennen die zugewiesene Beteiligung verschwindet. Dafür gibt es im Code keine Ursache: Das Feld für den Dateinamen löst außer mit Enter nichts aus. `templateSelection()` klappt allerdings die Spalten der Beteiligtentypen ein, die die Vorlage nicht nutzt (`PartiesPanel.expandParties()`), eine dort gesetzte Auswahl ist dann nicht mehr zu sehen. Das müsste im laufenden Client nachgestellt werden und ist hier nicht geändert.
- „Dokument aus Vorlage“ bei Kontakten (`AddAddressDocumentFromTemplateDialog`) überschreibt den Dateinamen auf dieselbe Weise und ist nicht geändert.

Refs #3234

🤖 Generated with [Claude Code](https://claude.com/claude-code)
