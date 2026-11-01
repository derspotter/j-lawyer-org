fix(addresses): rechter Rand im Kontakt-Editor wie links

## Problem
Im Kontakt-Editor sitzen oben rechts "unverschlüsselt" und der Verschlüsselungs-Button direkt am Fensterrand und wirken abgeschnitten (gemeldet unter Windows 10 mit 105 Prozent Skalierung).

## Ursache
Die äußere horizontale Gruppe in `AddressPanel.initComponents()` hat links einen Container-Abstand (`addContainerGap()`), rechts aber keinen. Im `.form` fehlt entsprechend das abschließende `EmptySpace`. Die Kopfzeile (`jPanel23` mit `chkEncryption` und `lblEncryption`, beide rechtsbündig), die Reiter und `lblHeaderInfo` reichen dadurch bis an den rechten Rand. Die vertikale Gruppe hat oben und unten einen Container-Abstand, `ArchiveFilePanel` hat ihn auch rechts.

## Fix
Rechten Container-Abstand ergänzt, in `AddressPanel.form` (`<EmptySpace max="-2" attributes="0"/>` am Ende der sequentiellen Gruppe) und gleichlautend im generierten Code (`.addContainerGap()`), so wie NetBeans es erzeugt und wie es in `ArchiveFilePanel` steht. Damit bekommen auch die Reiter rechts denselben Abstand wie links.

## Verifiziert
- `GroupLayoutCompletenessTest` ist grün. `.form` ist wohlgeformtes XML.
- Scratch-Test (nicht committet) mit derselben äußeren Gruppe aus `org.jdesktop.layout` und Platzhalter-Komponenten, headless mit Standard-Look-and-Feel: vorher 12 px links und 0 px rechts, nachher 12 px auf beiden Seiten.
- `git merge-tree` gegen `fix/1283-new-contact-after-tag-save` und gegen `fix/3511-double-right-click` (PR #3693), die `AddressPanel.java` ebenfalls ändern: beide ohne Konflikt.
- Build: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`
- JDK: Ubuntu OpenJDK 17.0.20 und OpenJFX 17.0.9 aus Maven Central, per jlink zu einem JDK zusammengefügt, weil die BellSoft-Hosts im Cloud-Proxy gesperrt waren. Mit diesem JDK sieht `--release 17` die JavaFX-Module nicht, deshalb `source`/`target` 17 statt `release`.
- Ergebnis: Der Build läuft durch, weil Testfehler mit `-Dmaven.test.failure.ignore=true` ignoriert werden. Fehlschlagende Testklassen sind nur FtpTest, SftpTest und SmbTest in j-lawyer-server-common (sie brauchen FTP-, SFTP- und SMB-Server), genau wie auf master. Client: 81 Tests, 0 Fehler (master: 81).

## Nicht abgedeckt
- Nicht im laufenden Client angesehen, nicht unter Windows mit 105 Prozent Skalierung und nicht im NetBeans GUI Builder geöffnet. Der echte Abstand hängt vom Look and Feel ab (FlatLaf im Client), der Scratch-Test zeigt nur, dass rechts jetzt derselbe Container-Abstand wie links entsteht.
- Screenshot im Issue nicht einsehbar (Bild-Host im Proxy gesperrt).

Closes #2111

🤖 Generated with [Claude Code](https://claude.com/claude-code)
