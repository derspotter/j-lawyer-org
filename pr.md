fix(finanzen): Warnung beim Speichern eines Belegs mit unbestätigter Belegart/Nummernkreis

## Problem
Um Belegart oder Nummernkreis eines Belegs zu ändern, klickt man auf den Stift, wählt neu aus und bestätigt mit dem Haken. Klickt man stattdessen direkt auf „Speichern“, wird die Auswahl ohne Hinweis verworfen und der Beleg behält z.B. den Nummernkreis „Entwurf“.

## Ursache
Die Änderung von Belegart und Nummernkreis wird bewusst nur über den Haken übernommen, weil dabei die Belegnummer vergeben wird. `InvoiceDialog.cmdSaveActionPerformed()` prüft aber nicht, ob dieser Bearbeitungsmodus noch offen ist.

## Fix
Wie von j-dimension im Issue vorgeschlagen, erscheint beim Speichern eine Warnung, wenn Belegart und Nummernkreis noch im Bearbeitungsmodus sind: „Belegart und Nummernkreis wurden noch nicht mit dem Haken bestätigt. Eine Änderung daran wird nicht gespeichert. Trotzdem speichern?“ Mit „Nein“ bleibt der Dialog offen und man kann die Änderung bestätigen. Mit „Ja“ wird wie bisher gespeichert. Das explizite Bestätigen bleibt also erhalten.

## Verifiziert
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Alle Module des Reactors bauen. Tests: 268 ausgeführt, 17 fehlgeschlagen. Fehlgeschlagen sind nur FtpTest, SftpTest und SmbTest in j-lawyer-server-common (sie brauchen echte FTP-, SFTP- und SMB-Server). Das sind dieselben Klassen und Testfälle wie auf master 7b3b3dfa8 mit demselben Setup.
- Kein Unit-Test: reine Dialoglogik in einem modalen Swing-Dialog.

## Nicht abgedeckt
- Die Warnung erscheint, sobald der Stift aktiv ist, auch wenn die Auswahl nicht verändert wurde. Ein Vergleich mit den gespeicherten Werten wäre möglich, macht die Änderung aber größer.
- Kein manueller Test gegen einen laufenden Server.

Closes #2169

🤖 Generated with [Claude Code](https://claude.com/claude-code)
