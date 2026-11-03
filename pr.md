fix(documents): PDF aufteilen schreibt nur noch die Seiten des jeweiligen Teils in die Teil-PDFs

## Problem
„PDF aufteilen“ im Kontextmenü der Akte erzeugt Teile, die so groß sind wie das Original. Beim Beispiel aus dem Issue (BGB-PDF von gesetze-im-internet.de, 2 MB) waren Teil 1 und Teil 2 jeweils 2 MB groß. Das ist auch ein Datenschutzproblem: Ein Teil, der z.B. an das Gericht geht, enthält das komplette Originaldokument, auch wenn ein PDF-Betrachter nur die Seiten des Teils anzeigt.

## Ursache
`ArchiveFilePanel.mnuSplitPdfActionPerformed()` kopiert die Seiten mit `PDDocument.importPage()`. Verweise der Anmerkungen auf andere Seiten bleiben dabei erhalten, z.B. die Links eines Inhaltsverzeichnisses. Beim Speichern schreibt PDFBox deshalb alle verwiesenen Seiten samt Seitenbaum des Originals mit in den Teil. PDFs ohne solche Verweise sind nicht betroffen, deshalb ließ sich das ohne passendes Beispiel nicht nachstellen.

## Fix
- Neue Hilfsmethode `PdfSplitUtils.extractPages(source, von, bis)`. Sie löst den Seitenbereich mit dem PDFBox-`Splitter` heraus, der die direkten Ziele von Links entfernt (wie bei der Aufteilung der Scans). Danach prüft sie jedes Objekt, das beim Speichern des Teils geschrieben wird, also alles, was vom Trailer des Teils aus erreichbar ist (Katalog, Seiten, Anmerkungen, Seitenaktionen `/AA`, Artikel-Beads `/B`, Formularfelder, Ressourcen und Dokumentinfo).
- Verweise auf Seiten des Originals, die zum Teil gehören, zeigen danach auf die Kopie im Teil. Folgeaktionen, Seitenaktionen und Artikel innerhalb des Teils funktionieren also weiter.
- Verweise auf alle anderen Seiten, auf die Knoten des Seitenbaums des Originals und auf Anmerkungen der anderen Seiten (z.B. weitere Widgets desselben Formularfelds) werden entfernt. In Zielangaben wird die Seite durch null ersetzt, damit die Struktur gültig bleibt.
- Als zusätzliche Absicherung wird jede andere Seite und jeder andere Seitenbaumknoten entfernt, der nicht zum Seitenbaum des Teils gehört. Damit kann keine fremde Seite und kein fremder Seitenbaum mehr in den Teil geschrieben werden.
- Doppelt eingegebene Seitenzahlen (z.B. „2,2“) ergeben keinen leeren Teil mehr. Bisher wurde dafür ein leeres PDF abgelegt.
- Die PDF-Dokumente werden in einem try-with-resources geschlossen, auch wenn ein Fehler auftritt.

## Verifiziert
- Test `PdfSplitUtilsTest` (j-lawyer-client) mit 11 Fällen: Inhaltsverzeichnis-Link, Formularfeld mit Widgets auf Seite 1 und Seite 5, Folgeaktion auf Seite 5, Folgeaktion auf die behaltene Seite 2, Artikel von Seite 1 bis Seite 5, Seitenaktionen beim Öffnen (Seite 1 auf Seite 5) und Schließen (Seite 2 auf Seite 1), der hintere Teil desselben Dokuments, alle 15 möglichen Seitenbereiche eines 5-seitigen Dokuments mit all diesen Verweisen sowie doppelte Seitenzahlen. Nach Speichern und Neuladen enthält jeder Teil genau seine eigenen Seitenobjekte in der richtigen Reihenfolge, und kein Datenstrom des Teils enthält den Text einer anderen Seite. Verweise auf behaltene Seiten zeigen auf die Seite im Teil.
- Mit dem Stand vom 02.11. (nur Anmerkungen und Beads geprüft, behaltene Seiten des Originals nicht umgelenkt) schlagen 5 dieser Tests fehl, die Teile enthalten 6 bis 8 Seitenobjekte.
- Zusätzlich mit 9 echten PDFs aus dem Repository ausprobiert (8 Formulare aus `zvfv/2024-09-01` mit Formularfeldern und `doc/forderungskonto-und-mahnverfahren.pdf`), jeweils an jeder Seitengrenze geteilt, 76 Teile: Seitenzahl, Text jeder Seite und Anzahl der Seitenobjekte stimmen, der jeweils größte Teil ist auf das Byte genauso groß wie mit dem Stand vom 02.11.
- Am 01.11. mit einem 82-seitigen PDF mit verlinktem Inhaltsverzeichnis (aus LibreOffice exportiert, 235 KB): Teil Seiten 1-3 mit dem bisherigen `importPage` 130 KB mit 85 Seitenobjekten, mit `extractPages()` 36 KB mit 3 Seitenobjekten.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches und Vergleichsbuild von master 7b3b3dfa8 mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 279 gesamt, davon 279 ausgeführt und 0 übersprungen, 17 fehlgeschlagen. Auf master: 268 gesamt, 268 ausgeführt, 0 übersprungen, 17 fehlgeschlagen. Neu sind 11 Tests.
- Die fehlgeschlagenen Tests sind genau dieselben wie auf master: FtpTest und SftpTest in j-lawyer-server-common brauchen einen echten FTP- bzw. SFTP-Server mit Zugangsdaten aus Umgebungsvariablen. SmbTest braucht die im Test fest eingetragene SMB-Freigabe smb://j-lawyer-box/j-lawyer-share/.

## Nicht abgedeckt
- Nicht in einem laufenden Client ausprobiert, das Beispiel-PDF aus dem Issue konnte hier nicht heruntergeladen werden.
- Wie beim Aufteilen der Scans entfernt der `Splitter` die direkten Ziele von Links, auch auf Seiten innerhalb desselben Teils. Ein Inhaltsverzeichnis im Teil ist danach nicht mehr anklickbar, der Text bleibt erhalten.
- Aktionen, die auf eine Seite außerhalb des Teils zeigen, bleiben ohne Zielseite stehen und bewirken nichts mehr. JavaScript-Aktionen, die Seiten über ihre Nummer ansprechen, werden nicht geändert.
- Formularfelder sind im Teil nicht mehr bearbeitbar, weil das Formular des Dokuments nicht übernommen wird (wie bisher). Das Widget auf der übernommenen Seite und seine Darstellung bleiben erhalten.
- Benutzt eine behaltene Seite dieselben Ressourcen wie eine andere Seite (z.B. ein gemeinsames Bild), werden diese mit in den Teil geschrieben, weil die behaltene Seite sie braucht.

Closes #3507

🤖 Generated with [Claude Code](https://claude.com/claude-code)
