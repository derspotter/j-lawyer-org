# Report #2151: Etiketten-Einstellungen ohne Berechtigung

- Branch: `fix/2151-tag-settings-permission-message` (Basis: upstream/master 7b3b3dfa8)
- Commit (Head): `8169e65abb7f4dd523800cc036ed7c894d57f4d0`
- Status: Code fertig. Offen: Autor auf derspotter umschreiben (Force-Push in der Cloud-Session abgelehnt), siehe Nachtrag.

## Nachtrag 01.11.2026 (Codex-Review, Autor)
- Autor auf `derspotter <jayjag@posteo.de>` umschreiben (amend + `git push --force-with-lease`): in der Cloud-Session vom Auto-Mode-Klassifikator als destruktive Git-Aktion abgelehnt. Nicht umgangen. Der Branch hat deshalb noch den Commit mit Autor Claude.
- Neu: Folge-Commit `8169e65` (Autor derspotter, Co-Authored-By Claude) begrenzt die Suche in `isPermissionDenied()` auf 20 Ebenen der Ursachenkette und testet eine zyklische Kette (`UserUtilsTest` jetzt 5 Tests). Fast-forward gepusht. Neu gebaut mit `clean package`: Client 86 Tests, 0 Fehler, fehlschlagend nur FtpTest/SftpTest/SmbTest (17 von 104 in j-lawyer-server-common), wie auf master. Ohne die Begrenzung würde der neue Test bis zum Timeout hängen.
- Der erste Commit `b6cbd7e` hat weiter Autor Claude.
- pr.md: `## Verifiziert` nennt jetzt den genauen Befehl inkl. `-Dmaven.test.failure.ignore=true` und Compiler-Overrides, das JDK-Setup und dass der Build nur wegen ignorierter Testfehler durchläuft (einzige fehlschlagende Klassen FtpTest/SftpTest/SmbTest wie auf master). pr.md `## Fix` nennt die Begrenzung.

### Für das Umschreiben (lokal im Orchestrator)
Beide Commits zu einem zusammenfassen, Autor derspotter, Nachricht:

```
fix(settings): verständliche Meldung bei fehlender Berechtigung in den Etiketten-Einstellungen. Closes #2151

Lehnt der Server das Anlegen, Löschen oder Umbenennen eines Eintrags im
Konfigurationsdialog (z.B. Akten-/Dokumenten-Etiketten) wegen einer
fehlenden Rolle ab (EJBAccessException), zeigt der Dialog jetzt einen
Hinweis auf die fehlende Berechtigung statt der technischen
Servermeldung (WFLYEJB0364 ...). Andere Fehler behalten ihre Meldung.
Die Suche nach der EJBAccessException in der Ursachenkette ist auf 20
Ebenen begrenzt, damit eine zyklische Kette nicht endlos läuft.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
```

## Dateien (Stand Head, beide Commits)
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/JKanzleiGUI.properties` +1 -0
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/JKanzleiGUI_en.properties` +1 -0
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/configuration/OptionGroupAddressTagRename.java` +2 -1
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/configuration/OptionGroupCaseDocumentTagRename.java` +2 -1
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/configuration/OptionGroupCaseTagRename.java` +2 -1
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/configuration/OptionGroupConfigurationDialog.java` +4 -3
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/utils/UserUtils.java` +35 -0
- `j-lawyer-client/src/test/java/org/jlawyer/test/client/utils/UserUtilsTest.java` +719 -0

Gesamt: 8 files changed, 766 insertions(+), 6 deletions(-). Davon sind 663 Zeilen der AGPL-Lizenzkopf von `UserUtilsTest.java`, wie bei den anderen Tests in `org/jlawyer/test/client/utils`.

## Build und Tests
Befehl: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 clean package`

- Baseline (upstream/master): Build läuft durch (Testfehler ignoriert). Fehlschlagende Testklassen: FtpTest, SftpTest, SmbTest (17 Failures von 104 in j-lawyer-server-common). Client: 81 Tests, 0 Fehler.
- Head `8169e65`: Build läuft durch (Testfehler ignoriert). Gleiche fehlschlagende Klassen. Client: 86 Tests, 0 Fehler (neu: UserUtilsTest, 5 Tests).

JDK: Ubuntu OpenJDK 17.0.20 plus OpenJFX 17.0.9 (Maven Central) per jlink zu einem Image zusammengefügt, weil BellSofts Hosts im Cloud-Proxy gesperrt sind. Mit `--release 17` sieht javac die per jlink ergänzten JavaFX-Module nicht, deshalb `-Dmaven.compiler.release=` mit source/target 17. Keine Änderung an pom.xml.

## Nicht verifiziert
- Kein laufender Client oder Server. Dass die Ablehnung als `EJBAccessException` ankommt, ist aus `@RolesAllowed` und dem bestehenden catch im LoginDialog abgeleitet, nicht live geprüft.
- Screenshot im Issue nicht einsehbar (GitHub-Bild-Host im Proxy gesperrt).

## Offene Fragen
- Soll der Dialog die Rollen vorab prüfen (`createOptionGroupRole`, `deleteOptionGroupRole`) und Aktionen sperren? Das würde auch den halben Umbenennen-Fall vermeiden (alter Eintrag gelöscht, neuer nicht angelegt, wenn nur die Anlege-Rolle fehlt). Bewusst nicht gemacht: der Rollen-Cache im Client kann veraltet sein, und es wäre mehr Verhaltensänderung.
- `MultiValueTagConfigurationDialog` (Listenetiketten) hat dasselbe Muster (`ex.getMessage()`). Kandidat für einen Folge-PR mit derselben Hilfsmethode.
