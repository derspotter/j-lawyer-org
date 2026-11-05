fix(backup): Fehler beim Einspielen der Datenbanksicherung erkennen und wahlweise fortsetzen

## Problem
Schlagen beim Wiederherstellen einer Datensicherung Anweisungen aus der Datenbanksicherung fehl, meldet der Backupmanager mit dem MariaDB-Client trotzdem eine erfolgreiche Wiederherstellung. Mit dem MySQL-Client kommt nur „Datenbankwiederherstellung fehlgeschlagen!“ ohne den Fehler. Die Datenbank ist in beiden Fällen unvollständig. Aufgefallen bei #2592: Beim Einspielen einer MySQL-8-Sicherung in MariaDB fehlen alle Tabellen, die Wiederherstellung gilt aber als erfolgreich.

Die Datenbank ist der letzte Schritt, die Dateien sind dann schon wiederhergestellt. Ein reiner Abbruch hinterlässt einen Mischzustand, und damit die Wiederherstellung durchläuft, müsste man das ZIP mit der Datenbanksicherung reparieren.

## Ursache
`RestoreExecutor.restoreDatabase()` spielt die Sicherung mit einem Aufruf von `mysql -e` ein, der die Datenbank löscht, neu anlegt und dann `source …/jlawyerdb-dump.sql` ausführt. Geprüft wird nur der Exit-Code. Bei einem Fehler in einer per `source` eingelesenen Anweisung verhalten sich die Clients unterschiedlich (geprüft mit MariaDB 10.11.14 und MySQL 8.0.46):
- Der MariaDB-Client macht auch ohne `--force` bis zum Ende der Datei weiter, gibt die Fehler auf stderr aus und endet mit Exit-Code 0.
- Der MySQL-Client hört bei der ersten fehlerhaften Anweisung auf und endet mit Exit-Code 1.
- Fehlt die Datei, melden beide nur `ERROR at line 1: Failed to open file …`, mit `--force` endet der Aufruf trotzdem mit Exit-Code 0.

## Fix
- Die Datenbank wird in zwei Aufrufen eingespielt. Der erste löscht die Datenbank und legt sie neu an. Jeder Fehler dabei bricht die Wiederherstellung ab, auch mit `--ignore-db-errors`. Der zweite bekommt die Datenbanksicherung als Eingabe (`mysql --skip-force … <Datenbank> < jlawyerdb-dump.sql`). So hören der MySQL- und der MariaDB-Client beide bei der ersten fehlerhaften Anweisung auf, die Anweisungen danach laufen nicht mehr. `--skip-force` sorgt dafür, dass das auch gilt, wenn `force` in der Konfiguration des Clients steht (z.B. `[mysql]` in my.cnf).
- Fehlt die ausgepackte Datenbanksicherung, bricht die Wiederherstellung ab, bevor die Datenbank gelöscht wird.
- Die Ausgabe des Clients wird angezeigt, stdout und stderr zusammen gelesen. Weil beide zusammen gelesen werden, kann der Client nicht mehr an einer vollen stderr-Pipe hängen bleiben. Der MariaDB-Client schreibt jede fehlerhafte Anweisung vollständig auf stderr.
- Beim ersten Fehler in einer Anweisung der Sicherung zeigt die Wiederherstellung den Fehler und den Pfad der ausgepackten Sicherung und fragt: „Abbrechen“ oder „Fehler ignorieren und fortsetzen“.
  - Oberfläche: Dialog in `BackupMgrController`. Schließen des Dialogs bricht ab. Wird der Backupmanager geschlossen, während die Wiederherstellung auf die Antwort wartet, gilt das als Abbrechen, statt dass der Prozess ewig weiterläuft.
  - Konsole: Rückfrage „Fehler ignorieren und fortsetzen J/N“ wie bei der Bestätigung vor der Wiederherstellung. Mit dem neuen Parameter `--ignore-db-errors` geht es ohne Rückfrage weiter, für unbeaufsichtigte Läufe. Endet die Eingabe ohne Antwort, wird abgebrochen wie bisher.
- Abbrechen: Die Datenbank bleibt so, wie sie bis vor die fehlerhafte Anweisung eingespielt ist. Die Wiederherstellung schlägt fehl mit „Datenbankwiederherstellung fehlgeschlagen: …“, dem Fehler und dem Pfad der ausgepackten Sicherung.
- Fortsetzen: Beide Aufrufe laufen noch einmal, die Datenbank wird also wieder gelöscht und neu angelegt, und die Sicherung wird mit `--force` bis zum Ende eingespielt. Keine Anweisung wird doppelt angewendet.
- Am Ende steht dann „Wiederherstellung mit Fehlern abgeschlossen“ mit allen Fehlern und dem Pfad der ausgepackten Sicherung statt „erfolgreich“. Die Zeilennummern in den Fehlern beziehen sich auf diese Datei. Die Oberfläche zeigt die Liste in einem Dialog. Die Konsole gibt sie aus und endet mit Exit-Code 2 (bei Erfolg weiter 0, bei Fehlschlag 1).
- Nur Fehler, die der Server zu einer Anweisung der Sicherung meldet, werden übersprungen. Alle anderen Fehler brechen immer ab, auch nach dem Fortsetzen und mit `--ignore-db-errors`. Dazu gehören eine abgelehnte Anmeldung, eine unbekannte Datenbank und Fehler des Clients selbst, also ohne Fehlernummer oder mit 2000 bis 2999 wie eine abgebrochene Verbindung.
- Die Entscheidung steckt in der neuen Klasse `DatabaseImport` und ist dort getestet. Dialog und Konsolenabfrage zeigen nur die fertige Meldung und geben die Antwort zurück. Das Warten auf die Antwort in der Oberfläche steckt in der kleinen Klasse `UiQuestion`, `BackupManager.stop()` beantwortet eine offene Frage mit Abbrechen.

## Verifiziert
- Neue Tests in j-lawyer-backupmgr, zusammen 21:
  - `DatabaseImportTest` mit 15 Fällen, darunter: Fehlerzeilen, Abbrechen, Fortsetzen, `--ignore-db-errors`, Fehler des Clients ohne Fehlernummer auch mit `--force`, fehlende Sicherung ohne Löschen der Datenbank, Fehler beim Löschen oder Anlegen der Datenbank auch mit `--force`, Abschlussmeldung.
  - In einem dieser Fälle steht ein Shell-Skript für den Client. Es hört wie der echte Client bei der ersten fehlerhaften Zeile auf, außer mit `--force`. Zwei Fälle prüfen, dass beim Rückfragen nur die Zeilen vor dem Fehler eingespielt sind, auch wenn der Client `force` aus seiner Konfiguration mitbringt, und dass der zweite Lauf mit einer leeren Datenbank beginnt.
  - `UiQuestionTest` mit 4 Fällen: Antwort, fehlschlagender Dialog, Rückfrage nach dem Beenden, Beenden während einer offenen Rückfrage.
  - `BackupManagerLauncherTest` mit 2 Fällen: Parameter `--ignore-db-errors`, Rückfrage auf der Konsole mit j, n und ohne Eingabe.
- Gegenprobe: Mit dem alten Verhalten an genau diesen Stellen (Fehler des Clients durchgelassen, keine Prüfung auf eine fehlende Sicherung, Fehler beim Anlegen mit `--force` übergangen, offene Rückfrage beim Beenden nicht beantwortet) schlagen genau die 4 zugehörigen Tests fehl.
- Ende-zu-Ende mit dem gebauten Backupmanager auf der Konsole (`-console`, Antworten über stdin). Die Sicherung enthält eine Tabelle mit unbekannter Kollation zwischen zwei intakten Tabellen. Geprüft mit dem MySQL-Client 8.0.46 gegen MySQL 8.0.46 und dem MariaDB-Client 10.11.14 gegen MariaDB 10.11.14:
  - Während die Rückfrage offen ist, enthält die Datenbank bei beiden Clients nur die Tabelle vor dem Fehler, auch mit `force` in `~/.my.cnf`. Vorher (Stand f7aa51e4b) hatte der MariaDB-Client zu diesem Zeitpunkt schon die ganze Sicherung eingespielt.
  - Abbrechen endet mit Exit-Code 1, die Datenbank enthält nur die Tabelle vor dem Fehler. Eine fehlende Antwort bricht ebenso ab.
  - Fortsetzen und `--ignore-db-errors` enden mit Exit-Code 2 und listen beide Fehler auf. Beide intakten Tabellen sind vollständig und ohne doppelte Zeilen.
  - Fehlt die Datenbanksicherung im Archiv, endet die Wiederherstellung mit Exit-Code 1, die vorhandene Datenbank bleibt unverändert. Vorher meldete sie mit `--ignore-db-errors` Erfolg bei leerer Datenbank.
  - Ein Datenbankbenutzer ohne Recht zum Löschen und Anlegen der Datenbank führt zu Exit-Code 1 mit dem Fehler, die vorhandene Datenbank bleibt unverändert. Vorher hieß es mit `--ignore-db-errors` „mit Fehlern abgeschlossen“.
  - Fehlerfreie Sicherung: „Backup erfolgreich wiederhergestellt.“ mit Exit-Code 0.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches (Stand 709a86f00, gestartet 05.11.2026 09:09 UTC) und Vergleichsbuild von master d34cd0531 (05.11.2026) mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-backupmgr -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 21 gesamt, davon 21 ausgeführt und 0 übersprungen, 0 fehlgeschlagen. Auf master: 0 gesamt, 0 ausgeführt, 0 übersprungen, 0 fehlgeschlagen. Neu sind 21 Tests.
- Keine fehlgeschlagenen Tests, wie auf master.
- Zusammen mit #3767: Beim Zusammenführen gibt es einen Konflikt in `RestoreExecutor.restoreDatabase()`, weil beide PRs den Aufruf von `mysql -e` ändern. Die Auflösung steht in report.md. Der so zusammengeführte Stand baut, alle 42 Tests der beiden PRs laufen durch, und die Wiederherstellung mit Rückfrage und Fortsetzen funktioniert.

## Nicht abgedeckt
- Die Oberfläche ist nicht ausprobiert, in dieser Umgebung gibt es keine Anzeige. Rückfrage-Dialog, Abschlussdialog und das Schließen während einer Rückfrage sind nicht im laufenden Backupmanager getestet, nur `UiQuestion` per Unit-Test.
- Windows, macOS und verschlüsselte Sicherungen sind nicht getestet.
- Zusammen mit #3767: #3767 packt die Datenbanksicherung in ein eigenes Arbeitsverzeichnis aus und löscht es am Ende der Wiederherstellung. Die Meldungen nennen dann eine Datei, die es nicht mehr gibt. Wer von #3758 und #3767 als zweites gemergt wird, muss das Arbeitsverzeichnis stehen lassen, wenn das Einspielen Fehler gemeldet hat. Die unverschlüsselte Datenbanksicherung bleibt dann liegen, bis der Anwender sie löscht.

Refs #2592

🤖 Generated with [Claude Code](https://claude.com/claude-code)
