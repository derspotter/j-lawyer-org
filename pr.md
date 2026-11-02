fix(bea): beA-Symbol auf dem Desktop grau, wenn keine ungelesenen Nachrichten vorliegen

## Problem
In der oberen Leiste des Desktops sind die Symbole für Scans, Postausgang und Nachrichten weiß, wenn etwas vorliegt, und grau, wenn nicht. Das beA-Symbol ist nach dem Login immer weiß, auch ohne ungelesene beA-Nachrichten.

## Ursache
`DesktopPanel.onEvent()` zeigt beim `BeaStatusEvent` immer das normale Symbol, unabhängig von der Anzahl.

## Fix
Ohne ungelesene Nachrichten zeigt das beA-Label die graue Variante seines Symbols, die das Look-and-Feel auch für deaktivierte Labels verwendet (`LookAndFeel.getDisabledIcon()`). Das Label selbst bleibt aktiviert. Ein Klick öffnet also weiterhin das beA-Modul, auch wenn nichts Neues vorliegt.

## Verifiziert
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches und Vergleichsbuild von master 7b3b3dfa8 mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 268 gesamt, davon 268 ausgeführt und 0 übersprungen, 17 fehlgeschlagen. Auf master: 268 gesamt, 268 ausgeführt, 0 übersprungen, 17 fehlgeschlagen.
- Die fehlgeschlagenen Tests sind genau dieselben wie auf master: FtpTest, SftpTest und SmbTest in j-lawyer-server-common brauchen echte FTP-, SFTP- und SMB-Server mit Zugangsdaten aus Umgebungsvariablen.
- Kein Unit-Test: reine Swing-Anzeige im Desktop.

## Nicht abgedeckt
- Nicht in einem laufenden Client ausprobiert: Aussehen des grauen Symbols, Wechsel zwischen 0 und mehr ungelesenen Nachrichten und der Klick bei 0 sind nicht interaktiv geprüft.
- Der Wunsch nach stärkerem Kontrast zwischen grau und weiß betrifft alle Symbole und ist nicht Teil dieses PRs.

Closes #1705

🤖 Generated with [Claude Code](https://claude.com/claude-code)
