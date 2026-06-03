package it.turin.hermesserver.controller;

import it.turin.hermesserver.model.ServerModel;
import it.turin.hermesserver.network.HermesServerSocket;
import it.turin.hermesserver.service.MailboxService;
import it.turin.hermesserver.service.RequestService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.util.Duration;

import java.util.Arrays;

/**
 * Controller JavaFX della vista principale del server.
 *
 * <p>Collega la lista dei log al {@link ServerModel}, inizializza i servizi
 * applicativi e avvia il socket server sulla porta configurata.</p>
 */
public class ServerViewController {

    private HermesServerSocket serverSocket;
    private ServerModel serverModel;
    private Timeline logFlushTimeline;

    @FXML
    private ListView<String> logListView;

    /**
     * Inizializza controller, servizi e server socket.
     *
     * <p>Il metodo collega la {@link ListView} alla lista osservabile dei log,
     * abilita lo scorrimento automatico verso l'ultimo messaggio, inizializza le
     * mailbox predefinite e avvia l'ascolto delle connessioni client.</p>
     *
     * @param serverModel modello condiviso con la vista
     * @param port porta TCP su cui avviare il server
     */
    public void init (ServerModel serverModel, int port) {
        this.serverModel = serverModel;
        ObservableList<String> ol = serverModel.getLogs();
        logListView.setItems(ol);
        ol.addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    logListView.scrollTo(ol.size() - 1);
                }
            }
        });
        logFlushTimeline = new Timeline(new KeyFrame(Duration.millis(100), event -> this.serverModel.flushLogs()));
        logFlushTimeline.setCycleCount(Animation.INDEFINITE);
        logFlushTimeline.play();
        MailboxService mailboxManager = new MailboxService(serverModel);
        RequestService service = new RequestService(mailboxManager, serverModel);
        boolean initResult = mailboxManager.initMailBoxes(Arrays.asList("ismael@hermes.it", "eileen@hermes.it", "francesco@hermes.it"));
        this.serverSocket =  new HermesServerSocket(port, serverModel, service, initResult);
        serverSocket.init();
    }

    /**
     * Arresta il server socket associato alla vista.
     */
    public void shutdown(){
        if (logFlushTimeline != null) {
            logFlushTimeline.stop();
        }
        serverSocket.stop();
    }
}
