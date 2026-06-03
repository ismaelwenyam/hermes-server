package it.turin.hermesserver.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Modello condiviso dell'applicazione server.
 *
 * <p>Espone una lista osservabile di log usata dalla vista JavaFX per mostrare
 * lo stato del server e le operazioni eseguite dai thread di servizio.</p>
 */
public class ServerModel {
    private static final DateTimeFormatter LOG_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss.SSS");
    private final ObservableList<String> logs;
    private final BlockingQueue<String> pendingLogs;

    /**
     * Crea il modello inizializzando la lista osservabile dei log.
     */
    public ServerModel() {
        logs = FXCollections.observableArrayList();
        pendingLogs = new LinkedBlockingQueue<>();
    }

    /**
     * Restituisce la lista osservabile dei log.
     *
     * @return lista osservabile aggiornata dal server e mostrata dalla vista
     */
    public ObservableList<String> getLogs() {
        return logs;
    }

    /**
     * Accoda un messaggio di log in modo thread-safe.
     *
     * <p>La coda viene svuotata dal thread JavaFX tramite {@link #flushLogs()}.</p>
     *
     * @param log messaggio da visualizzare nella lista dei log
     */
    public void addLog(String log){
        String timestamp = ZonedDateTime.now(ZoneId.systemDefault()).format(LOG_TIME_FORMAT);
        pendingLogs.offer(timestamp + " - " + log);
    }

    /**
     * Trasferisce i log in coda nella lista osservabile.
     *
     * <p>Questo metodo va chiamato dal thread JavaFX.</p>
     *
     * @return {@code true} se almeno un messaggio è stato aggiunto alla lista
     */
    public boolean flushLogs() {
        List<String> batch = new ArrayList<>();
        pendingLogs.drainTo(batch);

        if (batch.isEmpty()) {
            return false;
        }

        logs.addAll(batch);
        return true;
    }
}
