fix(documents): Tabellen aus Rechnern beim Einfügen in voller Breite und mit eingestellter Schriftgröße

## Problem
Kopiert man eine Tabelle aus den Rechner-Plugins (oder aus dem Forderungskonto) und fügt sie in ein Dokument ein, stimmt die Schriftgröße nicht mit der Einstellung in den Tabelleneinstellungen überein: Bei 16 erscheint sie in 12 Punkt, bei 12 noch kleiner. Außerdem ist die Tabelle schmaler als die Textbreite.

## Ursache
`StyledCalculationTable.toHtml()` erzeugt das HTML für die Zwischenablage. Die eingestellte Schriftgröße (`plugins.global.tableproperties.table.fontsize`) wird dort als Pixelwert behandelt und auf die HTML-Stufen von `<font size>` abgebildet. Aus 16 wird Stufe 3, die LibreOffice und Word als 12 Punkt einfügen. Beim Einsetzen derselben Tabelle in DOCX- und ODT-Vorlagen wird der Wert dagegen als Punktgröße verwendet. Die Tabellenbreite ist fest auf 85% gesetzt. Zudem werden `<b>`, `<i>` und `<u>` am Zellenende erneut geöffnet statt geschlossen.

## Fix
- Die Schriftgröße wird als `style="font-size:16pt"` ausgegeben, also in Punkt wie bei den Vorlagen. Ohne eingestellte Größe wird keine gesetzt, ebenfalls wie bei den Vorlagen.
- Die Tabelle wird mit 100% Breite ausgegeben.
- `<b>`, `<i>` und `<u>` werden korrekt geschlossen.
- Die nicht mehr verwendete Umrechnung `pixelFontSizeToHtmlSize()` entfällt.

## Verifiziert
- Neuer Test `StyledCalculationTableHtmlTest` (j-lawyer-server-common): Punktgröße statt `<font size>`, keine Größe ohne Einstellung, 100% Breite mit und ohne Rahmen, Formatierungstags geschlossen.
- Import des HTML mit LibreOffice 24.2 (Writer-HTML-Filter, wie beim Einfügen aus der Zwischenablage) und Prüfung des erzeugten ODT: bisheriges HTML mit Einstellung 16 ergibt `fo:font-size="12pt"` und 85% Breite, neues HTML ergibt `16pt` und 100% Breite.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build: `mvn -B -ntp -pl j-lawyer-client,j-lawyer-server/j-lawyer-server-ejb -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Alle Module des Reactors bauen. Tests: 1101 ausgeführt (1097 auf master plus 4 neue), 18 fehlgeschlagen. Fehlgeschlagen sind nur FtpTest, SftpTest und SmbTest in j-lawyer-server-common (brauchen echte FTP-, SFTP- und SMB-Server) sowie `LibreOfficeODFTest.setReplaceEmptyLine` in j-lawyer-server-ejb (bekannter Fehler auf master). Das sind dieselben Klassen und Testfälle wie auf master 7b3b3dfa8 mit demselben Setup.

## Nicht abgedeckt
- Wie die Rechner-Plugins die Tabelle in die Zwischenablage legen, liegt außerhalb dieses Repositorys (j-lawyer-calculations). Im Repository nutzt das Forderungskonto (`ClaimLedgerDialog`) denselben Weg über `toHtml()`.
- Einfügen in Microsoft Word nicht getestet.

Closes #3410

🤖 Generated with [Claude Code](https://claude.com/claude-code)
