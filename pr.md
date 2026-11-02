fix(documents): „ablegen als“ fragt bei vorhandener Datei nach Ersetzen oder neuem Namen, auch wenn sie im Papierkorb liegt

## Problem
Über „PDF und Konvertierung“ → „ablegen als“ wird ein Dokument in ein anderes Format konvertiert und in der Akte abgelegt. Gibt es den Zieldateinamen schon in der Akte oder in deren Papierkorb, bricht die Aktion mit dem Serverfehler „Dokument … existiert bereits in der Akte oder deren Papierkorb - bitte einen anderen Namen wählen!“ ab. Ohne den Papierkorb zu leeren, lässt sich das Dokument auf diesem Weg nicht ablegen. „Als PDF zur Akte speichern“ fragt in derselben Lage dagegen, ob die Datei ersetzt werden soll, oder lässt einen neuen Namen vergeben.

## Ursache
`ArchiveFilePanel.mnuDuplicateDocumentAsActionPerformed()` ruft `addDocument()` direkt auf, ohne vorher mit `doesDocumentExist()` zu prüfen (das auch den Papierkorb der Akte prüft und Groß- und Kleinschreibung nicht unterscheidet) und ohne Rückfrage.

## Fix
- Existiert der Name schon, wird wie bei „Als PDF zur Akte speichern“ gefragt, ob die Datei ersetzt werden soll. Bei „Nein“ kann ein neuer Name vergeben werden. Wird die Rückfrage geschlossen, bricht die Aktion ab.
- Das Ersetzen übernimmt die neue Klasse `DocumentReplacement`: Die neue Datei wird zuerst unter einem vorläufigen Namen gespeichert. Erst danach wird die vorhandene Datei gelöscht (aus der Akte samt Papierkorb oder nur aus dem Papierkorb), dann bekommt die neue Datei ihren Namen. Schlägt das Speichern fehl, bleibt die vorhandene Datei unverändert.
- Die vorhandene Datei wird wie beim Server ohne Beachtung der Groß- und Kleinschreibung gefunden, „Brief.PDF“ wird also durch „Brief.pdf“ ersetzt.

## Verifiziert
- Neuer Test `DocumentReplacementTest` (j-lawyer-client) mit einem nachgebildeten Server: Schlägt das Speichern fehl, wird nichts gelöscht. Ersetzen in der Akte und im Papierkorb mit abweichender Groß- und Kleinschreibung in der Reihenfolge Speichern, Löschen, Umbenennen. Ohne vorhandene Datei einfaches Hinzufügen.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches und Vergleichsbuild von master 7b3b3dfa8 mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 272 gesamt, davon 272 ausgeführt und 0 übersprungen, 17 fehlgeschlagen. Auf master: 268 gesamt, 268 ausgeführt, 0 übersprungen, 17 fehlgeschlagen. Neu sind 4 Tests.
- Die fehlgeschlagenen Tests sind genau dieselben wie auf master: FtpTest, SftpTest und SmbTest in j-lawyer-server-common brauchen echte FTP-, SFTP- und SMB-Server mit Zugangsdaten aus Umgebungsvariablen.

## Nicht abgedeckt
- Nicht in einem laufenden Client gegen einen Server ausprobiert. Für den Menüpunkt selbst gibt es keinen automatischen Test.
- Schlägt nach dem Löschen der vorhandenen Datei nur das Umbenennen fehl, bleibt die neue Datei unter ihrem vorläufigen Namen „Ersetzen_…“ in der Akte.
- „Als PDF zur Akte speichern“ (`convertDocumentToPdf()`) löscht beim Ersetzen weiterhin zuerst und unterscheidet Groß- und Kleinschreibung. Das ist hier nicht geändert, um den PR auf „ablegen als“ zu beschränken.
- Der erste Punkt des Issues (Rückfrage bei geöffnetem Dokument statt Warten bis es geschlossen ist) ist eine Gestaltungsfrage und nicht Teil dieses PRs.

Refs #2460

🤖 Generated with [Claude Code](https://claude.com/claude-code)
