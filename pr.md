fix(documents): Platzhalterwerte mit $ oder \ in E-Mail- und beA-Vorlagen, Dateinamen und Prompts unverändert einsetzen

## Problem
Enthält der Wert eines Platzhalters ein `$`, z.B. „5.000 $“, ein Kennwort wie „$Kanzlei1“ oder „$1“, bricht das Einsetzen mit einem Fehler ab („Illegal group reference“). Ein `\` verschwindet stillschweigend, aus „C:\Akten\2026“ wird „C:Akten2026“. Betroffen sind alle Stellen, die `CommonTemplatesUtil.replacePlaceHolders()` nutzen: E-Mail- und beA-Vorlagen (Betreff, Text, Empfänger), Textbausteine, Dateinamen neuer Dokumente und die Prompts von Ingo.

## Ursache
`replacePlaceHolders()` setzt den Wert mit `String.replaceAll()` ein. Im Ersetzungstext von `replaceAll()` sind `$` (Verweis auf eine Gruppe) und `\` (Maskierung) Sonderzeichen.

## Fix
Der Wert wird mit `Matcher.quoteReplacement()` maskiert und damit wörtlich eingesetzt.

## Verifiziert
- Neuer Test `ReplacePlaceHoldersTest` (j-lawyer-server-common): Werte mit `$`, `$1` und `\` werden wörtlich eingesetzt, normale Werte wie bisher (getrimmt, leer bei `null`). Ohne die Änderung schlägt der Test fehl: „Illegal group reference: group index is missing“ und „Pfad: C:Akten2026“.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build: `mvn -B -ntp -pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Alle Module des Reactors bauen. Tests: 1100 ausgeführt (1097 auf master plus 3 neue), 18 fehlgeschlagen. Fehlgeschlagen sind nur FtpTest, SftpTest und SmbTest in j-lawyer-server-common (sie brauchen echte FTP-, SFTP- und SMB-Server) sowie `LibreOfficeODFTest.setReplaceEmptyLine` in j-lawyer-server-ejb (bekannter Fehler auf master). Das sind dieselben Klassen und Testfälle wie auf master 7b3b3dfa8 mit demselben Setup.

## Nicht abgedeckt
- `LibreOfficeAccess.evaluateScript()` (Skripte in Dokumentvorlagen) setzt Werte ebenfalls per `replaceAll()` ein. Dort kommt das Maskieren von Anführungszeichen für Groovy dazu, das gehört zu #1875 und ist nicht Teil dieses PRs.

Ohne eigenes Issue, gefunden beim Prüfen von #2888 (gleiche Methode).

🤖 Generated with [Claude Code](https://claude.com/claude-code)
