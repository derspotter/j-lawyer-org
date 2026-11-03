fix(server): RAM-Überwachung zählt den Seitencache unter Linux nicht mehr als belegt

## Problem
Unter Linux zeigt die Serverüberwachung kurz nach dem Start fast den gesamten Arbeitsspeicher als belegt an (im Issue 7614 MB von 7767 MB, laut System sind 27% in Gebrauch). Dadurch kommen ständig Warnungen, auch bei Warnschwellen von 98 oder 99%.

## Ursache
`SystemMonitorTask.getPhysicalMemStatusLevel()` (Warnungen) und `SystemManagement.getMonitoringSnapshot()` (Anzeige) nehmen als freien Speicher `OperatingSystemMXBean.getFreePhysicalMemorySize()`. Unter Linux ist das nur der völlig unbenutzte Speicher. Puffer und Seitencache, die das System bei Bedarf sofort freigibt, zählen damit als belegt. Auf einem Linux-Server ist dieser Wert nach kurzer Zeit nahe null. Das ist der „buffered“ RAM aus dem Kommentar von j-dimension.

## Fix
Neue Hilfsmethode `SystemMemoryUtils.getAvailablePhysicalMemory()` in j-lawyer-server-common, die beide Stellen nutzen. Unter Linux liefert sie `MemAvailable` aus `/proc/meminfo`, also den tatsächlich verfügbaren Speicher inklusive freigebbarem Cache. In allen anderen Fällen bleibt es beim bisherigen Wert der JVM: ohne `/proc/meminfo` (Windows, macOS), ohne Lesezugriff darauf, ohne `MemAvailable` (Kernel vor 3.14) oder wenn `MemTotal` in `/proc/meminfo` nicht genau der Gesamtgröße entspricht, die die JVM sieht, z.B. in einem Container mit einem Speicherlimit unter dem Speicher des Hosts, auch wenn das Limit nur knapp darunter liegt. Ohne ein Limit, das die JVM erkennt, liefert sie unter Linux `sysconf(_SC_PHYS_PAGES)` mal Seitengröße, das ist bytegenau `MemTotal`. Erkennt sie ein Limit unter dem Speicher des Hosts, liefert sie das Limit. Ein Limit in Höhe des Hostspeichers oder darüber behandelt die JVM wie kein Limit, dann stimmen beide Werte wieder überein und `MemAvailable` des Hosts wird verwendet. Die Swap-Werte bleiben unverändert.

## Verifiziert
- Test `SystemMemoryUtilsTest` (j-lawyer-server-common) mit 9 Fällen: Auswertung von `/proc/meminfo` (MemAvailable wird genutzt, andere Speichergröße, Container-Limit innerhalb von 1 Prozent unter und über `MemTotal`, fehlendes MemAvailable, unlesbare Zeilen) und Rückfall auf den Wert der JVM mit einer nachgebildeten `OperatingSystemMXBean`, wenn die Datei fehlt, nicht lesbar ist oder ein anderes System beschreibt.
- JDK: Ubuntu OpenJDK 17.0.20 mit OpenJFX 17.0.9 (Jars von Maven Central, per `jlink` zu einem JDK mit JavaFX-Modulen zusammengefügt), Maven 3.9.11. `maven.compiler.release` ist leer gesetzt, weil `--release` die JavaFX-Module eines solchen JDK nicht sieht.
- Build dieses Branches (Stand 416422573, gestartet 03.11.2026 19:17 UTC) und Vergleichsbuild von master 7b3b3dfa8 (02.11.2026) mit genau demselben Befehl im selben Setup: `mvn -B -ntp -pl j-lawyer-server/j-lawyer-server-ejb,j-lawyer-server/j-lawyer-server-war -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- Der Befehl baut die gewählten Module samt ihren Abhängigkeiten im Reactor, alle davon bauen. Tests auf diesem Branch: 1030 gesamt, davon 1014 ausgeführt und 16 übersprungen, 22 fehlgeschlagen. Auf master: 1021 gesamt, 1005 ausgeführt, 16 übersprungen, 22 fehlgeschlagen. Neu sind 9 Tests.
- Die fehlgeschlagenen Tests sind genau dieselben wie auf master: FtpTest und SftpTest in j-lawyer-server-common brauchen einen echten FTP- bzw. SFTP-Server mit Zugangsdaten aus Umgebungsvariablen. SmbTest braucht die im Test fest eingetragene SMB-Freigabe smb://j-lawyer-box/j-lawyer-share/. `LibreOfficeODFTest.setReplaceEmptyLine` in j-lawyer-server-ejb ist ein bekannter Fehler auf master (behoben durch den offenen PR #3703). `BackupExecutorTest` in j-lawyer-server-war braucht lokale Verzeichnisse unter /home/jens/dev/projects und eine lokale MySQL-Datenbank für mysqldump. `FileNameEncodingTest` braucht lokale Vorlagendateien unter /home/jens/dev/projects/j-lawyer-data. `SyncTest.testSync` braucht lokale Sicherungen unter /home/jens/dev/projects/backups und einen SFTP-Zugang.

## Nicht abgedeckt
- Nicht auf einem laufenden j-lawyer-Server ausprobiert.
- In einem Container mit einem Speicherlimit (cgroup) unter dem Speicher des Hosts bleibt es beim bisherigen Verhalten. Dort zählt die JVM den Cache des Containers ebenfalls als belegt, im Test in einer solchen Umgebung meldete sie 0 MB frei bei 13677 MB Limit. Das ließe sich nur über die cgroup-Werte lösen und ist nicht Teil dieses PRs. Ohne Speicherlimit, z.B. in Docker ohne Limit, sehen JVM und `/proc/meminfo` denselben Speicher, dort greift die neue Berechnung. Ob beim Melder des Issues (7767 MB gesamt) ein Limit gesetzt ist, ist nicht bekannt.

Closes #1779

🤖 Generated with [Claude Code](https://claude.com/claude-code)
