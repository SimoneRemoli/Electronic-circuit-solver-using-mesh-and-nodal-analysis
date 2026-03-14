# MaglieNodi

MaglieNodi e un'applicazione web didattica per costruire, visualizzare e risolvere circuiti elettrici nel dominio dei fasori con:

- metodo delle maglie
- metodo dei nodi
- costruzione simbolica del sistema
- risoluzione numerica finale

L'obiettivo non e solo ottenere un risultato, ma rendere leggibile il passaggio da schema circuitale a sistema di equazioni.

## Vetrina

### Configurazione iniziale

La schermata iniziale imposta metodo di analisi, numero di incognite e inventario dei componenti disponibili.

![Home del software](docs/images/hero-home.png)

### Editor topologico

Il circuito viene costruito come grafo: nodi, rami, componenti, versi dei generatori e associazioni utili alla costruzione del sistema.

![Editor topologico](docs/images/topology-editor.png)

### Sistema simbolico

Una volta definita la topologia, il software genera la matrice del sistema, le equazioni espanse e le relazioni aggiuntive.

![Sistema dei nodi](docs/images/nodal-system.png)

### Inserimento valori numerici

Dopo la costruzione simbolica, il progetto consente di inserire valori di componenti e generatori nel dominio complesso.

![Valori numerici](docs/images/numeric-values.png)

## Cosa fa

- costruisce sistemi circuitali con metodo delle maglie e metodo dei nodi
- gestisce componenti passivi `R`, `L`, `C`
- gestisce generatori di corrente `Ig` e generatori di tensione `Vg`
- supporta gruppi equivalenti in serie e in parallelo
- consente la definizione topologica del circuito tramite editor visuale
- produce forma matriciale, equazioni espanse e sistema completo
- accetta valori numerici e fasoriali per la risoluzione finale

## Punti forti

- approccio visivo: il circuito viene prima costruito come struttura topologica
- approccio formale: il sistema viene mostrato in forma matematica leggibile
- approccio didattico: il focus e sul ragionamento, non solo sulla risposta finale
- workflow completo: dalla configurazione iniziale alla soluzione numerica

## Flusso di utilizzo

1. Scegli il metodo di analisi.
2. Definisci quante incognite vuoi usare.
3. Imposta l'inventario dei componenti.
4. Costruisci il circuito nell'editor topologico.
5. Specifica rami, componenti, orientazioni e relazioni utili.
6. Genera il sistema simbolico.
7. Inserisci i valori numerici e risolvi.

## Stack

- Java Servlet
- JSP
- Maven e Tomcat
- MathJax per il rendering delle formule

## Visione del progetto

MaglieNodi nasce con una finalita precisa: trasformare la costruzione del sistema circuitale in un processo esplicito, controllabile e leggibile.

Negli esercizi di elettrotecnica l'errore spesso non e nel conto finale, ma qui:

- segni sbagliati
- versi non coerenti
- generatori trattati male
- componenti condivisi interpretati in modo scorretto
- equivalenti serie/parallelo persi durante la formalizzazione

Questo progetto prova a intervenire proprio in quella fase.

## Autore

**Simone Remoli**

Progetto sviluppato come applicazione dedicata alla rappresentazione e alla risoluzione di sistemi circuitali, con impostazione coerente con il corso di Elettrotecnica per Ingegneria Informatica.
