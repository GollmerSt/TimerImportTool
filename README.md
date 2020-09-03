# TimerImportTool

## Überblick

Mittels dieses Tools können aus Programmen/Internet-Seiten, welche von von Anbietern (wie z.B. TVInfo, TV Movie) zur Verfügung gestelltet werden,
die aufzunehmenden Programme in verschiedne PC-TV-Aufnahme-Systeme übernommen werden.

Zum aktuellen Stand werden folgende Aufnahmesystem unterstützt:

* DVBViewer
* DVBViewerService

In Zukunft sind weitere geplant, wie z.B. das TVHeadend.

Folgende Anbieter von TV-Programmen/Internet-Seiten werden unterstützt:

* TVBrowser
* TVInfo
* Clickfinder von TV Movie
* TVGenial von Hörzu

Die Verwaltung der Kanalzuordnung erfolgt über eine GUI-Oberfläche:

![Gui channel assignment](https://github.com/GollmerSt/TimerImportTool/raw/master/Documentation/images/ProviderAssignment.png)

Das Tool lässt sich in die Oberfläche der obigen vier Anbieter/Programmen integrieren bzw. nutzt den Zugriff zu den Merkzetteln (TVInfo).

Am umfangreichsten ist das Tool in die TVBrowser-Oberfläche integriert. Hier ein Beispiel:

![TVBrowser](https://github.com/GollmerSt/TimerImportTool/raw/master/Documentation/images/TVBrowser.png)

Das Tool bietet folgende Features:

* Vorlauf-/Nachlaufzeiten können abhängig vom Wochentag, Uhrzeit und Kanal definiert werden
* Mehrere aufeinanderfolgende Sendungen können zu einer verbunden werden. Verbindungen können manuell leicht wieder aufgelöst werden.
* DVBViewerService-Unterstützung auch über das Internet 
* Bei DVBViewerService aufwecken des HTPC mittels WOL möglich
* Programm- unterstützte Anbieter-Aufnahmezuordnung durch speziellen Titel-Vergleichsalgorithmus
* Unterstützung bei der Kanalzuordnung durch speziellen Kanalnamen-Vergleichsalgorithmus
* Import der Kanalzuordnung des alten TVInfo-Tools möglich (aus tvinfoDVBV.ini-Datei)
*	Import der Kanäle aus den Anwendungen TV-Browser, TVInfo, Clickfinder und TV-Genial.
* Automatische Zuordnung der Anbieter-Kanälen und den DVBViewer-Kanälen bei TV-Browser, TVInfo und Clickfinder.

Nähere Informationen befinden sich in folgender PDF-Datei
[DVBViewerTimerImport.pdf](https://github.com/GollmerSt/TimerImportTool/raw/master/Documentation/DVBViewerTimerImport.pdf).

Sie ist aktuell auf einem etwas älteren Stand, der aktualisierte Inhalt wird in Zukunft in der Wiki zu finden sein.



