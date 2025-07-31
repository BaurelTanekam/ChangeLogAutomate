# Release1.0.1
## [1.0.1] - 2025-07-31

###Changed
- [MSPINTERN-2729] updated method to handling error and empty changelog file

## [1.0.1] - 2025-07-31

###Added
- [MSPINTERN-2729] fixed bug, methode updated, tag added
- [MSPINTERN-2729] fix bug, methode updated, tag added
- [MSPINTERN-2729] Maven dependencies added
- [MSPINTERN-2729] jira package implemented with several methods
- [MSPINTERN-2729] updated, fixed, implemented
- [MSPINTERN-2729] toString Method in JiraComment class updtaed, pintComment in JiraService class implemented and parseDateTime method in JiraIssueFetcher class implemented
- [MSPINTERN-2729] incrementVersionTaginChangeLog method implemented
- [MSPINTERN-2729] two Methods implemented, addReleaseVersionToChangeLog and createAndPushTag added
- [MSPINTERN-2729] method stripPrefix for a tagName,method to add comment in Changelog file implemented

###Changed
- [MSPINTERN-2729] Jira Package created with Classes
- [MSPINTERN-2729] Maven dependecy Json updated

## [1.0.1] - 2025-07-30

###Added
- [MSPINTERN-2729] fixed bug, methode updated, tag added
- [MSPINTERN-2729] fix bug, methode updated, tag added
- [MSPINTERN-2729] Maven dependencies added
- [MSPINTERN-2729] jira package implemented with several methods
- [MSPINTERN-2729] updated, fixed, implemented
- [MSPINTERN-2729] toString Method in JiraComment class updtaed, pintComment in JiraService class implemented and parseDateTime method in JiraIssueFetcher class implemented
- [MSPINTERN-2729] incrementVersionTaginChangeLog method implemented
- [MSPINTERN-2729] two Methods implemented, addReleaseVersionToChangeLog and createAndPushTag added
- [MSPINTERN-2729] method stripPrefix for a tagName,method to add comment in Changelog file implemented

###Changed
- [MSPINTERN-2729] Jira Package created with Classes
- [MSPINTERN-2729] Maven dependecy Json updated

## [1.0.2] - 2025-07-30

###Added
- [MSPINTERN-2729] method stripPrefix for a tagName,method to add comment in Changelog file implemented


## Jira Comments- Hallo Johanna, magst Du bitte die Beschreibung prüfen und bei Bedarf korrigieren oder ergänzen? Gibt mir das Ticket danach bitte zurück.  
- Recherche und Einschätzungen

*Möglicher Automatisierungsgrad*
Es dürfte Lösungsmöglichkeiten geben in einem Spektrum zwischen kleinen Unterstützungen einzelner Teilaufgaben bis zu einer 
vollautomatischen Lösung, bei der lediglich die LineUp-Dokumente an einem bestimmten Ort abgelegt werden müssen und wenig später stehen die fertigen Dokumente in den benötigten Varianten zur Verwendung bereit.

*Betrachtung der Teilaufgaben*

+Übersetzen unter Verwendung des Glossars+
Automatisches Übersetzen eines Texts unter Berücksichtigung eines Glossars ist durch Einsatz von KIs mit einem passend vorbereiteten Prompt möglich. Aufwand ensteht für die Bereitstellung der Infrastruktur und das Ermitteln des präzisen Prompts für die gewünschten Ergebnisse. Der Glossar müsste dem Prompt
mitgegeben werden. Denkbare Lösungen wären z.B.
- ein Prompt im KI-Genie
- eine programmatische Umsetzung, wie die ChatGPT-Schnittstelle im ServiceDesk-Assistenten
- Power Automate: die CoPilot Anfrage "Kann Power Automate Word-Dokumente übersetzen und in ein zweisprachiges Dokument umwandeln?" liefert eine Anleitung. Diese wäre zu prüfen. Siehe dazu auch die weiteren Punkte. 

+Automatisches Austauschen der Formatvorlage in einem Word-Dokument+ 
Die Formatvorlage in einem Word-Dokument ist lediglich ein Attribut, welches einfach programmatisch 
geändert werden kann. Ein VB-Code Beispiel dafür sähe so aus (Quelle: CoPilot)
{code}
        Set doc = Documents.Open(folderPath & file)
        doc.AttachedTemplate = templatePath
        doc.UpdateStylesOnOpen = True
        doc.Save
        doc.Close
{code}
Auch dafür gibt es laut CoPilot in Power Automate einen fertigen Baustein. Das dürfte also in allen Ausführungen trivial sein.	

+Einarbeiten der Zweispaltigkeit und Einfügen der Übersetzungen in die zweite Spalte unter Beibehaltung der Grafiken und Tabellen+
Dies dürfte die größte Herausforderung werden, da es sicherlich keine fertige Bibliotheksfunktionen oder Power Automate Baustein dafür geben wird. "Zweispaltig machen" wird es geben, aber dann wird der Textfluss schlicht in zwei Spalten umgebrochen, was nicht dem entspricht, was wir brauchen. Mit Hilfe von Bibliotheken, die aus dem Worddokumenten eine baumartige programmatisch manipulierbare Datenstruktur (z.B. XML) machen, so wären darin lediglich die Text-Knoten 
zu identifizieren, die Texte zu übersetzen und für jeden gefundenen Knoten ein weiterer daneben mit der Übersetzung einzufügen. Für verbreitete Hochsprachen, wie PHP und Python sind Bibliotheken zur Verarbeitung von Word verfügbar, aber ich habe noch mit keiner davon gearbeitet. Dabei ist auch zu bedenken, dass Word einen Feature-Reichtum an Formatierungen zulässt. Wenn der Aufwand nicht ausarten soll, können wir nur einen sehr kleinen Teil davon berücksichtigen. Wir brauchen zwar auch nur einen kleinen Teil, aber wir werden trotz intensiver Tests nicht ausschließen können, dass vereinzelt oder durch hinzukommende Verwendungen von Features durch LineUp Nacharbeiten an den Dokumenten oder Prozessen notwendig werden.  

+Austausch von Logo und Signatur+
Dies ist nur sicher möglich, wenn die gelieferten LineUp-Dokumente einheitlich genug sind, und die auszutauschenden Elemente in der Dokumentstruktur programmatisch eindeutig identifiziert werden können. Wenn dies sicher gestellt ist, so ist der Austausch trivial.

+Versionierung+
Die Versionierung wäre erst auf den fertigen Dokumenten bei Änderungen notwendig. Daher wäre dies voraussichtlich kein Bestandteil des Konvertierungsprozesses. Es könnte also, wie bisher, manuell unter Verwendung der Word-Versionierungsfunktionen erfolgen. Alterativ wäre die Speicherung in einem git-Repository möglich. git macht die Versionierung automatisch, funktioniert aber nicht gut auf binären Dokumenten, wie .docx. CoPilot empfiehlt dazu das Definieren eines gitattributes
{code}
*.docx diff=pandoc
{code}
Alternativ könnte das Word-Dokument in einem Textformat gespeichert werden, wie XML. Das aus Word-Dokumenten erzeugte XML ist aber bekanntermaßen wenig übersichtlich und nachbearbeitungsfreundlich. 
Da dies keine MUSS-Anforderung ist, würde ich hier vorerst keine Energie investieren.

+Projektstruktur im Teams+
Diese wird voraussichtlich so bleiben können, wie sie ist. Die Verzeichnisse und Dateien wären weiterhin manuell zu verwalten. Denkbar wäre ein automatischer Upload der Ergebnisse der Dokument-Konvertierung. Dazu müsste der Konvertierungsprozess eine Berechtigung auf das Verzeichnis erhalten und für die Übertragung wäre laut CoPilot die Graph-API oder SharePoint notwendig. Power Automate soll diese Funktion beinhalten. In dem Fall wäre aber zu bedenken, dass der Power Automate-Flow Zugriff auf das Verzeichnis haben muss (siehe Datenschutz).

*Gedanken zu Power Automate*
Beim Einsatz von Power Auomate ist zu bedenken, dass dies ein Entwicklungswerkzeug ist, für das wir, wie auch für SAP/ABAP und Webserver/PHP/SQL Expertise, Entwicklungsrichtlinien und Infrastrukturen brauchen und für die Dauer des Einsatzes pflegen müssen. 
Des Weiteren kann ich aus Erfahrung hinzufügen, dass Flow-Charts nur bis zu einer gewissen Größe und Komplexität übersichtlich und gut wartbar sind. Für einen schnellen Prototypen oder nur eine Teilautomatisierungen aus Standardbausteinen dürfte es aber grundsätzlich gut geeignet sein.

*Datenschutz*
Es ist zu bedenken, dass Daten aus den Verträgen an verschiedene Schnittstellen übertragen werden bzw. die vollständigen Vertragswerke für die Verarbeitung in Power Automate übertragen werden. Die Anbieter dieser Dienste können diese Informationen also einsehen. Das ist natürlich auch jetzt schon im Fall von DeepL der Fall.

*CRA*
Das CRA fordert von Vertriebspartnern von Herstellern, dass diese stellvertretend für die Hersteller die Anforderungen des CRA zu erfüllen haben. Es ist zu prüfen, ob dies bei unserer Kooperation mit LineUp der Fall ist. Das würde voraussichtlich hinsichtlich der Dokumente bedeuten, dass wir zukünftig weitere Dokumente von LineUp erhalten und geeignet an unsere Kunden weitergeben müssen.
- Moin [~nschadow], bei der Beschreibung habe ich nur ein paar Kleinigkeiten angepasst, ansonsten ist das so vollständig. 
- Danke, [~msp.j.neumann]!
- Hallo Johanna, magst Du bitte ein paar Originaldokumente von Lineup hier an das Ticket hängen, die Nazar und Baurel zur Entwicklung benutzen können und zusätzlich ein oder mehrere fertige Dokumente, damit wir eine klare Zielvorgabe zum Vergleichen haben?
- Moin Nico, ich habe mit den beiden gesprochen und die beigefügten Dateien erklärt. Sie schauen es sich mal an.
- [~msp.j.sievering], perfekt, vielen Dank!
- ChatGpt-API-Dokumentation:
https://platform.openai.com/docs/api-reference/making-requests

Einen Token für die ersten Tests habe ich [~msp.b.tanekam] und [~msp.n.buzyl] soeben als PrivateBin-Link geschickt.

- Hallo [~msp.j.sievering] ,

Mir fehlt jetzt nur noch deine Einschätzung. Wir haben ChatGPT für die Textübersetzung integriert, und dort gibt es eine Einstellungsoption für den „Grad der Menschlichkeit“ (temperature) – also wie detailliert die Übersetzung erfolgt. Ich habe eine Datei ([^Vergleich_AI_Uebersetzung.docx]) erstellt, in der ich den Originaltext sowie die Übersetzungen mit verschiedenen Stufen bereitgestellt habe (die direkt nebeneinander stehenden Versionen sind sich sehr ähnlich oder können sogar identisch sein).

Magst du bitte diese Versionen bewerten und mir mitteilen, welcher Grad am besten geeignet wäre. Falls du einen bestimmten Textabschnitt testen möchtest, lass es mich bitte wissen.

Außerdem habe ich die übersetzte Datei ([^Englisches Original_EN-DE_Java.docx]) mit Grad 0.2 angehängt. Magst du bitte das zu überprüfen, besonders auf Tabellen und das Format zu achten – ob dir diese Umwandlung passt. Die Datei wurde direkt nach der Generierung durch das Programm versendet, ohne jegliche Nachbearbeitung.

Du kannst auch darauf achten, dass in einigen übersetzten Abschnitten der Stil verändert wurde. Ich konnte bisher nicht herausfinden, woran das liegt. Falls es hilft, kann ich ein Standardstil setzen oder die Formatierung in Word manuell anpassen.

Falls du die Umwandlung noch detaillierter prüfen möchtest, kannst du auch weitere Dokumente schicken, mit denen du das Programm testen möchtest.
- Hallo Johanna,
[Das Programm|https://github.com/MSP-Medien-Systempartner/lineup-documentation-converter/tree/nazarbuzyl-msp] ist praktisch vollständig fertig, es fehlen nur noch einige spezifische Klarstellungen von Ihrer Seite, die im Kommentar unten stehen.

Das Programm ermöglicht es, durch Angabe des Pfads zu einem Ordner/Datei, in dem sich die zu übersetzenden Dateien befinden, und eines zweiten Arguments, das den Speicherort der übersetzten Dateien angibt, alle Dateien zu übersetzen. Ein zusätzliches Argument für eine genauere Übersetzung kann der Pfad zu einem Glossar sein, in dem die genauen Übersetzungen für Fachbegriffe angegeben sind.
- Lieber [~msp.n.buzyl], das klingt ziemlich cool - wenn Ihr fertig seid, würde ich mir die Lösung gern eimmal vorstellen lassen. Magst Du dazu vielleicht einmal nach einem Termin schauen - gern zusammen mit Johanna, Iris, Baurel und Nico ...beste Grüße Heike 
- Moin zusammen, 

ich habe mir die Versionen angeschaut. Bei der Temperatur bin ich noch unschlüssig, stellenweise gefällt mit 0.3 oder auch 0.9 ganz gut, letztlich sind aber alle dazwischenliegenden auch in Ordnung. Eine Nacharbeitung des Dokuments ist in jeden Fall noch erforderlich um das Layout zu finalisieren, Logos, Kopf- und Fußzeilen anzupassen. Insgesamt würde es den Prozess aber auch jetzt schon erleichtern, da die viele Klickerei für das Zusammensetzen der Bausteine entfällt. Ich hänge euch probeweise mal noch eine weitere Datei an, diesmal nur das englische Original, vielleicht könnt ihr die auch nochmal probeweise durchspielen? 

Danke 
- Moin,
Ich habe das neue Dokument durch das Programm laufen lassen. Ich füge das neue übersetzende Dokument([^2024-10-31 - MAS - INTS-1382 Invoice Printing OUTBOUND v2.0_EN-DE.docx]) hier bei. Beim Durchsehen kann man einige Ungenauigkeiten im Format erkennen:
 * 1, 7.3: Einige Texte kann die KI nicht übersetzen oder hält sie nicht für notwendig zu übersetzen. 
 * 7.3: Einige Zeilen “verstecken” sich hinter anderen (Testsystem:, Server:…)
 * 8.1: das Programm liest die Formatvorlage so, wie sie vorgegeben ist, daher erscheinen blaue Bulletpoints.

Für dieses Dokument ist auch eine Temperatur von 0,3 eingestellt.
- Hi [~msp.n.buzyl], wie besprochen, hier nochmal ein komplexes Dokument zum testen. Die "versteckten" Zeilen machen mir etwas Sorgen, da diese nicht leicht zu lokalisieren sind. Das gerade angehängte Dokument hat auch nochmal Einzüge, hängende, Einzüge, eine Tabelle, eine XML.. schau mal bitte, wie das verarbeitet wird. Danke!
- Das neue Dokument wurde problemlos durch das Programm verarbeitet. Es gab keine Abweichungen wie “versteckte” Zeilen. [^2024-05-28 - MAS - INTS-1379 Procset Integration v1.2_EN-DE_v4.docx]
- [~msp.n.buzyl] ich habe nun 3 noch gar nicht bearbeitete Dokumente von LUP bekommen, die sind so im Original. Können wir die auch noch ausprobieren? Habe ich gerade angehangen. Danke!
- [~msp.j.sievering] die Dokumente wurden getestet. Ich hänge dann gleich alle übersetzten Dokumente an. Das Problem mit den "versteckten" Zeilen haben wir gelöst. Eine Bemerkung: Im Dokument ([^2024-10-31 - MAS - INTS-1382 Invoice Printing OUTBOUND v2.0_EN-DE_v2.0.docx] ist in einigen Fällen der Abstand zwischen den Absätzen nach der Bearbeitung zu groß, aber das kann leicht manuell gelöst werden, wenn die Formatvorlage geändert wird.

## Jira Comments- Hallo Johanna, magst Du bitte die Beschreibung prüfen und bei Bedarf korrigieren oder ergänzen? Gibt mir das Ticket danach bitte zurück.  
- Recherche und Einschätzungen

*Möglicher Automatisierungsgrad*
Es dürfte Lösungsmöglichkeiten geben in einem Spektrum zwischen kleinen Unterstützungen einzelner Teilaufgaben bis zu einer 
vollautomatischen Lösung, bei der lediglich die LineUp-Dokumente an einem bestimmten Ort abgelegt werden müssen und wenig später stehen die fertigen Dokumente in den benötigten Varianten zur Verwendung bereit.

*Betrachtung der Teilaufgaben*

+Übersetzen unter Verwendung des Glossars+
Automatisches Übersetzen eines Texts unter Berücksichtigung eines Glossars ist durch Einsatz von KIs mit einem passend vorbereiteten Prompt möglich. Aufwand ensteht für die Bereitstellung der Infrastruktur und das Ermitteln des präzisen Prompts für die gewünschten Ergebnisse. Der Glossar müsste dem Prompt
mitgegeben werden. Denkbare Lösungen wären z.B.
- ein Prompt im KI-Genie
- eine programmatische Umsetzung, wie die ChatGPT-Schnittstelle im ServiceDesk-Assistenten
- Power Automate: die CoPilot Anfrage "Kann Power Automate Word-Dokumente übersetzen und in ein zweisprachiges Dokument umwandeln?" liefert eine Anleitung. Diese wäre zu prüfen. Siehe dazu auch die weiteren Punkte. 

+Automatisches Austauschen der Formatvorlage in einem Word-Dokument+ 
Die Formatvorlage in einem Word-Dokument ist lediglich ein Attribut, welches einfach programmatisch 
geändert werden kann. Ein VB-Code Beispiel dafür sähe so aus (Quelle: CoPilot)
{code}
        Set doc = Documents.Open(folderPath & file)
        doc.AttachedTemplate = templatePath
        doc.UpdateStylesOnOpen = True
        doc.Save
        doc.Close
{code}
Auch dafür gibt es laut CoPilot in Power Automate einen fertigen Baustein. Das dürfte also in allen Ausführungen trivial sein.	

+Einarbeiten der Zweispaltigkeit und Einfügen der Übersetzungen in die zweite Spalte unter Beibehaltung der Grafiken und Tabellen+
Dies dürfte die größte Herausforderung werden, da es sicherlich keine fertige Bibliotheksfunktionen oder Power Automate Baustein dafür geben wird. "Zweispaltig machen" wird es geben, aber dann wird der Textfluss schlicht in zwei Spalten umgebrochen, was nicht dem entspricht, was wir brauchen. Mit Hilfe von Bibliotheken, die aus dem Worddokumenten eine baumartige programmatisch manipulierbare Datenstruktur (z.B. XML) machen, so wären darin lediglich die Text-Knoten 
zu identifizieren, die Texte zu übersetzen und für jeden gefundenen Knoten ein weiterer daneben mit der Übersetzung einzufügen. Für verbreitete Hochsprachen, wie PHP und Python sind Bibliotheken zur Verarbeitung von Word verfügbar, aber ich habe noch mit keiner davon gearbeitet. Dabei ist auch zu bedenken, dass Word einen Feature-Reichtum an Formatierungen zulässt. Wenn der Aufwand nicht ausarten soll, können wir nur einen sehr kleinen Teil davon berücksichtigen. Wir brauchen zwar auch nur einen kleinen Teil, aber wir werden trotz intensiver Tests nicht ausschließen können, dass vereinzelt oder durch hinzukommende Verwendungen von Features durch LineUp Nacharbeiten an den Dokumenten oder Prozessen notwendig werden.  

+Austausch von Logo und Signatur+
Dies ist nur sicher möglich, wenn die gelieferten LineUp-Dokumente einheitlich genug sind, und die auszutauschenden Elemente in der Dokumentstruktur programmatisch eindeutig identifiziert werden können. Wenn dies sicher gestellt ist, so ist der Austausch trivial.

+Versionierung+
Die Versionierung wäre erst auf den fertigen Dokumenten bei Änderungen notwendig. Daher wäre dies voraussichtlich kein Bestandteil des Konvertierungsprozesses. Es könnte also, wie bisher, manuell unter Verwendung der Word-Versionierungsfunktionen erfolgen. Alterativ wäre die Speicherung in einem git-Repository möglich. git macht die Versionierung automatisch, funktioniert aber nicht gut auf binären Dokumenten, wie .docx. CoPilot empfiehlt dazu das Definieren eines gitattributes
{code}
*.docx diff=pandoc
{code}
Alternativ könnte das Word-Dokument in einem Textformat gespeichert werden, wie XML. Das aus Word-Dokumenten erzeugte XML ist aber bekanntermaßen wenig übersichtlich und nachbearbeitungsfreundlich. 
Da dies keine MUSS-Anforderung ist, würde ich hier vorerst keine Energie investieren.

+Projektstruktur im Teams+
Diese wird voraussichtlich so bleiben können, wie sie ist. Die Verzeichnisse und Dateien wären weiterhin manuell zu verwalten. Denkbar wäre ein automatischer Upload der Ergebnisse der Dokument-Konvertierung. Dazu müsste der Konvertierungsprozess eine Berechtigung auf das Verzeichnis erhalten und für die Übertragung wäre laut CoPilot die Graph-API oder SharePoint notwendig. Power Automate soll diese Funktion beinhalten. In dem Fall wäre aber zu bedenken, dass der Power Automate-Flow Zugriff auf das Verzeichnis haben muss (siehe Datenschutz).

*Gedanken zu Power Automate*
Beim Einsatz von Power Auomate ist zu bedenken, dass dies ein Entwicklungswerkzeug ist, für das wir, wie auch für SAP/ABAP und Webserver/PHP/SQL Expertise, Entwicklungsrichtlinien und Infrastrukturen brauchen und für die Dauer des Einsatzes pflegen müssen. 
Des Weiteren kann ich aus Erfahrung hinzufügen, dass Flow-Charts nur bis zu einer gewissen Größe und Komplexität übersichtlich und gut wartbar sind. Für einen schnellen Prototypen oder nur eine Teilautomatisierungen aus Standardbausteinen dürfte es aber grundsätzlich gut geeignet sein.

*Datenschutz*
Es ist zu bedenken, dass Daten aus den Verträgen an verschiedene Schnittstellen übertragen werden bzw. die vollständigen Vertragswerke für die Verarbeitung in Power Automate übertragen werden. Die Anbieter dieser Dienste können diese Informationen also einsehen. Das ist natürlich auch jetzt schon im Fall von DeepL der Fall.

*CRA*
Das CRA fordert von Vertriebspartnern von Herstellern, dass diese stellvertretend für die Hersteller die Anforderungen des CRA zu erfüllen haben. Es ist zu prüfen, ob dies bei unserer Kooperation mit LineUp der Fall ist. Das würde voraussichtlich hinsichtlich der Dokumente bedeuten, dass wir zukünftig weitere Dokumente von LineUp erhalten und geeignet an unsere Kunden weitergeben müssen.
- Moin [~nschadow], bei der Beschreibung habe ich nur ein paar Kleinigkeiten angepasst, ansonsten ist das so vollständig. 
- Danke, [~msp.j.neumann]!
- Hallo Johanna, magst Du bitte ein paar Originaldokumente von Lineup hier an das Ticket hängen, die Nazar und Baurel zur Entwicklung benutzen können und zusätzlich ein oder mehrere fertige Dokumente, damit wir eine klare Zielvorgabe zum Vergleichen haben?
- Moin Nico, ich habe mit den beiden gesprochen und die beigefügten Dateien erklärt. Sie schauen es sich mal an.
- [~msp.j.sievering], perfekt, vielen Dank!
- ChatGpt-API-Dokumentation:
https://platform.openai.com/docs/api-reference/making-requests

Einen Token für die ersten Tests habe ich [~msp.b.tanekam] und [~msp.n.buzyl] soeben als PrivateBin-Link geschickt.

- Hallo [~msp.j.sievering] ,

Mir fehlt jetzt nur noch deine Einschätzung. Wir haben ChatGPT für die Textübersetzung integriert, und dort gibt es eine Einstellungsoption für den „Grad der Menschlichkeit“ (temperature) – also wie detailliert die Übersetzung erfolgt. Ich habe eine Datei ([^Vergleich_AI_Uebersetzung.docx]) erstellt, in der ich den Originaltext sowie die Übersetzungen mit verschiedenen Stufen bereitgestellt habe (die direkt nebeneinander stehenden Versionen sind sich sehr ähnlich oder können sogar identisch sein).

Magst du bitte diese Versionen bewerten und mir mitteilen, welcher Grad am besten geeignet wäre. Falls du einen bestimmten Textabschnitt testen möchtest, lass es mich bitte wissen.

Außerdem habe ich die übersetzte Datei ([^Englisches Original_EN-DE_Java.docx]) mit Grad 0.2 angehängt. Magst du bitte das zu überprüfen, besonders auf Tabellen und das Format zu achten – ob dir diese Umwandlung passt. Die Datei wurde direkt nach der Generierung durch das Programm versendet, ohne jegliche Nachbearbeitung.

Du kannst auch darauf achten, dass in einigen übersetzten Abschnitten der Stil verändert wurde. Ich konnte bisher nicht herausfinden, woran das liegt. Falls es hilft, kann ich ein Standardstil setzen oder die Formatierung in Word manuell anpassen.

Falls du die Umwandlung noch detaillierter prüfen möchtest, kannst du auch weitere Dokumente schicken, mit denen du das Programm testen möchtest.
- Hallo Johanna,
[Das Programm|https://github.com/MSP-Medien-Systempartner/lineup-documentation-converter/tree/nazarbuzyl-msp] ist praktisch vollständig fertig, es fehlen nur noch einige spezifische Klarstellungen von Ihrer Seite, die im Kommentar unten stehen.

Das Programm ermöglicht es, durch Angabe des Pfads zu einem Ordner/Datei, in dem sich die zu übersetzenden Dateien befinden, und eines zweiten Arguments, das den Speicherort der übersetzten Dateien angibt, alle Dateien zu übersetzen. Ein zusätzliches Argument für eine genauere Übersetzung kann der Pfad zu einem Glossar sein, in dem die genauen Übersetzungen für Fachbegriffe angegeben sind.
- Lieber [~msp.n.buzyl], das klingt ziemlich cool - wenn Ihr fertig seid, würde ich mir die Lösung gern eimmal vorstellen lassen. Magst Du dazu vielleicht einmal nach einem Termin schauen - gern zusammen mit Johanna, Iris, Baurel und Nico ...beste Grüße Heike 
- Moin zusammen, 

ich habe mir die Versionen angeschaut. Bei der Temperatur bin ich noch unschlüssig, stellenweise gefällt mit 0.3 oder auch 0.9 ganz gut, letztlich sind aber alle dazwischenliegenden auch in Ordnung. Eine Nacharbeitung des Dokuments ist in jeden Fall noch erforderlich um das Layout zu finalisieren, Logos, Kopf- und Fußzeilen anzupassen. Insgesamt würde es den Prozess aber auch jetzt schon erleichtern, da die viele Klickerei für das Zusammensetzen der Bausteine entfällt. Ich hänge euch probeweise mal noch eine weitere Datei an, diesmal nur das englische Original, vielleicht könnt ihr die auch nochmal probeweise durchspielen? 

Danke 
- Moin,
Ich habe das neue Dokument durch das Programm laufen lassen. Ich füge das neue übersetzende Dokument([^2024-10-31 - MAS - INTS-1382 Invoice Printing OUTBOUND v2.0_EN-DE.docx]) hier bei. Beim Durchsehen kann man einige Ungenauigkeiten im Format erkennen:
 * 1, 7.3: Einige Texte kann die KI nicht übersetzen oder hält sie nicht für notwendig zu übersetzen. 
 * 7.3: Einige Zeilen “verstecken” sich hinter anderen (Testsystem:, Server:…)
 * 8.1: das Programm liest die Formatvorlage so, wie sie vorgegeben ist, daher erscheinen blaue Bulletpoints.

Für dieses Dokument ist auch eine Temperatur von 0,3 eingestellt.
- Hi [~msp.n.buzyl], wie besprochen, hier nochmal ein komplexes Dokument zum testen. Die "versteckten" Zeilen machen mir etwas Sorgen, da diese nicht leicht zu lokalisieren sind. Das gerade angehängte Dokument hat auch nochmal Einzüge, hängende, Einzüge, eine Tabelle, eine XML.. schau mal bitte, wie das verarbeitet wird. Danke!
- Das neue Dokument wurde problemlos durch das Programm verarbeitet. Es gab keine Abweichungen wie “versteckte” Zeilen. [^2024-05-28 - MAS - INTS-1379 Procset Integration v1.2_EN-DE_v4.docx]
- [~msp.n.buzyl] ich habe nun 3 noch gar nicht bearbeitete Dokumente von LUP bekommen, die sind so im Original. Können wir die auch noch ausprobieren? Habe ich gerade angehangen. Danke!
- [~msp.j.sievering] die Dokumente wurden getestet. Ich hänge dann gleich alle übersetzten Dokumente an. Das Problem mit den "versteckten" Zeilen haben wir gelöst. Eine Bemerkung: Im Dokument ([^2024-10-31 - MAS - INTS-1382 Invoice Printing OUTBOUND v2.0_EN-DE_v2.0.docx] ist in einigen Fällen der Abstand zwischen den Absätzen nach der Bearbeitung zu groß, aber das kann leicht manuell gelöst werden, wenn die Formatvorlage geändert wird.
