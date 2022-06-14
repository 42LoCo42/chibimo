# Themenblock 1 – Software Engineering / SEMAT Essence
1. Wie lässt sich Software Engineering definieren?
	Die Anwendung eines systematischen, disziplinierten, quantifizierbaren Ansatzes zu Entwicklung, Betrieb und Wartung von Software
2. Herrscht ein Mangel an Methoden/Prozessen zur Softwareentwicklung?
	Nein, es gibt eine Vielzahl von Methoden
3. Was erschwert den Einstieg in all die verfügbaren SE-Prozesse?
	Sie sind komplex und bauen aufeinander auf
4. Was ist SEMAT Essence? Welches Problem geht Essence an?
	Eine Initiative, Softwareentwicklung als rigorose Disziplin zu qualifizieren
	Das Problem, dass Softwareentwicklung oft unstrukturiert und mit wenig Planung abläuft, weswegen es nicht als echte Disziplin gesehen wird
5. Was ist der Essence Kernel? Wie kann der Essence Kernel Ihnen bei einem/jedem SE-Projekt Unterstützung liefern?
	Eine Sammlung von Definitionen, die die Bedeutung effektiver & skalierbarer Softwareentwicklung darstellt
6. Was sind die 7 Alphas des Essence Kernel? Wofür stehen die Alphas? Warum kann man sich in einem SE-Projekt nicht allein auf eines der Alphas konzentrieren?
	Die 7 Alphas sind & bedeuten:
		- Opportunity: Die Umstände, die zur Entwicklung eines Software-Systems führen
		- Stakeholders: Die Personen(gruppen) oder Organisationen, die mit dem Software-System interagieren (beeinflussen & beeinflusst werden)
		- Requirements: Die theoretischen Anforderungen, um die Wünsche des Kunden zu erfüllen
		- Software System: Die praktische Implementation dieser Anforderungen
		- Work: Arbeit (duh)
		- Team: Arbeiter (duh^2)
		- Way of Working: Arbeitsweise; wie das Team die Arbeit strukturiert und umsetzt
	Weil alle Alphas zueinander in Beziehungen stehen und gleichmäßig behandelt werden müssen.

# Themenblock 2 – Opportunistisches Vorgehen →  Wasserfall →  Iterativ/Inkrementell/Agile, UML, Vision & Scope, Use Cases
1. Warum reicht ein opportunistisches Vorgehen in komplexen SE-Projekte nicht mehr aus? Welche Probleme bestehen dabei?
	- Dauerhaft zu langsam
	- Qualität nicht gesichert, da ungeführter (quasi zufälliger) Entwicklungsprozess
	- Anforderungen unvollständig
	- Im Team nicht skalierbar
2. Was versteht man unter dem Wasserfall-Vorgehensmodell? Welche Phasen sind typischerweise enthalten? Wo liegen Vorteile und was sind die Nachteile des Wasserfallmodells?
	- Eine doppelt verkettete Liste von Phasen
	- bei Problemen: Rücksprung
	- Phasen sind:
		- Idee/Konzept
		- Anforderungen
		- Analyse
		- Entwurf
		- Realisierung
		- Test
		- Betrieb/Warung
	- Vorteile:
		- einfacher & logischer Aufbau
		- systematisches Vorgehen
		- Effektiv zu verwalten
	- Nachteile:
		- Anforderungen werden nur zu Beginn ermittelt (sie können sich im Laufe der Zeit verändern)
		- Fehler pflanzen sich fort und werden nur spät entdeckt
		- Späte Änderungen sehr teuer
		- Übergabe der Verantwortun
3. Was verbirgt sich in der Softwareentwicklung hinter der Aussage „Fehler pflanzen sich fort“?
	Bis ein grundsätzlicher Fehler entdeckt wird, kann er schon viele Teile des Entwicklungsprozesses negativ beeinflusst haben
4. Was versteht man unter Inkrementeller Entwicklung?
	Aufteilung des Prozesses in abgeschlossene Teile
5. Was versteht man unter iterativer Entwicklung?
	Aufteilung des Prozesses in Phasen gleicher Länge (jeweils einige Wochen)
6. Wie unterscheidet sich die Softwareentwicklung nach dem Wasserfallmodell und nach Iterativ/Inkrementeller Vorgehensweise?
	Das Wasserfallmodell ist relativ unflexibel, da alle Phasen durchlaufen werden müssen.
	Im interagieren Modell kommt es zum wiederholten Durchlauf aller relevanten Phasen, wodurch eine hohe Flexibilitä gewährleistet wird.
7. Was ist das agile Manifest? Welche Agilen Grundwerte enthält dieses Manifest? Wofür bildet es die Basis?
	Eine Liste von 4 Grundwerten von 12 Prinzipien. Die Grundwerte sind:
		- Individuen und Interaktionen  > Prozesse und Werkzeuge
		- Funktionierende Software      > umfassende Dokumentation
		- Zusammenarbeit mit dem Kunden > Vertragsverhandlung
		- Reagieren auf Veränderung     > das Befolgen eines Plans
	wobei ">" bedeutet: beide Werte sind wichtig, die jeweils linken werden jedoch höher eingeschätzt
8. Nach welchen Grundsätzen handeln agile Teams in Softwareprojekten?
	s.o.
9. Welche agile Methode (bzw. agiles Methodenframework) wird gegenwärtig am meisten verwendet?
	Die Verbesserung der Fähigkeit, veränderliche Prioritäten zu verwalten
10. Was verbirgt sich hinter den Abkürzungen „MVP“ und „MMF“? Warum sind diese Konzepte wichtig?
	Minimum Viable Product: die erste Produktversion, die bereits vom Kunden benutzt werden kann
		notwendig zum frühen Sammeln von Feedback
	Minimum Marketable Feature: die Programmteile, die vom Kunden prioritisierte Funktionalität enthalten
		durch frühe Entwicklung der MMFs kann das Software-Projekt schnell Gewinn machen (es ist kein Zufall, dass MMF auch für den "Make money fast!"-Email-Scam steht)
11. Warum muss – auch in Softwareprojekten – dokumentiert werden?
	Wissen geht permanent verloren
12. Was beschreibt ein Vision & Scope-Dokument? Wozu dient es?
	Den Nutzen des Projektes aus Sicht des Kundens
	So wird die Entwicklung unnötiger Funktionalität und Eigenschaften vermieden
13. Was ist ein Product Vision Board?
	Eine visuelle Darstellung der wichtigsten Planungsparameter:
		- Zielgruppe
		- Adressierte Notwendigkeiten
		- Produkt
		- Wirtschaftliche Ziele
		- Konkurrenten
		- Einkommenquellen
		- Kostenfaktoren
		- Publikationskanäle
14. Welche Zielkonflikte sind in (Software-)projekten typisch? Wie lassen sich Ziele unterscheiden und priorisieren?
	idfk
15. Was ist die UML?
	Unified Modelling Language, eine graphische Beschreibungssprache zur Darstellung komplexer Systeme
	Wird speziell für objektorientierte Vorgehensweisen eingesetzt
16. Was ist ein Anwendungsfall (Use Case)? Wie lässt sich ein Use Case beschreiben? Zeichnen Sie ein Use Case-Diagramm ...
	Interaktionsverläufe zwischen Akteuren und dem Software-System, welche in einem Ergebnis für den Akteur münden
	System: chibimo
	Akteur: Musik-hören-wollender User
	Interaktionen:
		- Ordner in Library expandieren →  Inhalte vom Ordner
		- Wenn ein Inhalt ein gewünschtes Lied ist: auswählen
		- chibimo spielt das Lied ab

# Themenblock 3 – User Story Mapping →  User Stories →  Domänenmodell
1. Was ist User Story Mapping? (Handout-Dokument) In welchen Schritten erfolgt User Story Mapping?
	Die Erstellung einer vollständigen Übersicht über Benutzer und Interaktionen eines Produktes
	Die Schritte der Erstellung sind:
		1. User Tasks: Aufgaben ermitteln, die von Nutzern durchgeführt werden
		2. User Activities: Aufgabengruppen bilden anhand zusammengehöriger/ähnlicher Aufgaben
		3. Activities ordnen gemäß der Reihenfolge, in der ein Nutzer sie durchführen würde, um ein Endziel zu erreichen
		4. Story Map durchlaufen: Mit verschiedenen Personengruppen
		5. User Stories schreiben
2. Welche Ziele verfolgt User Story Mapping?
	- kohärente Sicht auf das Produkt verschaffen
	- beim Verstehen von Nutzerinteraktionen helfen
3. Was ist eine User Story? (F32) Was ist eine typische Vorlage dafür?
	Konkrete Abläufe, die ein Nutzer durchführt, um ein Ziel zu erreichen
	Vorlage: Die einzelnen Inputs, um ein größeres Datenobjekt einzutragen
4. Welche Aspekte (5C) spielen bei den User Stories eine Rolle? Nennen Sie die Aspekte hinter den 5C! Erläutern Sie 2 der Aspekte!
	Card
	Conversation
	Confirmaton
	Construction
	Consequences
5. Welche Eigenschaften sollten gute User Stories haben (INVEST)?
	Independet
	Negotiable
	Valuable
	Small
	Testable
6. Welche Eigenschaften sollten Aufgaben/Ziele (auch) in der Softwareentwicklung haben? Wann ist eine Aufgabe bzw. ein Ziel SMART?
	Specific
	Measurable
	Achievable
	Relevant
	Time-boxed
7. Wozu dienen das Kano- und das MoSCoW-Modell?
	Zufriedenheit systematisch erringen
	Prioritisierung von Anforderungen anhand ihrer Wichtigkeit und Auswirkung
8. Wozu dient ein Domänenmodell? Wie wird dieses beschrieben? Erstellen Sie ein Domänenmodell ...
	UML der Anwendungsdomäne in konzeptuellen Klassen
	Modellierung des Problembereiches

# Themenblock 4 – Systemkontext →  NFAen →  Entwicklungsparadigmen →  Architektur(-muster)
1. Was versteht man unter einem Systemkontext? Warum ist ein gemeinsames Verständnis über den Systemkontext wichtig in einem Softwareprojekt?
2. Welche Eigenschaften haben "gute Anforderungen"?
3. Warum müssen User Tasks/Epics noch in kleinere User Stories geschnitten werden, bevor Sie aktiv im Projekt bearbeitet werden?
4. Was sind funktionale Anforderungen? Nennen und erläutern Sie kurz 2 Praktiken, um funktionale Anforderungen zu dokumentieren? (→ Use Case, User Story)
5. Was sind nicht-funktionale Anforderungen (NFA)? Nennen Sie 6 Formen der NFAen! Erläutern Sie 2 davon kurz!
6. Wie wirken sich NFAen auf die Gestaltung eines Software Systems aus?
7. Nennen Sie 4 Entwicklungsparadigmen der Softwareentwicklung für mobile Plattformen! Erläutern Sie 2 davon näher?
8. Was versteht man unter einer Softwarearchitektur?
9. Was beschreibt das Prinzip der „Separation of Concerns“?
10. Nennen Sie 3 Architekturmuster für mobile Apps! Welches Architekturmuster wird aktuell von den Android-Plattform-Entwicklern empfohlen? Welche Herausforderungen bewältigt dieses ohne weiteren Entwickler-Aufwand?

# Themenblock 5 – Designprinzipien (SOLID)
1. Nennen und erläutern Sie 5 Merkmale schlechten Softwaredesigns!
2. Wofür stehen die SOLID-Designprinzipien?
3. Erläutern Sie kurz das Single Responsibility Prinzip, das Interface Segregation Prinzip und das Dependency Inversion Prinzip!

# Themenblock 6 – Analyse und Design-Patterns
1. Was sind Muster (Patterns) in Analyse und Design/Architektur? Welche Vorteile bieten sie? Wie sind sie beschrieben?
2. Analyse-Muster:
	a. Exemplartyp / Abstraction-Ocurrence
	b. Wechselnde Rollen / Player-Role
	c. Allgemeine Hierarchie / General Hierarchy
3. In welche Typen können die Design Patterns der Gang of Four (GoF) gegliedert werden? Was ist typisch für Muster der jeweiligen Kategorie?
	Design Patterns: Builder, Singleton, Adapter, Composite (siehe Allgemeine Hierarchie), Facade, Observer, Immutable
4. Welches Analyse-/Designmuster wird hier dargestellt?
5. Welches Analyse-/Designmuster passt zur Lösung der folgenden Problembeschreibung?
6. Stellen Sie Analyse-/Designmuster XYZ in UML dar!

# Themenblock 7 – Test
1. Was können uns lt. Dijkstra Softwaretests zeigen? Was nicht? Warum?
2. Wozu werden Softwaretests durchgeführt?
3. Was macht das Testen mobiler Anwendungen besonders herausfordernd?
4. Was sollte getestet werden? Welche Werkzeuge stehen uns dafür in der Android-Entwicklung zur Verfügung?
5. Nennen Sie 2 Praktiken des Statischen Softwaretests! Nennen Sie Werkzeuge zur statischen Codeanalyse! Was testen diese Werkzeuge?
6. Wie unterscheiden sich Whitebox von Blackbox-Tests beim Dynamischen Softwaretest?
7. Was unterscheidet einen systematischen Test von einem „Mal-eben-Ausprobieren“-Vorgehen?
8. Wozu dient das Konzept der Äquivalenzklassen beim Softwaretest? Definieren Sie Äquivalenzklassen für folgende Anforderung!
9. Worauf testet man heute mobile Anwendungen? Welche Vor- und Nachteile sind damit verbunden?
10. Warum sollten Tests automatisiert werden?
11. Welche Struktur (Zusammensetzung) von Tests empfehlen die Android-Plattform-Entwickler? Was testen die Tests der jeweiligen Kategorie? Welche Werkzeuge stehen dafür zur Verfügung?

# Themenblock 8 – Endeavor (Team, Work, Way of Working)
1. Welche agilen Entwicklungsmethoden sind gegenwärtig am populärsten?
2. Was ist Scrum? (Scrum-Foliensatz)
	a. Welche Rollen sieht Scrum vor (Product-Owner, ScrumMaster, Team)? Welche Aufgaben hat die jeweilige Rolle? Wie sollte diese gestaltet sein?
	b. Wie groß ist ein Scrum-Team idealerweise?
	c. Was ist ein Sprint im Scrum-Kontext? Was sollte innerhalb des Sprints möglichst nicht geschehen?
	d. Welche Meetings sieht Scrum vor (Sprint-Planung, Sprint-Review, Sprint-Retrospektive, Daily Scrum)? Was geschieht in diesen Meetings?
	e. Welche Fragen werden von jedem Teilnehmer im Daily Scrum beantwortet?
	f. Was ist ein Product Backlog?
	g. Was ist das Sprint Backlog? Wie wird dieses gefüllt und verwaltet?
	h. Was stellt das Sprint Burndown Diagramm dar? Auf welcher Basis?
3. Wann passt Scrum ggf. nicht für ein Team? Welche agile Methode könnte man stattdessen verwenden?
4. Wofür steht WIP-Limit in Kanban? Warum wird dem eine so hohe Bedeutung beigemessen?
5. Welche Phasen durchläuft ein Team nach dem Phasenmodell nach Tuckman? Wodurch sind die Phasen charakterisiert?

# Themenblock 9 – Sicherheit
1. Wie kann man Sicherheit beschreiben? Welche 2 Aspekte unterscheidet man dabei?
2. Was ist der Security Development Lifecycle? Welche Ziele verfolgt dieser?
3. Was versteht man unter Threat Modeling?
4. Welche 4 Fragen sollte man sich beim Threat Modeling zwingend stellen?
5. Wer sollte aus Sicht der SDL-Entwickler Threat Modeling betreiben?
6. Wie sieht der Threat Modeling Prozess im SDL aus?
7. Welche 5 Elementtypen enthält ein Data Flow Diagram beim Threat Modeling?
8. Was ist eine Trust Boundary?
9. Wofür steht STRIDE beim Threat Modeling?
10. Welchen Gefährdungen sind diese Elementtypen jeweils ausgesetzt?
11. Werten Sie die Aussagen „Think like a Attacker“ und „Threat Modeling is for Specialists“ im Kontext des Threat Modeling!
12. Nennen Sie 3 Beispiele dafür, wie sich Sicherheitsbewusstsein in eine agile Entwicklungsmethode wie Scrum integrieren lässt! Erläutern Sie Ihre Beispiele kurz!
13. Wofür steht OWASP? Wie kann OWASP ein sicherheitsbewusstes Team in der Entwicklung mobiler Apps unterstützen?
14. Was ist Google’s App Security Improvement Program?
15. Wie unterstützt die Android Plattform im Hinblick auf die Entwicklung sicherer Apps? Nennen Sie 4 Beispiele!
