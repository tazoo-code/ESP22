NB: cambiare lo strumento di default per la bibliografia a biber.exe

Come compilare la prima volta:
	- Compilare il file ./document.tex
	- Compilare il file ./document-frn.tex
	- Ricompilare il file ./document.tex per visualizzare l'intero documento 

Compilare l'intero documento:
	- Aprire ./document.tex
	- Compilare
	- Dovrebbe visualizzarsi tutto il documento, compresi frontespizio e bibliografia

Aggiungere il proprio file all'intero documento:
	- Aggiungere il comando \input nel punto in cui voglio inserirlo, con il percorso del file da incorporare
	es: \input{capitoli/capitolo1}

Compilare un singolo file come segue:
	- Aprire solo il file miofile.tex
	- Compilare
	- Dovrebbe visualizzarsi il singolo capitolo, le citazioni dovrebbero funzionare (con il comando \cite)
	NB: se avete compilato l'intero documento in precedenza, è probabile che compili sempre quello.
	Per compilare un singolo documento chiudere e riaprire il programma che usate per compilare

Per aggiungere una refernza bibliografica:
	- Aprire il file ./bibliografia.bib
	- Aggiungere il riferimento bibliografico
	- Per visualizzare la bibliografia completa, chiudere il file della bibliografia
	- Aprire document.tex e compilare l'intero documento
	NB: un riferimento va in bibliografia in automatico solo se è stato usato (con \cite, per intenderci)