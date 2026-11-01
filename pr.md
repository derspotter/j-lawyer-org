fix(server): RAM-Überwachung zählt den Seitencache unter Linux nicht mehr als belegt

## Problem
Unter Linux zeigt die Serverüberwachung kurz nach dem Start fast den gesamten Arbeitsspeicher als belegt an (im Issue 7614 MB von 7767 MB, laut System sind 27% in Gebrauch). Dadurch kommen ständig Warnungen, auch bei Warnschwellen von 98 oder 99%.

## Ursache
`SystemMonitorTask.getPhysicalMemStatusLevel()` (Warnungen) und `SystemManagement.getMonitoringSnapshot()` (Anzeige) nehmen als freien Speicher `OperatingSystemMXBean.getFreePhysicalMemorySize()`. Unter Linux ist das nur der völlig unbenutzte Speicher. Puffer und Seitencache, die das System bei Bedarf sofort freigibt, zählen damit als belegt. Auf einem Linux-Server ist dieser Wert nach kurzer Zeit nahe null. Das ist der „buffered“ RAM aus dem Kommentar von j-dimension.

## Fix
Neue Hilfsmethode `SystemMemoryUtils.getAvailablePhysicalMemory()` in j-lawyer-server-common, die beide Stellen nutzen. Unter Linux liefert sie `MemAvailable` aus `/proc/meminfo`, also den tatsächlich verfügbaren Speicher inklusive freigebbarem Cache. Ohne `/proc/meminfo` (Windows, macOS), ohne `MemAvailable` (Kernel vor 3.14) oder wenn `/proc/meminfo` eine andere Speichergröße beschreibt als die JVM sieht, z.B. in einem Container mit Speicherlimit, bleibt es beim bisherigen Wert der JVM. Die Swap-Werte bleiben unverändert.

## Verifiziert
- Neuer Test `SystemMemoryUtilsTest` (j-lawyer-server-common): `MemAvailable` wird genutzt, `/proc/meminfo` eines anderen Systems (Container mit Limit) und fehlendes `MemAvailable` führen zum Rückfall auf den JVM-Wert, unlesbare Zeilen werden ignoriert.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build: `mvn -B -ntp -pl j-lawyer-server/j-lawyer-server-ejb,j-lawyer-server/j-lawyer-server-war -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Alle Module des Reactors bauen. Tests: 1025 ausgeführt (master plus 4 neue), 22 fehlgeschlagen. Fehlgeschlagen sind nur FtpTest, SftpTest und SmbTest in j-lawyer-server-common (brauchen echte FTP-, SFTP- und SMB-Server) sowie `LibreOfficeODFTest.setReplaceEmptyLine` in j-lawyer-server-ejb (bekannter Fehler auf master) sowie BackupExecutorTest, FileNameEncodingTest und SyncTest in j-lawyer-server-war (brauchen eine lokale Datenbank jlawyerdb für mysqldump). Das sind dieselben Klassen und Testfälle wie auf master 7b3b3dfa8, gebaut mit demselben Befehl im selben Setup.

## Nicht abgedeckt
- In einem Container mit Speicherlimit (cgroup) bleibt es beim bisherigen Verhalten. Dort zählt die JVM den Cache des Containers ebenfalls als belegt, im Test in einer solchen Umgebung meldete sie 0 MB frei bei 13677 MB Limit. Das ließe sich nur über die cgroup-Werte lösen und ist nicht Teil dieses PRs. Im Issue (7767 MB gesamt, also kein Limit) und in Docker ohne Speicherlimit sehen JVM und `/proc/meminfo` denselben Speicher, dort greift die neue Berechnung.
- Kein Test auf einem laufenden j-lawyer-Server.

Closes #1779

🤖 Generated with [Claude Code](https://claude.com/claude-code)
