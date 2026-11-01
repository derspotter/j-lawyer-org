fix(bea): beA-Symbol auf dem Desktop grau, wenn keine ungelesenen Nachrichten vorliegen

## Problem
In der oberen Leiste des Desktops sind die Symbole für Scans, Postausgang und Nachrichten weiß, wenn etwas vorliegt, und grau, wenn nicht. Das beA-Symbol ist nach dem Login immer weiß, auch ohne ungelesene beA-Nachrichten.

## Ursache
`DesktopPanel.onEvent()` setzt beim `BeaStatusEvent` das Label `lblUnreadBea` immer auf aktiviert, unabhängig von der Anzahl. Die anderen Symbole werden nur bei Anzahl größer 0 aktiviert, z.B. Scans und Nachrichten.

## Fix
`lblUnreadBea` wird nur aktiviert, wenn es ungelesene Nachrichten gibt, sonst deaktiviert und damit grau dargestellt, wie bei Scans und Nachrichten.

## Verifiziert
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Alle Module des Reactors bauen. Tests: 268 ausgeführt, 17 fehlgeschlagen. Fehlgeschlagen sind nur FtpTest, SftpTest und SmbTest in j-lawyer-server-common (sie brauchen echte FTP-, SFTP- und SMB-Server). Das sind dieselben Klassen und Testfälle wie auf master 7b3b3dfa8 mit demselben Setup.
- Kein Unit-Test: reine Swing-Anzeige.

## Nicht abgedeckt
- Wie bei den anderen grauen Symbolen öffnet ein Klick auf das graue beA-Symbol das beA-Modul nicht (`configureStatusLabel()` ignoriert Klicks auf deaktivierte Labels). Das beA-Modul bleibt über die Modulleiste erreichbar.
- Der Wunsch nach stärkerem Kontrast zwischen grau und weiß betrifft alle Symbole und ist nicht Teil dieses PRs.

Closes #1705

🤖 Generated with [Claude Code](https://claude.com/claude-code)
