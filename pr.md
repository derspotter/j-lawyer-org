fix(documents): „ablegen als“ fragt bei vorhandener Datei nach Ersetzen oder neuem Namen, auch wenn sie im Papierkorb liegt

## Problem
Über „PDF und Konvertierung“ → „ablegen als“ wird ein Dokument in ein anderes Format konvertiert und in der Akte abgelegt. Gibt es den Zieldateinamen schon in der Akte oder in deren Papierkorb, bricht die Aktion mit dem Serverfehler „Dokument … existiert bereits in der Akte oder deren Papierkorb - bitte einen anderen Namen wählen!“ ab. Ohne den Papierkorb zu leeren, lässt sich das Dokument auf diesem Weg nicht ablegen. „Als PDF zur Akte speichern“ fragt in derselben Lage dagegen, ob die Datei ersetzt werden soll, oder lässt einen neuen Namen vergeben.

## Ursache
`ArchiveFilePanel.mnuDuplicateDocumentAsActionPerformed()` ruft `addDocument()` direkt auf, ohne vorher mit `doesDocumentExist()` zu prüfen (das auch den Papierkorb der Akte prüft) und ohne die Rückfrage, die `convertDocumentToPdf()` für „Als PDF zur Akte speichern“ hat.

## Fix
„ablegen als“ übernimmt dieselbe Behandlung wie `convertDocumentToPdf()`: Existiert der Name schon, wird gefragt, ob die Datei ersetzt werden soll. Bei „Nein“ kann ein neuer Name vergeben werden, Abbrechen beendet die Aktion. Beim Ersetzen wird die vorhandene Datei erst nach erfolgreicher Konvertierung entfernt, aus der Akte samt Papierkorb oder nur aus dem Papierkorb.

## Verifiziert
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Alle Module des Reactors bauen. Tests: 268 ausgeführt, 17 fehlgeschlagen. Fehlgeschlagen sind nur FtpTest, SftpTest und SmbTest in j-lawyer-server-common (sie brauchen echte FTP-, SFTP- und SMB-Server). Das sind dieselben Klassen und Testfälle wie auf master 7b3b3dfa8 mit demselben Setup.
- Kein Unit-Test: Swing-Aktion mit Serveraufrufen. Der übernommene Ablauf ist derselbe wie in `convertDocumentToPdf()`.

## Nicht abgedeckt
- Der erste Punkt des Issues (Rückfrage bei geöffnetem Dokument statt Warten bis es geschlossen ist) ist eine Gestaltungsfrage und nicht Teil dieses PRs.
- Wie in `convertDocumentToPdf()` sucht das Ersetzen die vorhandene Datei mit exakt gleichem Namen. Unterscheidet sie sich nur in Groß- und Kleinschreibung, meldet der Server weiter den Fehler.
- Kein manueller Test gegen einen laufenden Server.

Refs #2460

🤖 Generated with [Claude Code](https://claude.com/claude-code)
