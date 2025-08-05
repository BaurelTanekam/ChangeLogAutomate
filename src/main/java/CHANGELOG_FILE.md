
## Jira Comments- Das kannst Du ignorieren. Der Grund für die Umbenennung ist, dass die Funktion, die den Namen setzt, in eine allgemeinere Bibliothek umgezogen ist. Daher war es sinnvoll den Namen in das allgemeinere "import_..." zu ändern.
Aber offensichtlich führt das dazu, dass die Dateien mit dem alten Namen nicht aufgeräumt werden. Die lösche ich in den kommenden Tagen manuell. Danke für den Hinweis!
- Okay, danke für die Rückmeldung :)
- Es ist etwas anders, als ich es in meinem vorherigen Kommentar dargestellt habe. Die Bibliotheksfunktion legt die "import"-Dateien automatisch zusätzlich an. Dort wird aber nichts hineingeschrieben, weil die TM-Importe dann stattdessen die tmimport-Dateien anlegen und benutzen. Trotzdem könnt Ihr das ignorieren.
Ich sorge dafür, dass keine unnötigen "import"-Dateien mehr erzeugt werden.
- Die Korrektur ist auf https://nwtwebtest2.nordwest-ticket.de. Am Montag werde ich sie life nehmen.
- Alles klar, passt so für mich :)
- In der Testumgebung sind seit Freitag keine neuen Dateien import* angelegt worden, so wie es sein soll.
Die Änderungen sind nun auf https://www.nordwest-ticket.de. Die leeren import*-Dateien habe ich manuell gelöscht.

## Jira Comments- Die Datei ist ausgetauscht auf beiden Systemen.
- Danke!

## Jira Comments- Die Änderungen sind auf https://nwtwebtest2.nordwest-ticket.de/ueber-nordwest-ticket.

Kurze Anmerkung zu den Bildern: das Seitenverhältnis passt. Die Größe war aber in mm anstelle von px. Daher musste ich die nochmal skalieren.
- Die Änderungen sind nun auch auf https://www.nordwest-ticket.de/ueber-nordwest-ticket.
- Vielen Dank, passt so und ist vom Marketing abgenommen.

## Jira Comments- Hallo Kai,
habt Ihr Anmerkungen oder Einwände zu diesem Thema?
- Hallo [~nschadow] Danke für die Info! Ich kläre das und melde mich asap.
- Soooo, bereits geklärt - bitte umsetzen und vielen Dank nochmal, dass du das auf dem Schirm hattest!
- Erster Entwurf: 
https://nwtwebtest2.nordwest-ticket.de/ezb
Den Link dorthin habe ich im Footer hinter Impressum eingefügt.

Es fehlt noch eine Angabe zur öffentlichen Meldestelle für den Fall dass wir nicht fristgerecht auf Beanstandungen reagieren, da mir nicht klar ist, welche Meldestelle das ist.
- Vielen lieben Dank - ist so von Angela freigegeben!
- Ich habe noch dafür gesorgt, dass die externen Links einen zusätzlichen Browser-Tab öffnen. 

Die Änderungen sind nun auf https://www.nordwest-ticket.de/ezb.
- Danke!

## Jira Comments- Die Änderung ist auf https://nwtwebtest2.nordwest-ticket.de/ueber-nordwest-ticket.
- Danke, bitte live einspielen!
- Änderung ist auf https://www.nordwest-ticket.de/ueber-nordwest-ticket.
- Danke, passt

## Jira Comments- Die Änderung ist auf https://nwtwebtest2.nordwest-ticket.de/datenschutzerklaerung.
- Danke, bitte live einspielen!
- Änderung ist auf https://www.nordwest-ticket.de/datenschutzerklaerung.
- Danke, passt!

## Jira Comments- Jetzt sieht es besser aus: https://www.nordwest-ticket.de/. Die Änderung sollte sichtbar sein, ohne ein Löschen des Browser-Caches.
- Danke!

## Jira Comments- Die Änderung ist auf https://nwtwebtest2.nordwest-ticket.de/ueber-nordwest-ticket.
- Danke, bitte live einspielen!
- Änderung ist auf https://www.nordwest-ticket.de/ueber-nordwest-ticket.
- Danke, passt

## Jira Comments- Die Änderung ist auf https://nwtwebtest2.nordwest-ticket.de/datenschutzerklaerung.
- Danke, bitte live einspielen!
- Änderung ist auf https://www.nordwest-ticket.de/datenschutzerklaerung.
- Danke, passt!

## Jira Comments- Jetzt sieht es besser aus: https://www.nordwest-ticket.de/. Die Änderung sollte sichtbar sein, ohne ein Löschen des Browser-Caches.
- Danke!

## Jira Comments- Mir scheinen die richtig angezeigt zu werden:

 !musikfest-bremen.jpg|thumbnail! 
 !musikfest-bremen-2.jpg|thumbnail! 

Vielleicht hat der Browser-Cache bei Euch das Aktualisieren der Seiten verhindert.
Oder meinst Du etwas anderes?
- Ach, Du meinst bestimmt auf der Suchergebnisseite.
- Genau :D Thema stimmt, Suchergebnisseite nicht.
- Die Suchindexeinträge sind da und ich sehe daran nichts Auffälliges. Die Filterung erfolgt also vermutlich danach. Das Venue ist in beiden Fällen die [2930] Glocke (Großer Saal), eine Dublette, die auf den Master [2924] verweist. Keines der Venues ist als PointOfSale markiert.
Bis hierhin sieht alles in Ordnung aus.
- Ich habe den Grund in der Datenbankabfrage eingekreist: die beiden Datensätze verschwinden durch das GROUP BY e.id, aber den Grund dafür verstehe ich noch nicht, denn die jeweilige e.id kommt nur einmal vor. Daher gibt es in diesen Fällen nichts zu gruppieren und somit dürften diese Zeilen auch nicht verschwinden.
Das muss ich genauer untersuchen.
- Ich habe das GROUP BY entfernt, da wir es seit der Umstellung auf nur noch events im Suchergebnis auch gar nicht mehr benötigen sollten. Warum es zu diesem Verhalten führt ist mir aber noch immer nicht klar.

Die Änderung ist auf https://nwtwebtest2.nordwest-ticket.de. Da es einige Aktualisierungen an den eingebundenen Bibliotheken gab, werde ich vor dem Produktvideployment die ganze Anwendung grob testen.
- Ich habe die ganze Anwendung auf https://nwtwebtest2.nordwest-ticket.de grob durchgetestet, ohne Fehler zu finden. Mögt Ihr einmal über die Suchergebnisseiten schauen, ob Euch dort nun neue Fehler auffallen?
- Hallo Nico, 

vielen Dank dafür. Das sieht für mich gut aus. Uns sind keine anderen Fehler aufgefallen.  
- Danke! Die Änderungen sind nun auf https://www.nordwest-ticket.de/.
- Hallo Nico, 

uns ist gerade etwas bei den Suchergebnissen aufgefallen. Es geht jetzt um ein ganz anderes Event. Ich weiß nicht, ob das mit dem Thema dieses Tickets etwas zu tun hat oder eine andere Ursache hat. Hier werden zwei Events doppelt in den Suchergebnissen angezeigt. 

 

Es sind nur 2 Events angelegt.

Einmal 22.03. 20 Uhr und einmal 23.03. 18 Uhr

Die doppelte Ansicht führt zum jeweiligen Event:

22.03. 20 Uhr – 1620201529 (Event ID)

23.03. 18 Uhr – 631718349 (Event ID) 

!image-2025-03-17-12-03-25-057.png|width=615,height=434!
- Vielen Dank für die Info, [~nwt.n.hosseini]! Ja, das hängt damit zusammen. Dann muss ich nach einer anderen Lösung suchen.
- Hallo Noushin,
dank Deiner Info bin ich der Ursache auf die Spur gekommen und konnte sie nun hoffentlich korrekt beheben. Die Änderungen sind auf https://nwtwebtest2.nordwest-ticket.de.
- *Details zur Ursache - falls von Interesse und für spätere Rückverfolgung*
Events und Attractions stehen in einer n:m-Beziehung zueinander. Aus diesem Grund muss die Datenbankabfrage auf der Suchergebnisseite ein GROUP BY haben. Andernfalls erscheint ein  Event im Ergebnis so oft, wie es Attractions zugewiesen ist. Die beiden betroffenen Events "West-Eastern Divan Orchestra" und "Von Heldinnen und Helden" sind mehreren Attractions zugewiesen. Nach dem Anwenden der WHERE-Bedingung sind daher noch mehrere Zeilen im Zwischenergebnis. Das GROUP BY sorgt nun dafür, dass diese zusammengefasst werden, allerdings nicht zu der gewünschten mit dem höchsten fulltext-score. Diese Zeilen werden von der nachfolgenden Filterung mittels HAVING score > x weggefiltert.
Die Lösung ist, den score-Filter aus der HAVING- in die WHERE-Bedingung zu verschieben. So werden die irrelevanten Zeilen schon vor dem GROUP BY aus dem Zwischenergebnis entfernt. 
- Hallo [~nschadow] 

vielen lieben Dank für die ausführliche Erklärung! Die Suchergebnisse sehen jetzt im Testsystem sowohl beim Musikfest als auch beim Event Electronic Instruments gut aus. 
- Die Änderung ist nun auch auf https://www.nordwest-ticket.de/.

## Jira Comments- [~nschadow] hast du hierzu eine Idee?
- An 4 der letzten 8 Tagen ist der Import nicht vollständig durchgelaufen. Die Ursache ist weiterhin der sporadisch auftretende Timeout wegen fehlender Antwort vom Server.
Wir können ausprobieren, wie es sich verhält, wenn ich die Anzahl der Datensätze pro Abfrage auf 200 erhöhe.
- Gerne!

Zur Dokumentation: Ich habe heute den Import zweimal manuell angestoßen und auch diese beiden Male ist er nicht durchgelaufen. Es gab jeweils den Curl-Error.
- Ich stelle gerade peinlich berührt fest, dass meine Änderung auf 50 Datensätze pro Abfrage, wie in NWTDL-507 beschrieben, gar nicht gegriffen hat. Die Logs zeigen nach wie vor 100 Datensätze pro Abfrage. Offensichtlich habe ich das nach der Änderung gar nicht kontrolliert.
Die zentrale Einstellung, die ich geändert habe, wird bei den einzelnen Schnittstellenfunktionen wieder mit 100 überschrieben. Diese Überschreibungen habe ich nun entfernt.

Nun habe ich den Import zweimal lokal gestartet, einmal mit 20 Datensätze pro Abfrage und einmal mit 200. Diesmal habe ich auch kontrolliert, dass die Einstellung auch wirklich greift (Log in [^tmimport_20250313.log]). Beide Läufe sind irgendwo mitten in den Venues an dem Timeout abgebrochen.

Eine mögliche Ursache könnten Firewalls oder andere Netzwerkkomponenten sein, die auf Grund der Anfragemenge pro Zeiteinheit möglicherweise eine DOS-Attacke vermuten und weitere Anfragen kurzzeitig unterbinden.
Ich experimentiere mit einer kleinen Verzögerung zwischen den Abfragen.
- Ich habe heute auch nochmal mit TM geschrieben wegen der Abbrüche. Die API ist zwar auch bei denen instabil, aber nicht in dem Maße wie bei uns, sagt mir mein Kontakt. Frage von ihm:
 
_Kannst du mir noch einmal genau beschreiben, wie oft, und wann genau ihr was abruft? Vielleicht kann man das noch optimieren, indem man die Abrufe aufteilt bzw. anders strukturiert._
 
Kannst du mir hierzu Infos liefern?
- Verzögerungen von 200ms und 1s haben keine Änderung im Verhalten gebracht.
- Der Ablauf ist folgender:
Wir rufen erst attractions, dann venues und dann events ab. Da wir keine Filtermöglichkeit für einen Delta-Import der Form "was hat sich seit dem letztem Import verändert" haben, aktualisieren wir prakitsch immer unsere ganze Datenbank. Die Abfragen iterieren folgende Requests, bis die Antwort leer ist, wobei zum Blättern "start" immer um "rows" hochgezählt wird. (API-Keys aus Sicherheitsgründen ausgesternt)

https://app.ticketmaster.eu/mfxapi/v2/attractions?has_events=true&apikey=****&lang=de-de&domain=germany&start=0&rows=200

https://app.ticketmaster.eu/mfxapi/v2/venues?apikey=****&lang=de-de&domain=germany&start=0&rows=200

https://app.ticketmaster.eu/mfxapi/v2/events?exclude_external=true&apikey=****&lang=de-de&domain=germany&start=0&rows=200

Wir haben mit rows=100, 50, 20 und 200 probiert. Des Weiteren haben wir und zwischen je zwei Requests 200ms und 1s Verzögerung eingebaut. Diese Versuche haben keine erkennbare Verringerung der Wahrscheinlichkeit für die Timeouts erbracht.
Der ganze Ablauf sollte in den Access-Logs bei Filterung auf unseren API-Key nachvollziehbar sein.
- Danke, die Infos gebe ich weiter!
- Hallo Nico,

der Import lief ja offenbar die letzte Woche problemlos durch, wenn ich es richtig sehe - Top!

Können wir dieses Ticket also schließen und ich sage TM, dass aktuell alles stabil läuft? War es denn jetzt diese Änderung, auf 50 Datensätze pro Abfrage, die wir dann am 13.03. umgesetzt hatten?
- Es sind jetzt 200 Datensätze pro Abfrage eingestellt:
 !nwt-log.jpg|thumbnail! 

Aber ja, das ist die Einstellung seit dem 13.03.25.
- Alles klar, danke!

## Version  - 2025-08-05T15:10:36.7706289


### Others changements
-[NWTDL-571] Importe erzeugen nun keine unnötige import.log mehr
- [NWTDL-573] ansprechpartner.pdf ausgetauscht
- [NWTDL-567] Aktualisierung /ueber-nordwest-ticket
- [NWTDL-566] Erklärung zur Barrierefreiheit hinzugefügt
- [NWTDL-559] Text auf /ueber-nordwest-ticket aktualisiert
- [NWTDL-560] URL zum Datenschutzbeauftragten aktualisiert
- [NWTDL-556] Startseitenslider 'Neu im Vorverkauf': Styling der mobilen Ansicht optimiert
- [NWTDL-559] Text auf /ueber-nordwest-ticket aktualisiert
- [NWTDL-560] URL zum Datenschutzbeauftragten aktualisiert
- [NWTDL-556] Startseitenslider 'Neu im Vorverkauf': Styling der mobilen Ansicht optimiert
- [NWTDL-554] Suche baut nun die Filterung auf den score in WHERE ein anstelle vom HAVING, so dass nun das GROUP BY nicht mehr zur Filterung falscher Zeilen führt
- [NWTDL-554] Suchergebnisseite: GROUP BY e.id entfernt
- [NWTDL-553] TM-Import: Rows=200

---
