import java.util.*;

// ============================================================================
// PARTE 1: GERARCHIA PERSONE (Persona, Impiegato, Istruttore, Utente)
// ============================================================================

// Classe astratta Persona: fa da base per Utente, Impiegato e Istruttore
abstract class Persona {
    private String nome;
    private int eta;
    // final perché il codice fiscale è univoco e non cambia mai
    private final String codiceFiscale; 
    
    // Set statico per controllare l'univocità del codice fiscale tra tutte le persone
    private static Set<String> codiciUsati = new HashSet<>();

    public Persona(String nome, int eta, String codiceFiscale) throws Exception {
        if (codiciUsati.contains(codiceFiscale)) {
            throw new Exception("Codice fiscale già esistente!");
        }
        codiciUsati.add(codiceFiscale);
        
        this.nome = nome;
        this.eta = eta;
        this.codiceFiscale = codiceFiscale;
    }

    // Metodi get per leggere gli attributi privati dall'esterno
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

    // Accumula le ore di straordinario
    public void aggiungiStraordinario(int ore) {
        oreStraordinario += ore;
    }

    // Calcola la busta paga e azzera gli straordinari come richiesto dalla traccia
    public double calcolaBustaPaga() {
        double totale = stipendio + (oreStraordinario * 10.0);
        oreStraordinario = 0; // Azzeramento richiesto
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
        numeroTicket = 0;
        corsiTitolare = new ArrayList<>();
    }

    // Accumula i ticket di supplenza
    public void aggiungiTicket(int ticket) {
        numeroTicket += ticket;
    }

    // Calcola la busta paga e azzera i ticket come richiesto dalla traccia
    public double calcolaBustaPaga() {
        double totale = remunerazioneBase + (corsiTitolare.size() * 200.0) + (numeroTicket * 10.0);
        numeroTicket = 0; // Azzeramento richiesto
        return totale;
    }

    // Assegna un corso in cui l'istruttore è titolare
    public void insegno(Corso c) throws Exception {
        if (corsiTitolare.contains(c)) {
            throw new Exception("L'istruttore è già titolare di questo corso!");
        }
        corsiTitolare.add(c);
    }

    // Restituisce il numero di turni in cui insegna in un determinato corso
    public int afferenza(Corso c) {
        if (!corsiTitolare.contains(c)) {
            return 0;
        }
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
    private String certificatoIdoneita; 
    private Corso corsoIscritto; // "Un utente può essere iscritto a un solo corso"

    public Utente(String nome, int eta, String codiceFiscale, double abbonamento, String certificatoIdoneita) throws Exception {
        super(nome, eta, codiceFiscale);
        this.abbonamento = abbonamento;
        this.certificatoIdoneita = certificatoIdoneita;
        corsoIscritto = null; // Inizialmente non è iscritto a nessun corso
    }

    public String getCertificatoIdoneita() { return certificatoIdoneita; }
    public Corso getCorsoIscritto() { return corsoIscritto; }
    public void setCorsoIscritto(Corso corsoIscritto) { this.corsoIscritto = corsoIscritto; }

    // Valuta se è iscritto a un corso e restituisce il numero di turni a cui partecipa
    public int afferenza() {
        if (corsoIscritto == null) {
            return 0;
        }
        int contaTurni = 0;
        for (Composizione comp : corsoIscritto.getTurni().values()) {
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
    private int giorno; // Valori da 1 a 6

    public Turno(String orario, int giorno) {
        this.orario = orario;
        this.giorno = giorno;
    }

    public String getOrario() { return orario; }
    public int getGiorno() { return giorno; }

    // equals e hashCode obbligatori perché usiamo questa classe come chiave di una Map
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

// Classe Composizione: valore associato al Turno nella Map del Corso
class Composizione {
    private Istruttore istruttore;
    private List<Utente> partecipanti;

    public Composizione(Istruttore istruttore) {
        this.istruttore = istruttore;
        partecipanti = new ArrayList<>(); // Lista inizialmente vuota
    }

    public Istruttore getIstruttore() { return istruttore; }
    public void setIstruttore(Istruttore istruttore) { this.istruttore = istruttore; }
    public List<Utente> getPartecipanti() { return partecipanti; }
}

// Classe Corso: gestisce i turni, le iscrizioni e i controlli
class Corso {
    private String nome;
    private final String codiceUnivoco; // final perché univoco
    private int capacitaMassima;
    private List<String> requisitiIdoneita;
    private int etaMinima; 
    private Map<Turno, Composizione> turni;

    private static Set<String> codiciCorsoUsati = new HashSet<>();

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
        turni = new HashMap<>();
    }

    public Map<Turno, Composizione> getTurni() { return turni; }
    public String getCodiceUnivoco() { return codiceUnivoco; }

    // Aggiunge un turno specificando l'istruttore
    public void aggiungiTurno(Turno t, Istruttore i) {
        turni.put(t, new Composizione(i));
    }

    // Cancella un turno
    public void cancellaTurno(Turno t) {
        turni.remove(t);
    }

    // Verifica la presenza di un utente tramite codice fiscale in tutti i turni del corso
    public boolean verificaPresenzaUtente(String codiceFiscale) {
        for (Composizione comp : turni.values()) {
            for (Utente u : comp.getPartecipanti()) {
                if (u.getCodiceFiscale().equals(codiceFiscale)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Valuta i requisiti di un utente rispetto al corso (età e certificato)
    public boolean idoneo(Utente u) {
        if (u.getEta() >= etaMinima && requisitiIdoneita.contains(u.getCertificatoIdoneita())) {
            return true;
        }
        return false;
    }

    // Inserisce un utente nel turno verificando spazio, idoneità e vincolo di un solo corso
    public void iscrivi(Utente u, Turno t) throws Exception {
        Composizione comp = turni.get(t);
        if (comp == null) {
            throw new Exception("Turno non esistente!");
        }
        // Controllo: un utente può essere iscritto a un solo corso
        if (u.getCorsoIscritto() != null && u.getCorsoIscritto() != this) {
            throw new Exception("L'utente è già iscritto a un altro corso!");
        }
        // Controllo idoneità richiesto dalla traccia
        if (!idoneo(u)) {
            throw new Exception("L'utente non è idoneo per questo corso!");
        }
        // Controllo capienza massima richiesto dalla traccia
        if (comp.getPartecipanti().size() >= capacitaMassima) {
            throw new Exception("Capienza massima raggiunta per questo turno!");
        }

        // Se tutti i controlli passano, aggiunge l'utente
        comp.getPartecipanti().add(u);
        u.setCorsoIscritto(this);
    }

    // Cancella un utente da uno specifico turno
    public void cancellaUtenteDaTurno(Utente u, Turno t) {
        Composizione comp = turni.get(t);
        if (comp != null) {
            comp.getPartecipanti().remove(u);
            if (u.afferenza() == 0) {
                u.setCorsoIscritto(null);
            }
        }
    }

    // Cambia l'istruttore di un turno
    public void cambiaIstruttore(Turno t, Istruttore nuovoIstruttore) {
        Composizione comp = turni.get(t);
        if (comp != null) {
            comp.setIstruttore(nuovoIstruttore);
        }
    }

    // Stampa tutti i turni con istruttore e numero di iscritti
    public void stampaTurni() {
        for (Map.Entry<Turno, Composizione> entry : turni.entrySet()) {
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

// Valore per la mappa concorrente dei thread
class ComposizioneOraria {
    private String codiceIstruttore; 
    private int numeroIscritti;      

    public ComposizioneOraria(String codiceIstruttore) {
        this.codiceIstruttore = codiceIstruttore;
        numeroIscritti = 0;
    }

    public String getCodiceIstruttore() { return codiceIstruttore; }
    public void setCodiceIstruttore(String codiceIstruttore) { this.codiceIstruttore = codiceIstruttore; }
    public int getNumeroIscritti() { return numeroIscritti; }
    public void incrementaIscritti() { numeroIscritti++; }
    public void decrementaIscritti() { numeroIscritti--; }
}

// Monitor condiviso per gestire l'accesso sicuro dei thread
class MonitorMap {
    private Map<Integer, ComposizioneOraria> map = new HashMap<>();
    private final int MAX = 10; // Limite massimo iscritti richiesto dalla traccia

    public synchronized void write(int turno, String codiceIstruttore) {
        if (map.containsKey(turno)) {
            map.get(turno).setCodiceIstruttore(codiceIstruttore);
            System.out.println("[Writer] Aggiornato turno " + turno + " con istruttore " + codiceIstruttore);
        } else {
            map.put(turno, new ComposizioneOraria(codiceIstruttore));
            System.out.println("[Writer] Creato turno " + turno + " con istruttore " + codiceIstruttore);
        }
        notifyAll();
    }

    public synchronized void produce(int turno) throws InterruptedException {
        // Se il turno non esiste o ha raggiunto il massimo, il thread attende (wait)
        while (!map.containsKey(turno) || map.get(turno).getNumeroIscritti() >= MAX) {
            wait();
        }
        map.get(turno).incrementaIscritti();
        System.out.println("[Producer] Iscritto aggiunto al turno " + turno + ". Totale: " + map.get(turno).getNumeroIscritti());
        notifyAll();
    }

    public synchronized void consume(int turno) throws InterruptedException {
        // Se il turno non esiste o ha 0 iscritti, il thread attende (wait)
        while (!map.containsKey(turno) || map.get(turno).getNumeroIscritti() <= 0) {
            wait();
        }
        map.get(turno).decrementaIscritti();
        System.out.println("[Consumer] Iscritto rimosso dal turno " + turno + ". Totale: " + map.get(turno).getNumeroIscritti());
        notifyAll();
    }

    public synchronized void erase() {
        map.clear();
        System.out.println("[Eraser] Configurazione azzerata.");
        notifyAll();
    }

    public synchronized void read() {
        System.out.println("--- [Reader] Lettura turni attivi ---");
        for (Map.Entry<Integer, ComposizioneOraria> entry : map.entrySet()) {
            System.out.println("Turno: " + entry.getKey() + 
                               " | Istruttore: " + entry.getValue().getCodiceIstruttore() + 
                               " | Iscritti: " + entry.getValue().getNumeroIscritti());
        }
    }
}

// Thread Writer
class Writer extends Thread {
    private MonitorMap monitor;
    private Random rand = new Random();

    public Writer(MonitorMap monitor) { this.monitor = monitor; }

    @Override
    public void run() {
        int turnoCasuale = rand.nextInt(10);
        int indexCasuale = rand.nextInt(4);
        monitor.write(turnoCasuale, "I" + indexCasuale);
    }
}

// Thread Producer
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

// Thread Consumer
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

// Thread Eraser
class Eraser extends Thread {
    private MonitorMap monitor;

    public Eraser(MonitorMap monitor) { this.monitor = monitor; }

    @Override
    public void run() {
        monitor.erase();
    }
}

// Thread Reader
class Reader extends Thread {
    private MonitorMap monitor;

    public Reader(MonitorMap monitor) { this.monitor = monitor; }

    @Override
    public void run() {
        monitor.read();
    }
}

// ============================================================================
// MAIN DI PROVA
// ============================================================================
public class EsamePalestra {
    public static void main(String[] args) {
        try {
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

            // Test Thread
            MonitorMap monitor = new MonitorMap();
            monitor.write(1, "I0");
            
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
