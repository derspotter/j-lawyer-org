fix(settings): verständliche Meldung bei fehlender Berechtigung in den Etiketten-Einstellungen

## Problem
Wer unter Einstellungen > Akten > Akten-Etiketten oder Einstellungen > Dokumente > Dokumenten-Etiketten ohne die nötige Berechtigung einen Eintrag anlegt, löscht oder umbenennt, bekommt die technische Servermeldung zu sehen, etwa `WFLYEJB0364: Invocation on method: ... of bean: SystemManagement is not allowed`. Je nach Aktion ändert sich der Text.

## Ursache
Die Servermethoden sind per `@RolesAllowed` geschützt: `createOptionGroup` mit `createOptionGroupRole`, `removeOptionGroup` mit `deleteOptionGroupRole`. Das Umbenennen an vorhandenen Akten, Dokumenten und Adressen braucht zusätzlich `writeArchiveFileRole` bzw. `writeAddressRole`. Fehlt die Rolle, lehnt der Server den Aufruf mit einer `EJBAccessException` ab. `OptionGroupConfigurationDialog` und die Umbenennen-Handler zeigen dann einfach `ex.getMessage()` an.

## Fix
- `UserUtils.isPermissionDenied(Throwable)` erkennt eine `EJBAccessException`, auch wenn sie als Ursache in einer anderen Exception steckt.
- `UserUtils.getServerErrorMessage(Throwable)` liefert in diesem Fall den neuen Text `msg.permissionrequired` aus `JKanzleiGUI.properties` (de und en): "Für diese Aktion fehlt Ihnen die erforderliche Berechtigung. Wenden Sie sich gegebenenfalls an Ihren Administrator." Bei allen anderen Fehlern bleibt es bei `ex.getMessage()`.
- `OptionGroupConfigurationDialog` (Hinzufügen, Löschen, Umbenennen) und die Umbenennen-Handler `OptionGroupCaseTagRename`, `OptionGroupCaseDocumentTagRename` und `OptionGroupAddressTagRename` verwenden diese Meldung.

Der Dialog wird auch für die anderen einfachen Wertelisten genutzt (z.B. Anreden, Titel, Adress-Etiketten). Dort erscheint der Hinweis genauso. Keine Änderung an `.form`-Dateien, nur an den Rümpfen der Event-Handler.

## Verifiziert
- Neuer `UserUtilsTest` mit 4 Tests: eine direkte und eine verschachtelte `EJBAccessException` ergeben den Hinweistext, andere Fehler behalten ihre Meldung, `null` gilt nicht als fehlende Berechtigung.
- `mvn -pl j-lawyer-client -am package` mit Tests: BUILD SUCCESS. Es scheitern nur FtpTest, SftpTest und SmbTest wie auf master (brauchen externe Zugangsdaten). Client-Tests 85 statt 81, alle grün.

## Nicht abgedeckt
- Nicht mit einem laufenden Client gegen einen Server mit eingeschränktem Nutzer ausprobiert. Dass die Ablehnung als `EJBAccessException` beim Client ankommt, folgt aus `@RolesAllowed` und wird im `LoginDialog` schon genauso abgefangen, ist hier aber nicht live geprüft.
- Die Rollen werden nicht vorab geprüft. Die Aktion wird weiter versucht, nur die Meldung ist jetzt verständlich. Beim Umbenennen löscht der Dialog den alten Eintrag und legt danach den neuen an. Fehlt nur `createOptionGroupRole`, ist der alte Eintrag danach weg. Das war vorher schon so und ist hier unverändert.
- Der Dialog für Listenetiketten (`MultiValueTagConfigurationDialog`) zeigt weiterhin die Servermeldung. Das wäre ein eigener kleiner Folge-PR.

Closes #2151

🤖 Generated with [Claude Code](https://claude.com/claude-code)
