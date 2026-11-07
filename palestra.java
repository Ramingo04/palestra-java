import java.util.*;

// ============================================================================
// PARTE 1: GERARCHIA PERSONE (Persona, Impiegato, Istruttore, Utente)
// ============================================================================

// Classe astratta Persona: fa da base per Utente, Impiegato e Istruttore
abstract class Persona {
    private String nome;
    private int eta;
    // final perché il codice fiscale non può cambiare una volta assegnato
    private final String codiceFiscale; 
    
    // Set statico per controllare che il codice fiscale sia davvero univoco tra tutte le persone create
    private static Set<String> codiciUsati = new HashSet<>();

    public Persona(String nome, int eta, String codiceFiscale) throws Exception {
        // Controllo univocità richiesto dalla traccia
        if (codiciUsati.contains(codiceFiscale)) {
            throw new Exception("Codice fiscale già esistente!");
        }
        codiciUsati.add(codiceFiscale);
        
        this.nome = nome;
        this.eta = eta;
        this.codiceFiscale = codiceFiscale;
    }

    // Getter base (servono alle sottoclassi poiché gli attributi sono private e non protected)
    public String getNome() { return nome; }
    public int getEta() { return eta; }
    public String getCodiceFiscale() { return codiceFiscale; }
}

// Classe Impiegato: estende Persona
class Impiegato extends Persona {
    private String ruolo;
    private double stipendio;
    private int oreStraordinario;

    public Impiegato(String nome, int eta, String codiceFiscale, String ruolo, double stipendio) throws Exception {
        super(nome, eta, codiceFiscale);
        this.ruolo = ruolo;
        this.stipendio = stipendio;
        this.oreStraordinario = 0; // Parte da 0
    }

    // Metodo per accumulare le ore di straordinario
    public void aggiungiStraordinario(int ore) {
        this.oreStraordinario += ore;
    }

    // Calcola la busta paga e azzera gli straordinari come richiesto dalla traccia
    public double calcolaBustaPaga() {
        double totale = this.stipendio + (this.oreStraordinario * 10.0);
        this.oreStraordinario = 0; // Azzeramento richiesto
        return totale;
    }
    
    public String getRuolo() { return ruolo; }
}

// Classe Istruttore: estende Persona
class Istruttore extends Persona {
    private double remunerazioneBase;
    private int numeroTicket;
    private List<Corso> corsiTitolare;

    public Istruttore(String nome, int eta, String codiceFiscale, double remunerazioneBase) throws Exception {
        super(nome, eta, codiceFiscale);
        this.remunerazioneBase = remunerazioneBase;
        this.numeroTicket = 0;
        this.corsiTitolare = new ArrayList<>();
    }

    // Metodo per accumulare ticket di supplenza
    public void aggiungiTicket(int ticket) {
        this.numeroTicket += ticket;
    }

    // Calcola la busta paga e azzera i ticket come richiesto dalla traccia
    public double calcolaBustaPaga() {
        double totale = this.remunerazioneBase + (this.corsiTitolare.size() * 200.0) + (this.numeroTicket * 10.0);
        this.numeroTicket = 0; // Azzeramento richiesto
        return totale;
    }

    // Metodo richiesto nelle precisazioni: assegna un corso in cui insegnare se non è già titolare
    public void insegno(Corso c) throws Exception {
        if (this.corsiTitolare.contains(c)) {
            throw new Exception("L'istruttore è già titolare di questo corso!");
        }
        this.corsiTitolare.add(c);
    }

    // Metodo richiesto nelle precisazioni: valuta in quale corso insegna e restituisce il numero di turni
    public int afferenza(Corso c) {
        // Se non insegna in questo corso, restituisce 0
        if (!this.corsiTitolare.contains(c)) {
            return 0;
        }
        // Conta in quanti turni di questo corso compare come istruttore
        int contaTurni = 0;
        for (Composizione comp : c.getTurni().values()) {
            if (comp.getIstruttore().equals(this)) {
                contaTurni++;
            }
        }
        return contaTurni;
    }
}

// Classe Utente: estende Persona
class Utente extends Persona {
    private double abbonamento;
    private String certificatoIdoneita; // Es. "studente", "gestante", ecc.
    private Corso corsoIscritto; // "Un utente può essere iscritto a un solo corso"

    public Utente(String nome, int eta, String codiceFiscale, double abbonamento, String certificatoIdoneita) throws Exception {
        super(nome, eta, codiceFiscale);
        this.abbonamento = abbonamento;
        this.certificatoIdoneita = certificatoIdoneita;
        this.corsoIscritto = null; // Inizialmente non è iscritto a nessun corso
    }

    public String getCertificatoIdoneita() { return certificatoIdoneita; }
    public Corso getCorsoIscritto() { return corsoIscritto; }
    public void setCorsoIscritto(Corso c) { this.corsoIscritto = c; }

    // Metodo richiesto nelle precisazioni: valuta se è iscritto al corso e restituisce il numero di turni
    public int afferenza() {
        if (this.corsoIscritto == null) {
            return 0;
        }
        int contaTurni = 0;
        // Scorre tutte le composizioni (i valori della mappa) del corso a cui è iscritto
        for (Composizione comp : this.corsoIscritto.getTurni().values()) {
            if (comp.getPartecipanti().contains(this)) {
                contaTurni++;
            }
        }
        return contaTurni;
    }
}

// ============================================================================
// PARTE 2: CORSI, TURNI E COMPOSIZIONE
// ============================================================================

// Classe Turno: fa da chiave per la Map del Corso (coppia orario + giorno)
class Turno {
    private String orario;
    private int giorno; // Valori da 1 a 6 (oppure Monday-Saturday)

    public Turno(String orario, int giorno) {
        this.orario = orario;
        this.giorno = giorno;
    }

    public String getOrario() { return orario; }
    public int getGiorno() { return giorno; }

    // equals e hashCode sono obbligatori quando si usa una classe creata da noi come chiave di una Map!
    // Nota: su NetBeans puoi generarli in 2 secondi con Tasto Destro -> Insert Code -> equals() and hashCode()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Turno turno = (Turno) o;
        return giorno == turno.giorno && Objects.equals(orario, turno.orario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orario, giorno);
    }

    @Override
    public String toString() {
        return "Giorno " + giorno + " ore " + orario;
    }
}

// Classe Composizione: fa da valore per la Map del Corso (contiene istruttore + lista partecipanti)
class Composizione {
    private Istruttore istruttore;
    private List<Utente> partecipanti;

    public Composizione(Istruttore istruttore) {
        this.istruttore = istruttore;
        this.partecipanti = new ArrayList<>(); // Lista inizialmente vuota
    }

    public Istruttore getIstruttore() { return istruttore; }
    public void setIstruttore(Istruttore istruttore) { this.istruttore = istruttore; }
    public List<Utente> getPartecipanti() { return partecipanti; }
}

// Classe Corso: gestisce i turni e le iscrizioni degli utenti
class Corso {
    private String nome;
    private final String codiceUnivoco; // final perché univoco e immutabile
    private int capacitaMassima;
    private List<String> requisitiIdoneita;
    private int etaMinima; // Vincolo sull'età
    private Map<Turno, Composizione> turni;

    private static Set<String> codiciCorsoUsati = new HashSet<>();

    // Costruttore: crea un corso vuoto (con mappa dei turni vuota)
    public Corso(String nome, String codiceUnivoco, int capacitaMassima, List<String> requisitiIdoneita, int etaMinima) throws Exception {
        if (codiciCorsoUsati.contains(codiceUnivoco)) {
            throw new Exception("Codice corso già esistente!");
        }
        codiciCorsoUsati.add(codiceUnivoco);

        this.nome = nome;
        this.codiceUnivoco = codiceUnivoco;
        this.capacitaMassima = capacitaMassima;
        this.requisitiIdoneita = requisitiIdoneita;
        this.etaMinima = etaMinima;
        this.turni = new HashMap<>();
    }

    public Map<Turno, Composizione> getTurni() { return turni; }
    public String getCodiceUnivoco() { return codiceUnivoco; }

    // Operazione 1: Aggiungere un turno specificando l'istruttore
    public void aggiungiTurno(Turno t, Istruttore i) {
        this.turni.put(t, new Composizione(i));
    }

    // Operazione 2: Cancellare un turno
    public void cancellaTurno(Turno t) {
        this.turni.remove(t);
    }

    // Operazione 3: Verificare la presenza di un utente tramite codice fiscale
    public boolean verificaPresenzaUtente(String codiceFiscale) {
        for (Composizione comp : this.turni.values()) {
            for (Utente u : comp.getPartecipanti()) {
                if (u.getCodiceFiscale().equals(codiceFiscale)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Metodo richiesto nelle precisazioni: valuta i requisiti di un utente rispetto al corso
    public boolean idoneo(Utente u) {
        // Controlla se l'età va bene e se il certificato dell'utente è nella lista dei requisiti del corso
        if (u.getEta() >= this.etaMinima && this.requisitiIdoneita.contains(u.getCertificatoIdoneita())) {
            return true;
        }
        return false;
    }

    // Operazione 4a e Precisazione: Inserire un utente in uno specifico turno (metodo iscrivi)
    public void iscrivi(Utente u, Turno t) throws Exception {
        Composizione comp = this.turni.get(t);
        if (comp == null) {
            throw new Exception("Turno non esistente!");
        }
        // Controllo precisazione: "Un utente può essere iscritto a un solo corso"
        if (u.getCorsoIscritto() != null && !u.getCorsoIscritto().equals(this)) {
            throw new Exception("L'utente è già iscritto a un altro corso!");
        }
        // Controllo idoneità richiesto dalla traccia
        if (!idoneo(u)) {
            throw new Exception("L'utente non è idoneo per questo corso!");
        }
        // Controllo capienza massima richiesto dalla traccia
        if (comp.getPartecipanti().size() >= this.capacitaMassima) {
            throw new Exception("Capienza massima raggiunta per questo turno!");
        }

        // Se tutti i controlli passano, iscrive l'utente al turno e registra il corso sull'utente
        comp.getPartecipanti().add(u);
        u.setCorsoIscritto(this);
    }

    // Operazione 4b: Cancellare un utente da uno specifico turno
    public void cancellaUtenteDaTurno(Utente u, Turno t) {
        Composizione comp = this.turni.get(t);
        if (comp != null) {
            comp.getPartecipanti().remove(u);
            // Se l'utente non frequenta più nessun turno di questo corso, liberiamo il suo slot corso
            if (u.afferenza() == 0) {
                u.setCorsoIscritto(null);
            }
        }
    }

    // Operazione 5: Cambiare l'istruttore di un turno
    public void cambiaIstruttore(Turno t, Istruttore nuovoIstruttore) {
        Composizione comp = this.turni.get(t);
        if (comp != null) {
            comp.setIstruttore(nuovoIstruttore);
        }
    }

    // Operazione 6: Stampare tutti i turni correnti con istruttore e numero di iscritti
    public void stampaTurni() {
        for (Map.Entry<Turno, Composizione> entry : this.turni.entrySet()) {
            Turno t = entry.getKey();
            Composizione c = entry.getValue();
            System.out.println("Turno: " + t + 
                               " | Istruttore: " + c.getIstruttore().getNome() + 
                               " | Numero Iscritti: " + c.getPartecipanti().size());
        }
    }
}

// ============================================================================
// PARTE 3: THREAD E CONCORRENZA
// ============================================================================

// Classe valore per la mappa concorrente
class ComposizioneOraria {
    private String codiceIstruttore; // formato "I" + index (0..3)
    private int numeroIscritti;      // max 10

    public ComposizioneOraria(String codiceIstruttore) {
        this.codiceIstruttore = codiceIstruttore;
        this.numeroIscritti = 0;
    }

    public String getCodiceIstruttore() { return codiceIstruttore; }
    public void setCodiceIstruttore(String codiceIstruttore) { this.codiceIstruttore = codiceIstruttore; }
    public int getNumeroIscritti() { return numeroIscritti; }
    public void incrementaIscritti() { this.numeroIscritti++; }
    public void decrementaIscritti() { this.numeroIscritti--; }
}

// Monitor che gestisce la mappa condivisa in modo thread-safe (con synchronized, wait e notifyAll)
class MonitorMap {
    private Map<Integer, ComposizioneOraria> map = new HashMap<>();
    private final int MAX = 10; // Costante richiesta dalla traccia

    // Metodo chiamato dal Thread Writer
    public synchronized void write(int turno, String codiceIstruttore) {
        if (map.containsKey(turno)) {
            // Se il turno è già presente, aggiorna il codiceIstruttore
            map.get(turno).setCodiceIstruttore(codiceIstruttore);
            System.out.println("[Writer] Aggiornato turno " + turno + " con istruttore " + codiceIstruttore);
        } else {
            // Altrimenti inserisce il nuovo turno
            map.put(turno, new ComposizioneOraria(codiceIstruttore));
            System.out.println("[Writer] Creato turno " + turno + " con istruttore " + codiceIstruttore);
        }
        notifyAll(); // Sveglia eventuali thread in attesa che il turno venisse creato
    }

    // Metodo chiamato dal Thread Producer
    public synchronized void produce(int turno) throws InterruptedException {
        // Se il turno non esiste ancora oppure ha raggiunto MAX (10) iscritti, deve attendere (wait)
        while (!map.containsKey(turno) || map.get(turno).getNumeroIscritti() >= MAX) {
            wait();
        }
        map.get(turno).incrementaIscritti();
        System.out.println("[Producer] Iscritto aggiunto al turno " + turno + ". Totale iscritti: " + map.get(turno).getNumeroIscritti());
        notifyAll(); // Sveglia i Consumer in attesa di iscritti da rimuovere
    }

    // Metodo chiamato dal Thread Consumer
    public synchronized void consume(int turno) throws InterruptedException {
        // Se il turno non esiste ancora oppure ha 0 iscritti, deve attendere (wait)
        while (!map.containsKey(turno) || map.get(turno).getNumeroIscritti() <= 0) {
            wait();
        }
        map.get(turno).decrementaIscritti();
        System.out.println("[Consumer] Iscritto rimosso dal turno " + turno + ". Totale iscritti: " + map.get(turno).getNumeroIscritti());
        notifyAll(); // Sveglia i Producer in attesa che si liberi un posto
    }

    // Metodo chiamato dal Thread Eraser
    public synchronized void erase() {
        map.clear();
        System.out.println("[Eraser] Configurazione del corso azzerata.");
        notifyAll();
    }

    // Metodo chiamato dal Thread Reader
    public synchronized void read() {
        System.out.println("--- [Reader] Lettura turni attivi ---");
        for (Map.Entry<Integer, ComposizioneOraria> entry : map.entrySet()) {
            System.out.println("Turno giornaliero: " + entry.getKey() + 
                               " | Istruttore: " + entry.getValue().getCodiceIstruttore() + 
                               " | Iscritti: " + entry.getValue().getNumeroIscritti());
        }
    }
}

// Thread Writer: inserisce o modifica un turno casuale (0-9) con istruttore casuale (I0-I3)
class Writer extends Thread {
    private MonitorMap monitor;
    private Random rand = new Random();

    public Writer(MonitorMap monitor) { this.monitor = monitor; }

    @Override
    public void run() {
        int turnoCasuale = rand.nextInt(10); // Valore tra 0 e 9
        int indexCasuale = rand.nextInt(4);  // Valore tra 0 e 3
        String codiceIstruttore = "I" + indexCasuale;
        monitor.write(turnoCasuale, codiceIstruttore);
    }
}

// Thread Producer: incrementa il numero di iscritti in un turno
class Producer extends Thread {
    private MonitorMap monitor;
    private int turno;

    public Producer(MonitorMap monitor, int turno) {
        this.monitor = monitor;
        this.turno = turno;
    }

    @Override
    public void run() {
        try {
            monitor.produce(turno);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// Thread Consumer: decrementa il numero di iscritti in un turno
class Consumer extends Thread {
    private MonitorMap monitor;
    private int turno;

    public Consumer(MonitorMap monitor, int turno) {
        this.monitor = monitor;
        this.turno = turno;
    }

    @Override
    public void run() {
        try {
            monitor.consume(turno);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// Thread Eraser: azzera la configurazione del corso
class Eraser extends Thread {
    private MonitorMap monitor;

    public Eraser(MonitorMap monitor) { this.monitor = monitor; }

    @Override
    public void run() {
        monitor.erase();
    }
}

// Thread Reader: legge i turni attivi
class Reader extends Thread {
    private MonitorMap monitor;

    public Reader(MonitorMap monitor) { this.monitor = monitor; }

    @Override
    public void run() {
        monitor.read();
    }
}

// ============================================================================
// MAIN DI PROVA (Facoltativo ma utile per testare che tutto funzioni)
// ============================================================================
public class EsamePalestra {
    public static void main(String[] args) {
        try {
            // Test rapido Parte 1 e 2
            Istruttore istr = new Istruttore("Mario Rossi", 35, "RSSMRA80A01C351X", 1200.0);
            Utente ut = new Utente("Luigi Bianchi", 22, "BNCLGU04A01C351Y", 50.0, "studente");

            List<String> req = new ArrayList<>();
            req.add("studente");

            Corso corso = new Corso("Pesi Base", "C01", 10, req, 18);
            istr.insegno(corso);

            Turno t1 = new Turno("10:00", 1);
            corso.aggiungiTurno(t1, istr);
            corso.iscrivi(ut, t1);
            corso.stampaTurni();

            // Test rapido Parte 3 (Thread)
            MonitorMap monitor = new MonitorMap();
            monitor.write(1, "I0"); // Inizializziamo il turno 1 per far lavorare subito Producer e Consumer
            
            Producer p = new Producer(monitor, 1);
            Consumer c = new Consumer(monitor, 1);
            Reader r = new Reader(monitor);

            p.start();
            c.start();
            r.start();

        } catch (Exception e) {
            System.out.println("Errore: " + e.getMessage());
        }
    }
}
