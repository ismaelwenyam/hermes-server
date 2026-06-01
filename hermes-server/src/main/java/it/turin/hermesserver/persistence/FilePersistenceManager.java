package it.turin.hermesserver.persistence;

import it.turin.hermesserver.model.Email;
import it.turin.hermesserver.model.MailboxMetadata;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * La classe {@code FilePersistenceManager} gestisce la persistenza su file system delle mailbox del server.
 *
 * <p>Questa classe incapsula le operazioni di basso livello necessarie per
 * creare directory, salvare, leggere ed eliminare email e metadati. Gli oggetti
 * vengono serializzati tramite {@link ObjectOutputStream} e riletti tramite
 * {@link ObjectInputStream}; di conseguenza i tipi persistiti devono implementare
 * {@link Serializable}.</p>
 *
 * <p>La classe non applica sincronizzazione interna. La gestione della
 * concorrenza deve quindi essere effettuata dal chiamante.</p>
 */
public class FilePersistenceManager {


    /**
     * Verifica se il percorso specificato esiste ed è una directory.
     *
     * @param pathname percorso della directory da controllare
     * @return {@code true} se il percorso esiste ed è una directory,
     *         {@code false} altrimenti
     */
    public boolean directoryExists (String pathname){
        File file = new File(pathname);
        return file.isDirectory();
    }

    /**
     * Crea la directory indicata e tutte le eventuali directory padre mancanti.
     *
     * <p>Il metodo delega la creazione a {@link File#mkdirs()} e non segnala
     * come errore il caso in cui la directory esista già.</p>
     *
     * @param pathname percorso della directory da creare
     */
    public void createDirectory (String pathname) {
        File file = new File(pathname);
        if (file.mkdirs()) System.out.println(Thread.currentThread().getName() + " created directory: " + pathname);
    }

    /**
     * Scrive un'email serializzata nella directory specificata.
     *
     * <p>Il nome del file viene costruito concatenando {@code fileId} ed
     * {@code extension}. Se {@code data} è {@code null} o se il file di
     * destinazione esiste già, il metodo non scrive nulla e restituisce
     * {@code false}.</p>
     *
     * @param data email da salvare
     * @param fileId identificativo usato come nome base del file
     * @param pathname directory in cui salvare l'email
     * @param extension estensione del file, ad esempio {@code ".bin"}
     * @return {@code true} se l'email è stata salvata correttamente,
     *         {@code false} se l'input è nullo o il file esiste già
     * @throws IOException se si verifica un errore durante la scrittura
     */
    public boolean writeEmail(Email data, String fileId, String pathname, String extension) throws IOException {
        if (data == null) return false;
        String filename = fileId.concat(extension);
        File file = new File(pathname, filename);
        if (file.exists()) {
            return false;
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(data);
            out.flush();
        } catch (IOException ex) {
            throw new IOException("storing data: " + data + " to: " + file.getCanonicalPath());
        }
        return true;
    }


    /**
     * Scrive i metadati di una mailbox nella directory specificata.
     *
     * <p>Il nome del file viene costruito concatenando {@code fileId} ed
     * {@code extension}. Se il file esiste già e {@code update} è {@code false},
     * il metodo non sovrascrive il contenuto esistente. Se {@code data} è
     * {@code null}, il metodo termina senza effettuare operazioni.</p>
     *
     * @param data metadati della mailbox da salvare
     * @param fileId identificativo usato come nome base del file
     * @param pathname directory in cui salvare i metadati
     * @param extension estensione del file, ad esempio {@code ".bin"}
     * @param update se {@code true}, consente di sovrascrivere un file esistente
     * @throws IOException se si verifica un errore durante la scrittura
     */
    public void writeMetadata(MailboxMetadata data, String fileId, String pathname, String extension, boolean update) throws IOException {
        if (data == null) return;
        String filename = fileId.concat(extension);
        File file = new File(pathname, filename);
        if (file.exists() && !update) {
            return;
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(data);
            out.flush();
        } catch (IOException ex) {
            throw new IOException("storing data: " + data + " to: " + file.getCanonicalPath());
        }
    }

    /**
     * Elimina un'email dalla directory specificata.
     *
     * <p>Il file da eliminare viene individuato concatenando {@code filename}
     * ed {@code extension}. Il metodo non elimina directory e restituisce
     * {@code false} se il file non esiste.</p>
     *
     * @param filename nome base del file da eliminare
     * @param pathname directory che contiene il file
     * @param extension estensione del file, ad esempio {@code ".bin"}
     * @return {@code true} se il file è stato eliminato, {@code false} altrimenti
     */
    public boolean removeEmail (String filename, String pathname, String extension) {
        File file = new File(pathname, filename.concat(extension));
        if (file.isDirectory() || !file.exists()) return false;
        return file.delete();
    }


    /**
     * Legge una pagina di email dalla directory specificata.
     *
     * <p>I file presenti nella directory vengono ordinati in base al valore
     * numerico del nome file, in ordine decrescente, così da restituire prima
     * le email più recenti. Il metodo assume che i file email abbiano nomi
     * numerici con estensione {@code ".bin"}.</p>
     *
     * <p>La pagina parte dall'indice {@code page * nrElements}. Se il percorso
     * non esiste, è un file o la directory non è leggibile, viene restituita una
     * lista vuota.</p>
     *
     * @param pathname directory che contiene i file delle email
     * @param nrElements numero di elementi richiesti per pagina
     * @param page indice della pagina da leggere, a partire da {@code 0}
     * @return lista di email deserializzate, oppure lista vuota se il percorso
     *         non è una directory valida
     * @throws Exception se si verifica un errore durante la lettura o la
     *                   deserializzazione dei file
     */
    public List<Email> readEmails(String pathname, int nrElements, int page) throws Exception {
        File dir = new File(pathname);
        if (dir.isFile() || !dir.exists()) return Collections.emptyList();
        String[] files = dir.list();
        if (files == null) return Collections.emptyList();
        Arrays.sort(files, (a, b) -> {
            int n1 = Integer.parseInt(a.replace(".bin", ""));
            int n2 = Integer.parseInt(b.replace(".bin", ""));
            // sort: desc order (n2, n1). asc order (n1, n2)
            return Integer.compare(n2, n1);
        });
        int from = page * nrElements;
        int to = Math.min(from + nrElements, files.length);
        List<String> filenames = new ArrayList<>();
        for (int i = from, j = 0; i < to; i++, j++) {
            filenames.add(files[i]);
        }
        return getEmails(pathname, filenames);
    }

    public List<Email> readEmail(String pathname) throws Exception {
        File dir = new File(pathname);
        if (dir.isFile() || !dir.exists()) return Collections.emptyList();
        String[] files = dir.list();
        if (files == null) return Collections.emptyList();
        Arrays.sort(files, (a, b) -> {
            int n1 = Integer.parseInt(a.replace(".bin", ""));
            int n2 = Integer.parseInt(b.replace(".bin", ""));
            // sort: desc order (n2, n1). asc order (n1, n2)
            return Integer.compare(n2, n1);
        });
        List<String> filenames = new ArrayList<>();
        filenames.add(files[0]);
        return getEmails(pathname, filenames);
    }

    /**
     * Deserializza le email corrispondenti ai nomi file indicati.
     *
     * @param pathname directory che contiene i file da leggere
     * @param filenames nomi dei file email da deserializzare
     * @return lista di email lette dai file
     * @throws Exception se un file non è leggibile o non contiene un oggetto
     *                   {@link Email} valido
     */
    private static List<Email> getEmails(String pathname, List<String> filenames) throws Exception {
        List<Email> result = new ArrayList<>();
        for (String filename : filenames) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(pathname, filename)))) {
                Email data = (Email) in.readObject();
                result.add(data);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("reading email: " + e.getMessage());
                throw new Exception("error occured reading emails");
            }
        }
        return result;
    }

    /**
     * Legge i metadati di una mailbox da file system.
     *
     * <p>Il parametro deve puntare direttamente al file dei metadati, non alla
     * directory che lo contiene. Se il percorso non esiste o rappresenta una
     * directory, il metodo restituisce {@code null}.</p>
     *
     * @param pathname percorso completo del file dei metadati
     * @return metadati deserializzati, oppure {@code null} se il file non esiste
     *         o il percorso indica una directory
     * @throws Exception se si verifica un errore durante la lettura o la
     *                   deserializzazione del file
     */
    public MailboxMetadata readMetadata(String pathname) throws Exception {
        File file = new File(pathname);
        if (file.isDirectory() || !file.exists()) return null;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))){
            return (MailboxMetadata) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new Exception("error occured reading metadata");
        }
    }
}
