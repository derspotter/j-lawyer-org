fix(bea): Hinweis, wenn markierte beA-Nachrichten nicht gelöscht werden können

## Problem
Schlägt das Löschen markierter beA-Nachrichten fehl (Verschieben in den Papierkorb bzw. endgültiges Löschen im Papierkorb), passiert scheinbar nichts. Die Nachrichten bleiben in der Liste, eine Meldung erscheint nicht. Beim Leeren des Papierkorbs werden solche Fehler dagegen gemeldet.

## Ursache
`BeaInboxPanel.cmdDeleteActionPerformed()` schreibt Fehler je Nachricht nur ins Log und übergeht auch ein `false` als Ergebnis von `deleteMessage()` bzw. `moveMessageToTrash()`.

## Fix
Fehlgeschlagene Nachrichten werden gezählt und am Ende gemeldet, wie beim Leeren des Papierkorbs: „2 Nachricht(en) konnten nicht gelöscht werden, letzte Meldung: …“.

## Verifiziert
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Alle Module des Reactors bauen. Tests: 268 ausgeführt, 17 fehlgeschlagen. Fehlgeschlagen sind nur FtpTest, SftpTest und SmbTest in j-lawyer-server-common (sie brauchen echte FTP-, SFTP- und SMB-Server). Das sind dieselben Klassen und Testfälle wie auf master 7b3b3dfa8 mit demselben Setup.
- Kein Test gegen beA oder beAstie und kein Unit-Test: Die Meldung hängt am Ergebnis der Serveraufrufe im Swing-Panel.

## Nicht abgedeckt
- Warum das Löschen fehlschlägt, z.B. bei #3396, ändert dieser PR nicht. Er sorgt nur dafür, dass der Fehler sichtbar wird.

Refs #3396

🤖 Generated with [Claude Code](https://claude.com/claude-code)
