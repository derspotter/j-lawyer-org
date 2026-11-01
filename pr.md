fix(documents): Aktenvorschlag beim Zuordnen erkennt auch Aktenzeichen mit Großbuchstaben

## Problem
Beim Zuordnen zu einer Akte (E-Mail, beA, Scans, Kontoauszüge und weitere) schlägt der Dialog „Akte suchen und zuordnen“ passende Akten vor, wenn deren Aktenzeichen im Betreff oder Text vorkommt. Enthält das Aktenzeichen Großbuchstaben, z.B. mit dem Platzhalter `C` (zufällige Buchstaben) im Muster für Aktenzeichen, wird die Akte nie vorgeschlagen.

## Ursache
`SearchAndAssignDialog` wandelt den Suchtext in Kleinbuchstaben um, vergleicht ihn aber mit dem unveränderten Aktenzeichen (`searchContext.contains(a.getFileNumber())`). Großbuchstaben kommen in einem kleingeschriebenen Text nie vor. Dasselbe gilt beim Vorziehen passender Treffer aus der Volltextsuche.

## Fix
Das Aktenzeichen wird vor dem Vergleich ebenfalls in Kleinbuchstaben umgewandelt, wie es für das Kurzrubrum schon geschieht.

## Verifiziert
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Alle Module des Reactors bauen. Tests: 268 ausgeführt, 17 fehlgeschlagen. Fehlgeschlagen sind nur FtpTest, SftpTest und SmbTest in j-lawyer-server-common (sie brauchen echte FTP-, SFTP- und SMB-Server). Das sind dieselben Klassen und Testfälle wie auf master 7b3b3dfa8 mit demselben Setup.
- Kein Unit-Test: Der Vergleich steckt im Dialog zwischen Serveraufrufen.

## Nicht abgedeckt
- Vorgeschlagen wird weiterhin aus den 150 zuletzt geänderten Akten, danach aus der Volltextsuche. Das ist unverändert.

Ohne eigenes Issue, gefunden beim Prüfen von #3230.

🤖 Generated with [Claude Code](https://claude.com/claude-code)
