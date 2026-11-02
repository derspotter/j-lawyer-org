fix(bea): Zähler ungelesener beA-Nachrichten summiert alle Postfächer statt nur das zuletzt geprüfte

## Problem
Der beA-Zähler (Modulleiste und Desktop) zählt bei neuen Nachrichten hoch, aber nach dem Lesen oder Ablegen einer Nachricht nicht wieder herunter. Er bleibt z.B. auf 2, obwohl im Posteingang nur noch eine gelesene Nachricht liegt.

## Ursache
Beide Stellen, die die Zahl der ungelesenen Nachrichten melden, werten bei mehreren Postfächern nur eines aus:
- `BeaCheckTimerTask.run()` überschreibt die Zahl in der Schleife über die Postfächer (`unread = messages.size()`) und meldet nur die des zuletzt geprüften Postfachs.
- `BeaInboxPanel.refreshFolders()` veröffentlicht je Posteingang ein eigenes `BeaStatusEvent`. Jedes Event ersetzt beim Empfänger die vorherige Zahl, angezeigt wird also wieder nur das zuletzt geladene Postfach.
Wer mehrere Postfächer eingebunden hat (z.B. mehrere Berufsträger einer Kanzlei), sieht damit die Zahl eines anderen Postfachs. Liest oder legt man dann eine Nachricht im ersten Postfach ab, ändert sich der Zähler nicht.

## Fix
- `BeaCheckTimerTask` summiert die ungelesenen Nachrichten aller Postfächer.
- `BeaInboxPanel.refreshFolders()` zählt über die neue Klasse `BeaUnreadCounter` alle Posteingänge und veröffentlicht ein einziges Event mit der Gesamtzahl.
- Lässt sich die Zahl nicht bestimmen, weil kein Postfach geladen werden konnte oder das Laden eines Posteingangs fehlschlägt, wird nichts veröffentlicht und der Zähler behält seinen Wert. Bisher galt das auch, nach der ersten Fassung dieses PRs wäre dann 0 angezeigt worden.

## Verifiziert
- Neuer Test `BeaUnreadCounterTest` (j-lawyer-client): Summe über zwei Postfächer, leere Posteingänge ergeben 0, kein Postfach geladen und Fehler beim Laden eines Posteingangs ergeben „nicht bestimmbar“.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches und Vergleichsbuild von master 7b3b3dfa8 mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 272 gesamt, davon 272 ausgeführt und 0 übersprungen, 17 fehlgeschlagen. Auf master: 268 gesamt, 268 ausgeführt, 0 übersprungen, 17 fehlgeschlagen. Neu sind 4 Tests.
- Die fehlgeschlagenen Tests sind genau dieselben wie auf master: FtpTest, SftpTest und SmbTest in j-lawyer-server-common brauchen echte FTP-, SFTP- und SMB-Server mit Zugangsdaten aus Umgebungsvariablen.

## Nicht abgedeckt
- Nicht gegen beA oder beAstie und nicht in einem laufenden Client ausprobiert. `BeaAccess` wurde nicht geändert.
- Können nur einige Postfächer nicht geladen werden (laut Log meist fehlende Berechtigungen), zählt der Zähler die lesbaren Postfächer.
- Nach dem Lesen oder Ablegen einer Nachricht wird der Zähler weiterhin erst bei der nächsten Abfrage (alle knapp 10 Minuten) oder beim nächsten Aktualisieren der Ordner neu berechnet.
- Ob beim Melder des Issues mehrere Postfächer eingebunden sind, ist nicht bestätigt. Mit nur einem Postfach erklärt dieser Fehler das Verhalten nicht.

Refs #2658

🤖 Generated with [Claude Code](https://claude.com/claude-code)
