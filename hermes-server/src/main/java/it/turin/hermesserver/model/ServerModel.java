package it.turin.hermesserver.model;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Modello condiviso dell'applicazione server.
 *
 * <p>Espone una lista osservabile di log usata dalla vista JavaFX per mostrare
 * lo stato del server e le operazioni eseguite dai thread di servizio.</p>
 */
public class ServerModel {
    private final ObservableList<String> logs;

    /**
     * Crea il modello inizializzando la lista osservabile dei log.
     */
    public ServerModel() {
        logs = FXCollections.observableArrayList();
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
     * Aggiunge un messaggio di log nel thread JavaFX.
     *
     * @param log messaggio da visualizzare nella lista dei log
     */
    public void addLog(String log){
        Platform.runLater(() -> {
            logs.add(log);
        });
    }
}
