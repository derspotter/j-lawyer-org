fix(backup): Fehler beim Einspielen der Datenbanksicherung erkennen und wahlweise fortsetzen

## Problem
Schlagen beim Wiederherstellen einer Datensicherung Anweisungen aus der Datenbanksicherung fehl, meldet der Backupmanager mit dem MariaDB-Client trotzdem eine erfolgreiche Wiederherstellung. Mit dem MySQL-Client kommt nur „Datenbankwiederherstellung fehlgeschlagen!“ ohne den Fehler. Die Datenbank ist in beiden Fällen unvollständig. Aufgefallen bei #2592: Beim Einspielen einer MySQL-8-Sicherung in MariaDB fehlen alle Tabellen, die Wiederherstellung gilt aber als erfolgreich.

Die Datenbank ist der letzte Schritt, die Dateien sind dann schon wiederhergestellt. Ein reiner Abbruch hinterlässt einen Mischzustand, und damit die Wiederherstellung durchläuft, müsste man das ZIP mit der Datenbanksicherung reparieren.

## Ursache
`RestoreExecutor.restoreDatabase()` spielt die Sicherung mit einem Aufruf von `mysql -e` ein, der die Datenbank löscht, neu anlegt und dann `source …/jlawyerdb-dump.sql` ausführt. Geprüft wird nur der Exit-Code. Bei einem Fehler in einer per `source` eingelesenen Anweisung verhalten sich die Clients unterschiedlich (geprüft mit MariaDB 10.11.14 und MySQL 8.0.46):
- Der MariaDB-Client macht auch ohne `--force` bis zum Ende der Datei weiter, gibt die Fehler auf stderr aus und endet mit Exit-Code 0.
- Der MySQL-Client hört bei der ersten fehlerhaften Anweisung auf und endet mit Exit-Code 1. `--force` wirkt bei ihm nur, wenn es nach `-e` steht, weil `-e` ein vorher angegebenes `--force` zurücksetzt.

## Fix
- Der Client wird mit gemeinsamem stdout und stderr gestartet und seine Ausgabe zeilenweise angezeigt. Fehlerzeilen wie `ERROR 1273 (HY000) at line 6 in file: '…'` werden gesammelt, Warnungen zählen nicht. Weil beide Ausgaben zusammen gelesen werden, kann der Client nicht mehr an einer vollen stderr-Pipe hängen bleiben. Der MariaDB-Client schreibt jede fehlerhafte Anweisung vollständig auf stderr.
- Beim ersten Fehler in einer Anweisung der Sicherung zeigt die Wiederherstellung den Fehler und den Pfad der ausgepackten Sicherung und fragt: „Abbrechen“ oder „Fehler ignorieren und fortsetzen“.
  - Oberfläche: Dialog in `BackupMgrController`. Schließen des Dialogs bricht ab.
  - Konsole: Rückfrage „Fehler ignorieren und fortsetzen J/N“ wie bei der Bestätigung vor der Wiederherstellung. Mit dem neuen Parameter `--ignore-db-errors` geht es ohne Rückfrage weiter, für unbeaufsichtigte Läufe. Endet die Eingabe ohne Antwort, wird abgebrochen wie bisher.
- Abbrechen: Die Wiederherstellung schlägt fehl mit „Datenbankwiederherstellung fehlgeschlagen: …“, dem Fehler und dem Pfad der ausgepackten Sicherung.
- Fortsetzen: Hat der Client beim Fehler aufgehört (MySQL-Client, Exit-Code ungleich 0), läuft er ein zweites Mal mit `--force` am Ende der Kommandozeile. Wie der erste Lauf löscht der zweite die Datenbank zuerst und legt sie neu an, keine Anweisung wird doppelt angewendet. Der MariaDB-Client ist zu diesem Zeitpunkt schon am Ende der Sicherung angekommen, ein zweiter Lauf entfällt.
- Am Ende steht dann „Wiederherstellung mit Fehlern abgeschlossen“ mit allen Fehlern und dem Pfad der ausgepackten Sicherung statt „erfolgreich“. Die Oberfläche zeigt die Liste in einem Dialog. Die Konsole gibt sie aus und endet mit Exit-Code 2 (bei Erfolg weiter 0, bei Fehlschlag 1).
- Fehler außerhalb der Sicherung, z.B. eine abgelehnte Anmeldung, brechen wie bisher ohne Rückfrage ab.
- Den Pfad der ausgepackten Sicherung nennt der Client in jeder Fehlerzeile („in file: '…'“), die Meldungen übernehmen ihn von dort. Auf master ist das `<Sicherungsverzeichnis>/jlawyerdb-dump.sql`, dorthin packt `restoreDatabase` die Sicherung aus.
- Die Entscheidung steckt in der neuen Klasse `DatabaseImport` und ist dort getestet. Dialog und Konsolenabfrage zeigen nur die fertige Meldung und geben die Antwort zurück.

## Verifiziert
- Neue Tests in j-lawyer-backupmgr: `DatabaseImportTest` mit 12 Fällen (Fehlerzeilen und Pfad, keine Fehler, Fortsetzen mit zweitem Lauf nach Abbruch des Clients, Fortsetzen ohne zweiten Lauf, Abbrechen, keine Rückfrage möglich, `--ignore-db-errors`, Fehler außerhalb der Sicherung, Abbruch des Clients im zweiten Lauf, Fehlschlag ohne Fehlerzeile, Abschlussmeldung, Aufruf eines Ersatz-Clients mit `--force` am Ende und über 200 KB auf stderr) und `BackupManagerLauncherTest` mit 2 Fällen (Parameter `--ignore-db-errors`, Rückfrage auf der Konsole mit j, n und ohne Eingabe). Die Tests prüfen neuen Code, auf dem vorigen Stand e256acae0 lassen sie sich nicht übersetzen.
- Ende-zu-Ende mit dem gebauten Backupmanager auf der Konsole (`-console`, Antworten über stdin). Die Sicherung enthält eine Tabelle mit unbekannter Kollation zwischen zwei intakten Tabellen:
  - MySQL-Client 8.0.46 gegen MySQL 8.0.46: Abbrechen endet mit Exit-Code 1 und der Meldung mit Fehler und Pfad, nur die Tabelle vor dem Fehler ist da. Fortsetzen spielt neu ein und endet mit Exit-Code 2, beide Fehler sind aufgelistet, beide intakten Tabellen vollständig und ohne doppelte Zeilen. `--ignore-db-errors` fragt nicht und ergibt dasselbe.
  - MariaDB-Client 10.11.14 gegen MariaDB 10.11.14: Fortsetzen ohne zweiten Lauf, Exit-Code 2, dieselbe Liste. Abbrechen und eine fehlende Antwort enden mit Exit-Code 1.
  - Fehlerfreie Sicherung: „Backup erfolgreich wiederhergestellt.“ mit Exit-Code 0.
  - Zum Vergleich mit derselben Sicherung: master meldet mit dem MariaDB-Client Erfolg, der vorige Stand e256acae0 meldet mit dem MySQL-Client nur „Datenbankwiederherstellung fehlgeschlagen!“ ohne den Fehler.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches (Stand f7aa51e4b, gestartet 05.11.2026 08:31 UTC) und Vergleichsbuild von master d34cd0531 (05.11.2026) mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-backupmgr -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 14 gesamt, davon 14 ausgeführt und 0 übersprungen, 0 fehlgeschlagen. Auf master: 0 gesamt, 0 ausgeführt, 0 übersprungen, 0 fehlgeschlagen. Neu sind 14 Tests.
- Keine fehlgeschlagenen Tests, wie auf master.
- Zusammen mit #3767 (Kollation bei MariaDB): Der Branch lässt sich ohne Konflikte mit #3767 zusammenführen, der zusammengeführte Stand baut, und alle 35 Tests der beiden PRs laufen durch.

## Nicht abgedeckt
- Die Oberfläche ist nicht ausprobiert, in dieser Umgebung gibt es keine Anzeige. Rückfrage-Dialog und Abschlussdialog sind nicht getestet.
- Windows, macOS und verschlüsselte Sicherungen sind nicht getestet.
- Mit dem MariaDB-Client kommt die Rückfrage erst, wenn der Client die ganze Sicherung eingespielt hat. Abbrechen meldet dann einen Fehlschlag, die Datenbank enthält aber schon alles außer den fehlerhaften Anweisungen.
- Zusammen mit #3767 nennen die Meldungen den Pfad der Sicherung im Arbeitsverzeichnis von #3767. Dieses Verzeichnis löscht #3767 am Ende der Wiederherstellung, die genannte Datei gibt es danach nicht mehr. Ob die ausgepackte Sicherung nach Fehlern liegen bleiben soll, ist noch zu entscheiden (Details in report.md).

Refs #2592

🤖 Generated with [Claude Code](https://claude.com/claude-code)
