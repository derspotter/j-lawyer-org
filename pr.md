fix(documents): PDF aufteilen schreibt nur noch die Seiten des jeweiligen Teils in die Teil-PDFs

## Problem
„PDF aufteilen“ im Kontextmenü der Akte erzeugt Teile, die so groß sind wie das Original. Beim Beispiel aus dem Issue (BGB-PDF von gesetze-im-internet.de, 2 MB) waren Teil 1 und Teil 2 jeweils 2 MB groß. Das ist auch ein Datenschutzproblem: Ein Teil, der z.B. an das Gericht geht, enthält das komplette Originaldokument, auch wenn ein PDF-Betrachter nur die Seiten des Teils anzeigt.

## Ursache
`ArchiveFilePanel.mnuSplitPdfActionPerformed()` kopiert die Seiten mit `PDDocument.importPage()`. Verweise auf andere Seiten bleiben dabei erhalten, z.B. die Links eines Inhaltsverzeichnisses. Beim Speichern schreibt PDFBox deshalb alle verlinkten Seiten samt Seitenbaum des Originals mit in den Teil. PDFs ohne interne Links sind nicht betroffen, deshalb ließ sich das ohne passendes Beispiel nicht nachstellen. Die Aufteilung von Scans (`PDFSplitAction`) nutzt dagegen den `Splitter` von PDFBox, der solche Verweise entfernt.

## Fix
Neue Hilfsmethode `PdfSplitUtils.extractPages(source, von, bis)`, die einen Seitenbereich mit dem PDFBox-`Splitter` herauslöst, und Nutzung dieser Methode beim Aufteilen. Der `Splitter` entfernt Verweise auf Seiten außerhalb des Teils, so wie es auch die Aufteilung der Scans tut.

## Verifiziert
- Neuer Test `PdfSplitUtilsTest`: erzeugt ein PDF mit Inhaltsverzeichnis-Link von Seite 1 auf die letzte Seite und prüft, dass ein Teil nach Speichern und Neuladen nur seine eigenen Seitenobjekte enthält und alle Teile zusammen alle Seiten abdecken. Mit dem bisherigen `importPage` enthält ein 2-seitiger Teil eines 5-seitigen Dokuments 7 Seitenobjekte und ist größer als das ganze Original.
- Zusätzlich ausprobiert mit einem 82-seitigen PDF mit verlinktem Inhaltsverzeichnis (aus LibreOffice exportiert, 235 KB): Teil Seiten 1-3 bisher 130 KB mit 85 Seitenobjekten, jetzt 36 KB mit 3 Seitenobjekten.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Alle Module des Reactors bauen. Tests: 271 ausgeführt (268 auf master plus 3 neue), 17 fehlgeschlagen. Fehlgeschlagen sind nur FtpTest, SftpTest und SmbTest in j-lawyer-server-common (sie brauchen echte FTP-, SFTP- und SMB-Server). Das sind dieselben Klassen und Testfälle wie auf master 7b3b3dfa8 mit demselben Setup.

## Nicht abgedeckt
- Wie beim Aufteilen der Scans entfernt der `Splitter` auch Links auf Seiten innerhalb desselben Teils. Ein Inhaltsverzeichnis im Teil ist danach nicht mehr anklickbar, der Text bleibt erhalten.
- Das Beispiel-PDF aus dem Issue konnte hier nicht heruntergeladen werden.

Closes #3507

🤖 Generated with [Claude Code](https://claude.com/claude-code)
