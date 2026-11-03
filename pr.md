fix(documents): „ablegen als“ fragt bei vorhandener Datei nach Ersetzen oder neuem Namen, auch wenn sie im Papierkorb liegt

## Problem
Über „PDF und Konvertierung“ → „ablegen als“ wird ein Dokument in ein anderes Format konvertiert und in der Akte abgelegt. Gibt es den Zieldateinamen schon in der Akte oder in deren Papierkorb, bricht die Aktion mit dem Serverfehler „Dokument … existiert bereits in der Akte oder deren Papierkorb - bitte einen anderen Namen wählen!“ ab. Ohne den Papierkorb zu leeren, lässt sich das Dokument auf diesem Weg nicht ablegen. „Als PDF zur Akte speichern“ fragt in derselben Lage dagegen, ob die Datei ersetzt werden soll, oder lässt einen neuen Namen vergeben.

## Ursache
`ArchiveFilePanel.mnuDuplicateDocumentAsActionPerformed()` ruft `addDocument()` direkt auf, ohne vorher mit `doesDocumentExist()` zu prüfen (das auch den Papierkorb der Akte prüft und Groß- und Kleinschreibung nicht unterscheidet) und ohne Rückfrage.

## Fix
- Existiert der Name schon, wird wie bei „Als PDF zur Akte speichern“ gefragt, ob die Datei ersetzt werden soll. Bei „Nein“ kann ein neuer Name vergeben werden. Wird die Rückfrage geschlossen, bricht die Aktion ab.
- Das Ersetzen übernimmt die neue Klasse `DocumentReplacement`. Die neue Datei wird zuerst unter einem kurzen vorläufigen Namen gespeichert („Ersetzen_“, Zeitstempel und die Endung der Datei). So lassen sich auch Dateien mit dem längsten zulässigen Namen (250 Zeichen) ersetzen. Schlägt das Speichern fehl, bleibt alles unverändert.
- Die Etiketten, die die automatischen Etikettenregeln für den vorläufigen Namen gesetzt haben, werden wieder entfernt. Beim Umbenennen wendet der Server die Regeln für den endgültigen Namen an. Die neue Datei trägt also dieselben Etiketten, als wäre sie direkt unter ihrem Namen abgelegt worden.
- Welche Datei ersetzt wird, wird erst danach beim Server ermittelt (Akte und Papierkorb der Akte, ohne Beachtung der Groß- und Kleinschreibung wie beim Server) und nicht aus der Dokumentliste des Clients, die veraltet sein kann. Unmittelbar vor dem Löschen wird der Name der Datei beim Server noch einmal geprüft. Ersetzt wird also die Datei, die den Namen zu diesem Zeitpunkt trägt. Eine inzwischen umbenannte Datei bleibt erhalten.
- Schlägt ein Schritt vor dem endgültigen Löschen fehl, wird die neue Datei wieder entfernt und eine schon in den Papierkorb verschobene Datei wiederhergestellt. Schlägt erst das Umbenennen fehl, nennt die Fehlermeldung den vorläufigen Namen der neuen Datei und die gelöschte Datei, und die Dokumentliste der Akte zeigt diesen Stand.

## Verifiziert
- Neuer Test `DocumentReplacementTest` (j-lawyer-client) mit 14 Fällen gegen einen nachgebildeten Server, der wie der echte Server einen Namen je Akte nur einmal zulässt (Akte und Papierkorb, ohne Beachtung der Groß- und Kleinschreibung), Namen bis 250 Zeichen speichert und Etikettenregeln beim Speichern und Umbenennen anwendet. Geprüft wird jeweils der Stand auf dem Server: Ersetzen in der Akte und im Papierkorb, veraltete Dokumentliste (Datei A wurde umbenannt, Datei B trägt jetzt den Namen, ersetzt wird B, A bleibt), Umbenennung kurz vor dem Löschen, inzwischen hinzugekommene Datei ohne Ersetzen, Etiketten mit einer Regel „Name ist nicht Brief.pdf“ und einer Regel „Name enthält brief“, Name mit 250 Zeichen, Fehler beim Verschieben in den Papierkorb, beim endgültigen Löschen (Akte und Papierkorb) und beim Umbenennen (Akte und Papierkorb).
- Mit dem Stand vom 02.11. und demselben nachgebildeten Server: Bei veralteter Dokumentliste wird Datei A endgültig gelöscht und danach schlägt das Umbenennen fehl. Die ersetzte Datei trägt das Etikett der Regel „Name ist nicht Brief.pdf“. Ein Name mit 250 Zeichen lässt sich nicht ersetzen („Data too long“).
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches und Vergleichsbuild von master 7b3b3dfa8 mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 282 gesamt, davon 282 ausgeführt und 0 übersprungen, 17 fehlgeschlagen. Auf master: 268 gesamt, 268 ausgeführt, 0 übersprungen, 17 fehlgeschlagen. Neu sind 14 Tests.
- Die fehlgeschlagenen Tests sind genau dieselben wie auf master: FtpTest und SftpTest in j-lawyer-server-common brauchen einen echten FTP- bzw. SFTP-Server mit Zugangsdaten aus Umgebungsvariablen. SmbTest braucht die im Test fest eingetragene SMB-Freigabe smb://j-lawyer-box/j-lawyer-share/.

## Nicht abgedeckt
- Nicht in einem laufenden Client gegen einen Server ausprobiert. Für den Menüpunkt selbst gibt es keinen automatischen Test.
- Das Ersetzen besteht aus mehreren Serveraufrufen und ist nicht atomar. Benennt ein anderer Client die Datei genau zwischen der letzten Prüfung und dem Löschen um, wird sie trotzdem gelöscht. Ganz ausschließen ließe sich das nur mit einer neuen Servermethode, die hier nicht hinzugefügt wird.
- In der Historie der Akte erscheinen das Hinzufügen unter dem vorläufigen Namen und das Umbenennen. Externe Hooks für neu angelegte Dokumente erhalten den vorläufigen Namen, danach folgt das Ereignis für die Umbenennung.
- „Als PDF zur Akte speichern“ (`convertDocumentToPdf()`) löscht beim Ersetzen weiterhin zuerst und unterscheidet Groß- und Kleinschreibung. Das ist hier nicht geändert, um den PR auf „ablegen als“ zu beschränken.
- Der erste Punkt des Issues (Rückfrage bei geöffnetem Dokument statt Warten bis es geschlossen ist) ist eine Gestaltungsfrage und nicht Teil dieses PRs.

Refs #2460

🤖 Generated with [Claude Code](https://claude.com/claude-code)
