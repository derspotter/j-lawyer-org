fix(documents): PDF aufteilen schreibt nur noch die Seiten des jeweiligen Teils in die Teil-PDFs

## Problem
„PDF aufteilen“ im Kontextmenü der Akte erzeugt Teile, die so groß sind wie das Original. Beim Beispiel aus dem Issue (BGB-PDF von gesetze-im-internet.de, 2 MB) waren Teil 1 und Teil 2 jeweils 2 MB groß. Das ist auch ein Datenschutzproblem: Ein Teil, der z.B. an das Gericht geht, enthält das komplette Originaldokument, auch wenn ein PDF-Betrachter nur die Seiten des Teils anzeigt.

## Ursache
`ArchiveFilePanel.mnuSplitPdfActionPerformed()` kopiert die Seiten mit `PDDocument.importPage()`. Verweise der Anmerkungen auf andere Seiten bleiben dabei erhalten, z.B. die Links eines Inhaltsverzeichnisses. Beim Speichern schreibt PDFBox deshalb alle verwiesenen Seiten samt Seitenbaum des Originals mit in den Teil. PDFs ohne solche Verweise sind nicht betroffen, deshalb ließ sich das ohne passendes Beispiel nicht nachstellen.

## Fix
- Neue Hilfsmethode `PdfSplitUtils.extractPages(source, von, bis)`. Sie löst den Seitenbereich mit dem PDFBox-`Splitter` heraus, der die direkten Ziele von Links entfernt (wie bei der Aufteilung der Scans). Zusätzlich entfernt sie in den Anmerkungen der übernommenen Seiten alle übrigen Verweise auf Seiten außerhalb des Teils und auf Anmerkungen dieser Seiten: weitere Felder-Widgets desselben Formularfelds (`/Parent`, `/Kids`), Folgeaktionen (`/Next`), Antworten (`/IRT`), Popups und Artikel-Beads.
- Doppelt eingegebene Seitenzahlen (z.B. „2,2“) ergeben keinen leeren Teil mehr. Bisher wurde dafür ein leeres PDF abgelegt.
- Die PDF-Dokumente werden in einem try-with-resources geschlossen, auch wenn ein Fehler auftritt.

## Verifiziert
- Test `PdfSplitUtilsTest` (j-lawyer-client) mit 6 Fällen, darunter Inhaltsverzeichnis-Link, Formularfeld mit Widgets auf Seite 1 und Seite 5, Link mit Folgeaktion auf Seite 5 und doppelte Seitenzahlen. Nach Speichern und Neuladen enthält ein Teil genau seine eigenen Seitenobjekte. Nur mit dem `Splitter` enthalten die Teile beim Formularfeld und bei der Folgeaktion 7 statt 2 Seitenobjekte, mit dem bisherigen `importPage` schon beim einfachen Link.
- Zusätzlich ausprobiert mit einem 82-seitigen PDF mit verlinktem Inhaltsverzeichnis (aus LibreOffice exportiert, 235 KB): Teil Seiten 1-3 bisher 130 KB mit 85 Seitenobjekten, jetzt 36 KB mit 3 Seitenobjekten. Teile 4-82, 11-11 und 1-82 enthalten ebenfalls genau ihre Seiten.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches und Vergleichsbuild von master 7b3b3dfa8 mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 274 gesamt, davon 274 ausgeführt und 0 übersprungen, 17 fehlgeschlagen. Auf master: 268 gesamt, 268 ausgeführt, 0 übersprungen, 17 fehlgeschlagen. Neu sind 6 Tests.
- Die fehlgeschlagenen Tests sind genau dieselben wie auf master: FtpTest, SftpTest und SmbTest in j-lawyer-server-common brauchen echte FTP-, SFTP- und SMB-Server mit Zugangsdaten aus Umgebungsvariablen.

## Nicht abgedeckt
- Nicht in einem laufenden Client ausprobiert, das Beispiel-PDF aus dem Issue konnte hier nicht heruntergeladen werden.
- Wie beim Aufteilen der Scans entfernt der `Splitter` auch Links auf Seiten innerhalb desselben Teils. Ein Inhaltsverzeichnis im Teil ist danach nicht mehr anklickbar, der Text bleibt erhalten.
- Formularfelder sind im Teil nicht mehr bearbeitbar, weil das Formular des Dokuments nicht übernommen wird (wie bisher). Das Widget auf der übernommenen Seite und seine Darstellung bleiben erhalten.
- Verweise außerhalb der Anmerkungen (Seiteninhalt, Ressourcen) werden nicht geändert, dort verweisen PDFs nicht auf andere Seiten.

Closes #3507

🤖 Generated with [Claude Code](https://claude.com/claude-code)
