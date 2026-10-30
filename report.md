# Report #2151: Etiketten-Einstellungen ohne Berechtigung

- Branch: `fix/2151-tag-settings-permission-message` (Basis: upstream/master 7b3b3dfa8)
- Commit: `b6cbd7ecc1b95324e5b188683366c48747aa99c6`
- Status: fertig, bereit für Codex-Review

## Dateien
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/JKanzleiGUI.properties` +1 -0
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/JKanzleiGUI_en.properties` +1 -0
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/configuration/OptionGroupAddressTagRename.java` +2 -1
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/configuration/OptionGroupCaseDocumentTagRename.java` +2 -1
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/configuration/OptionGroupCaseTagRename.java` +2 -1
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/configuration/OptionGroupConfigurationDialog.java` +4 -3
- `j-lawyer-client/src/main/java/com/jdimension/jlawyer/client/utils/UserUtils.java` +34 -0
- `j-lawyer-client/src/test/java/org/jlawyer/test/client/utils/UserUtilsTest.java` +710 -0

Gesamt: 8 files changed, 756 insertions(+), 6 deletions(-). Davon sind 663 Zeilen der AGPL-Lizenzkopf von `UserUtilsTest.java`, wie bei den anderen Tests in `org/jlawyer/test/client/utils`. Ohne Kopf sind es 47 Zeilen Test.

## Build und Tests
Befehl: `mvn -B -ntp -pl j-lawyer-client -am -Dmaven.test.failure.ignore=true -Dmaven.compiler.release= -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 package`

- Baseline (upstream/master): BUILD SUCCESS. Fehlschlagende Testklassen: FtpTest, SftpTest, SmbTest (17 Failures in j-lawyer-server-common). Client: 81 Tests, 0 Fehler.
- Mit Fix: BUILD SUCCESS. Gleiche fehlschlagende Klassen (FtpTest, SftpTest, SmbTest). Client: 85 Tests, 0 Fehler (neu: UserUtilsTest, 4 Tests).

Umgebungshinweis: BellSofts Download-Hosts sind im Cloud-Proxy gesperrt. JDK ist Ubuntu OpenJDK 17.0.20 plus OpenJFX 17.0.9 (Maven Central) per jlink zu einem Image zusammengefügt. Mit `--release 17` sieht javac die per jlink ergänzten JavaFX-Module nicht (ct.sym kennt sie nicht, Liberica Full bringt sie mit). Deshalb `-Dmaven.compiler.release=` mit source/target 17. Das JDK selbst ist 17, also gleichwertig. Keine Änderung an pom.xml.

## Nicht verifiziert
- Kein laufender Client oder Server. Dass die Ablehnung als `EJBAccessException` ankommt, ist aus `@RolesAllowed` und dem bestehenden catch im LoginDialog abgeleitet, nicht live geprüft.
- Screenshot im Issue nicht einsehbar (GitHub-Bild-Host im Proxy gesperrt).

## Offene Fragen
- Soll der Dialog die Rollen vorab prüfen (`createOptionGroupRole`, `deleteOptionGroupRole`) und Aktionen sperren? Das würde auch den halben Umbenennen-Fall vermeiden (alter Eintrag gelöscht, neuer nicht angelegt, wenn nur die Anlege-Rolle fehlt). Bewusst nicht gemacht: der Rollen-Cache im Client kann veraltet sein, und es wäre mehr Verhaltensänderung.
- `MultiValueTagConfigurationDialog` (Listenetiketten) hat dasselbe Muster (`ex.getMessage()`). Kandidat für einen Folge-PR mit derselben Hilfsmethode.
- Commit-Autor ist die Git-Identität der Cloud-Session (Claude), mit Co-Authored-By-Zeile.
