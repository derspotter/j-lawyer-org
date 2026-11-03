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
- Lässt sich die Zahl nicht bestimmen, wird nichts veröffentlicht und der Zähler behält seinen Wert. Das ist der Fall, wenn für ein Postfach kein Posteingang gefunden wird (die Ordner konnten nicht geladen werden oder enthalten keinen Posteingang) oder das Laden eines Posteingangs fehlschlägt. Das gilt auch, wenn nur eines von mehreren Postfächern scheitert, denn dessen Posteingang kann ungelesene Nachrichten enthalten. Den Grund eines solchen Fehlers kann der Client nicht erkennen, weil die beA-Aufrufe über den Server laufen. Eine fehlende Berechtigung lässt sich also nicht von z.B. einer Zeitüberschreitung unterscheiden. `BeaCheckTimerTask` verhält sich schon so, dort beendet jeder Fehler die Zählung ohne Meldung.
- In der ersten Fassung dieses PRs wurde 0 angezeigt, wenn die Ordner keines Postfachs geladen werden konnten. Das ist damit behoben.

## Verifiziert
- Neuer Test `BeaUnreadCounterTest` (j-lawyer-client) mit 10 Fällen. Zählung: Summe über zwei Postfächer, leere Posteingänge ergeben 0, „nicht bestimmbar“ wenn die Ordner keines Postfachs oder eines von zwei Postfächern nicht geladen werden können, wenn für eines von zwei Postfächern oder für keines ein Posteingang gefunden wird und wenn die Nachrichten eines Posteingangs nicht geladen werden können. Veröffentlichung über den `EventBroker`, mit einem angemeldeten Empfänger und auf dem Event-Dispatch-Thread zugestellt: genau ein Event mit der Summe bei zwei Postfächern, genau ein Event mit 0 bei leeren Posteingängen, kein Event, wenn für kein Postfach oder für eines von zwei Postfächern ein Posteingang gefunden wird oder ein Posteingang nicht geladen werden kann.
- Mit dem Stand vom 02.11. ergibt der Fall „Posteingang von Postfach 1 leer, Ordner von Postfach 2 nicht ladbar“ den Wert 0, der Zähler wäre also auf 0 gesetzt worden.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches (Stand 4b7600b07, gestartet 03.11.2026 09:37 UTC) und Vergleichsbuild von master 7b3b3dfa8 (02.11.2026) mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 278 gesamt, davon 278 ausgeführt und 0 übersprungen, 17 fehlgeschlagen. Auf master: 268 gesamt, 268 ausgeführt, 0 übersprungen, 17 fehlgeschlagen. Neu sind 10 Tests.
- Die fehlgeschlagenen Tests sind genau dieselben wie auf master: FtpTest und SftpTest in j-lawyer-server-common brauchen einen echten FTP- bzw. SFTP-Server mit Zugangsdaten aus Umgebungsvariablen. SmbTest braucht die im Test fest eingetragene SMB-Freigabe smb://j-lawyer-box/j-lawyer-share/.

## Nicht abgedeckt
- Nicht gegen beA oder beAstie und nicht in einem laufenden Client ausprobiert. `BeaAccess` wurde nicht geändert.
- Kann ein Postfach dauerhaft nicht geladen werden (laut Log meist fehlende Berechtigungen), wird der Zähler beim Aktualisieren der Ordner nicht mehr neu gesetzt. Bei der regelmäßigen Abfrage in `BeaCheckTimerTask` war das schon bisher so. Der zuletzt ermittelte Wert bleibt stehen und kann veraltet sein.
- Nach dem Lesen oder Ablegen einer Nachricht wird der Zähler weiterhin erst bei der nächsten Abfrage (alle knapp 10 Minuten) oder beim nächsten Aktualisieren der Ordner neu berechnet.
- Ob beim Melder des Issues mehrere Postfächer eingebunden sind, ist nicht bestätigt. Mit nur einem Postfach erklärt dieser Fehler das Verhalten nicht.

Refs #2658

🤖 Generated with [Claude Code](https://claude.com/claude-code)
