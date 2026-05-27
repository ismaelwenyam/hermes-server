package it.turin.hermesserver.controller;

import it.turin.hermesserver.service.MailboxService;
import it.turin.hermesserver.model.ServerModel;
import it.turin.hermesserver.network.HermesServerSocket;
import it.turin.hermesserver.service.RequestService;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import java.util.Arrays;

public class ServerViewController {

    private HermesServerSocket serverSocket;

    @FXML
    private ListView<String> logListView;

    public void init (ServerModel serverModel, int port) {
        ObservableList<String> ol = serverModel.getLogs();
        logListView.setItems(ol);
        ol.addListener((ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    Platform.runLater(() ->
                            logListView.scrollTo(ol.size() - 1)
                    );
                }
            }
        });
        MailboxService mailboxManager = new MailboxService(serverModel);
        RequestService service = new RequestService(mailboxManager, serverModel);
        boolean initResult = mailboxManager.initMailBoxes(Arrays.asList("ismael@hermes.it", "eileen@hermes.it", "francesco@hermes.it"));
        this.serverSocket =  new HermesServerSocket(port, serverModel, service, initResult);
        serverSocket.init();
    }

    public void shutdown(){
        serverSocket.stop();
    }
}
