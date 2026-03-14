# MaglieNodi

> Un ambiente didattico per costruire, visualizzare e risolvere sistemi circuitali nel dominio dei fasori con il metodo delle maglie e il metodo dei nodi.

## Visione

**MaglieNodi** nasce come progetto orientato al corso di **Elettrotecnica per Ingegneria Informatica**.
L'obiettivo non e soltanto ottenere un risultato numerico, ma rendere leggibile l'intero percorso di analisi:

- scelta del metodo risolutivo;
- definizione delle incognite;
- associazione dei componenti alle maglie o ai nodi;
- costruzione del sistema simbolico;
- inserimento dei valori numerici;
- risoluzione finale nel dominio complesso.

Il software prova quindi a stare a meta tra un eserciziario assistito e uno strumento di formalizzazione, mantenendo una struttura abbastanza rigorosa da riflettere la complessita reale degli esercizi di elettrotecnica.

## Cosa fa

Il progetto supporta:

- **Metodo delle maglie**
  - costruzione della matrice delle impedenze;
  - gestione di generatori di corrente con relazione aggiuntiva;
  - gestione del verso dei generatori di tensione rispetto alla corrente di maglia;
  - supporto a raggruppamenti di componenti passivi in **serie** e **parallelo**.

- **Metodo dei nodi**
  - costruzione della matrice delle ammettenze;
  - specifica del **nodo di riferimento**;
  - gestione delle incognite aggiuntive `Ix` associate ai generatori di tensione;
  - gestione del verso dei generatori di corrente e del segno del polo positivo dei generatori di tensione rispetto al nodo;
  - supporto a equivalenti passivi in **serie** e **parallelo**.

- **Risoluzione numerica**
  - inserimento dei valori dei componenti passivi;
  - inserimento diretto dei generatori nel dominio dei fasori, ad esempio `3-j`, `2+4j`, `-j`;
  - visualizzazione del sistema numerico;
  - calcolo della soluzione finale delle incognite.

## Perche e utile

Negli esercizi di elettrotecnica l'errore spesso non nasce dal calcolo finale, ma dalla costruzione del sistema:

- segni sbagliati;
- contributi condivisi tra maglie;
- generatori orientati in modo non coerente;
- equivalenti serie/parallelo trattati in modo errato;
- relazioni aggiuntive dimenticate.

Questo progetto mette il focus proprio su quella fase, cercando di rendere il passaggio da circuito a sistema il piu trasparente possibile.

## Esperienza d'uso

L'applicazione e pensata come un piccolo laboratorio digitale:

1. scegli il metodo, maglie o nodi;
2. definisci il numero di incognite e i componenti presenti;
3. associ ogni componente alle correnti di maglia o ai nodi coinvolti;
4. specifichi versi, relazioni aggiuntive e gruppi serie/parallelo;
5. osservi il sistema simbolico completo;
6. inserisci i valori numerici e risolvi il circuito.

Il risultato non e solo una soluzione, ma una traccia leggibile del ragionamento circuitale.

## Identita del progetto

La natura del software riflette bene la complessita del corso:

- formalismo matematico;
- sensibilita fisica sui versi;
- gestione di impedenze e ammettenze nel dominio armonico;
- attenzione ai dualismi tra metodo delle maglie e metodo dei nodi.

Non e un semplice calcolatore automatico: e uno strumento che prova a rispettare il linguaggio e la logica con cui l'elettrotecnica viene insegnata in ambito universitario.

## Stack

- Java Servlet
- JSP
- MathJax per il rendering delle formule
- Maven per la build

## Avvio

Per compilare il progetto:

```bash
mvn -q -DskipTests compile
```

Per eseguirlo, deploya l'applicazione su un container Servlet compatibile, ad esempio Tomcat, e apri la pagina iniziale del progetto.

## Autore

**Simone Remoli**

Progetto sviluppato come applicazione dedicata alla rappresentazione e alla risoluzione di sistemi circuitali, con un'impostazione aderente ai contenuti di **Elettrotecnica per Ingegneria Informatica**.
