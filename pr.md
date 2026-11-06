fix(documents): „ablegen als“ fragt bei vorhandener Datei nach Ersetzen oder neuem Namen, wie „Als PDF zur Akte speichern“

## Problem
Über „PDF und Konvertierung“ → „ablegen als“ wird ein Dokument in ein anderes Format konvertiert und in der Akte abgelegt. Gibt es den Zieldateinamen schon in der Akte oder in deren Papierkorb, bricht die Aktion mit dem Serverfehler „Dokument … existiert bereits in der Akte oder deren Papierkorb - bitte einen anderen Namen wählen!“ ab. Ohne den Papierkorb zu leeren, lässt sich das Dokument auf diesem Weg nicht ablegen. „Als PDF zur Akte speichern“ fragt in derselben Lage dagegen, ob die Datei ersetzt werden soll, oder lässt einen neuen Namen vergeben.

## Ursache
`ArchiveFilePanel.mnuDuplicateDocumentAsActionPerformed()` ruft `addDocument()` direkt auf, ohne vorher mit `doesDocumentExist()` zu prüfen (das auch den Papierkorb der Akte prüft und Groß- und Kleinschreibung nicht unterscheidet) und ohne Rückfrage. `convertDocumentToPdf()` enthält die Rückfrage und das Ersetzen zweimal fast gleich, einmal für die Konvertierung und einmal für das Duplizieren eines vorhandenen PDF. Dort wird das zu ersetzende Dokument mit `equals()` gesucht, bei `Brief.PDF` gegenüber `Brief.pdf` also nicht gefunden, und das Speichern scheitert mit demselben Serverfehler.

## Fix
- Neue kleine Klasse `DocumentTargetName` klärt den Namen: Solange es den Namen in der Akte oder ihrem Papierkorb gibt, wird gefragt, ob die vorhandene Datei ersetzt werden soll. Bei „Nein“ kann ein neuer Name vergeben werden, der wieder geprüft wird. Wird die Rückfrage geschlossen oder kein neuer Name eingegeben, bricht die Aktion ab.
- Beim Ersetzen entfernt `ArchiveFilePanel.removeDocumentToReplace()` die vorhandene Datei endgültig, wie es „Als PDF zur Akte speichern“ bisher schon tut: aus der Akte über den Papierkorb, sonst aus dem Papierkorb der Akte. Das geschieht erst unmittelbar vor dem Speichern der neuen Datei, nach der Konvertierung.
- Alle drei Wege nutzen das: „ablegen als“, „Als PDF zur Akte speichern“ und das Duplizieren eines vorhandenen PDF. Die beiden bisherigen Kopien in `convertDocumentToPdf()` entfallen.
- Das zu ersetzende Dokument wird ohne Rücksicht auf Groß- und Kleinschreibung gesucht, so wie `doesDocumentExist()` prüft. Das gilt jetzt für alle drei Wege.
- Bei „Als PDF zur Akte speichern“ bricht das Schließen der Rückfrage jetzt ab. Bisher öffnete es die Eingabe eines neuen Namens. So verhalten sich alle drei Wege gleich.
- „ablegen als“: Scheitert das Verschieben in den Ordner der Ausgangsdatei, kommt eine Warnung. Die Datei ist trotzdem abgelegt und erscheint in der Liste.
- Im Server: `renameDocument()` benennt die Datei eines Dokuments, das noch unter seinem Namen gespeichert ist (aus der Zeit vor der Speicherung unter der ID), vor dem Ändern des Namens in seine ID um (`migrateDocument()`, wie beim Lesen des Inhalts). Bisher änderte das Umbenennen nur den Namen in der Datenbank, danach war der Inhalt nicht mehr zu finden (Wiederherstellen, Herunterladen, Bereinigen des Papierkorbs). Das betraf auch das gewöhnliche Umbenennen aktiver Dokumente und ist dort ebenso behoben.
- Im Server: Der Suchindex nimmt bei einer Aktualisierung (`ACTION_UPDATE` im `SearchIndexProcessor`) nur noch Dokumente auf, die existieren und nicht im Papierkorb liegen, wie schon bei `ACTION_UPDATE_METADATA`. Aktualisierungen für Dokumente im Papierkorb und verspätete Aktualisierungen für inzwischen gelöschte Dokumente erscheinen also nicht in der Volltextsuche.

## Verifiziert
- Neuer Test `DocumentTargetNameTest` (j-lawyer-client) mit 6 Fällen ohne Server: freier Name ohne Rückfrage, Ersetzen, neuer Name, bis er frei ist, Ersetzen der Datei mit dem neuen Namen, Abbruch durch Schließen der Rückfrage oder ohne neuen Namen, Suche des zu ersetzenden Dokuments ohne Rücksicht auf Groß- und Kleinschreibung.
- Gegenprobe: Mit `equals()` statt `equalsIgnoreCase()` und mit dem bisherigen Verhalten von „Als PDF zur Akte speichern“ beim Schließen der Rückfrage schlagen genau die Fälle für die Groß- und Kleinschreibung und für den Abbruch fehl.
- Neuer Test `RenameDocumentTest` (j-lawyer-server-ejb) mit einem Fall gegen `ArchiveFileService` mit nachgebildeten Fassaden und einer echten Datei: Ein Dokument, dessen Datei noch seinen Namen trägt, wird umbenannt, danach liefert `getDocumentContent()` seinen Inhalt. Mit dem Stand c47d1e66b schlägt er fehl („Dokument Ersetzt_1_Brief.pdf existiert nicht!“). Die Prüfung im Suchindex hat keinen eigenen Test, sie ist dieselbe Bedingung wie im Zweig für Metadaten.
- Die Korrektur in `renameDocument()` lief auch gegen einen echten Server (WildFly 26.1.3 mit der Konfiguration aus `docker/wildfly`, MariaDB 10.11, EAR aus 5a13f3300, die Server-Änderungen dieses PRs sind seitdem unverändert): Eine Datei im Papierkorb, deren Inhalt noch unter ihrem Namen gespeichert war, ist nach dem Umbenennen über ihre ID lesbar. Mit dem EAR ohne diese Änderung ist der Inhalt danach nicht mehr zu finden.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches (Stand 0361eb863, gestartet 06.11.2026 11:35 UTC) und Vergleichsbuild von master 4e721956a (06.11.2026) mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 1212 gesamt, davon 1196 ausgeführt und 16 übersprungen, 17 fehlgeschlagen. Auf master: 1205 gesamt, 1189 ausgeführt, 16 übersprungen, 17 fehlgeschlagen. Neu sind 7 Tests.
- Die fehlgeschlagenen Tests sind genau dieselben wie auf master: FtpTest und SftpTest in j-lawyer-server-common brauchen einen echten FTP- bzw. SFTP-Server mit Zugangsdaten aus Umgebungsvariablen. SmbTest braucht die im Test fest eingetragene SMB-Freigabe smb://j-lawyer-box/j-lawyer-share/.

## Nicht abgedeckt
- Die Menüpunkte wurden nicht in einem laufenden Client bedient. Für sie und `removeDocumentToReplace()` gibt es keinen automatischen Test.
- Beim Ersetzen wird die vorhandene Datei endgültig gelöscht, bevor die neue gespeichert wird, wie bisher bei „Als PDF zur Akte speichern“. Scheitert das Speichern danach, ist die vorhandene Datei weg. Ein Ersetzen ohne endgültiges Löschen bräuchte eine eigene Servermethode, damit es in einem Schritt abläuft.
- Liegt die zu ersetzende Datei im Papierkorb, wird dafür wie bisher der ganze Papierkorb geladen (`getDocumentsBin()`). Eine Abfrage nur für die Akte gibt es in `ArchiveFileServiceRemote` noch nicht.
- Indexeinträge, die schon früher für Dokumente im Papierkorb entstanden sind, werden nicht bereinigt.
- Der erste Punkt des Issues (Rückfrage bei geöffnetem Dokument statt Warten bis es geschlossen ist) ist eine Gestaltungsfrage und nicht Teil dieses PRs.

Refs #2460

🤖 Generated with [Claude Code](https://claude.com/claude-code)
