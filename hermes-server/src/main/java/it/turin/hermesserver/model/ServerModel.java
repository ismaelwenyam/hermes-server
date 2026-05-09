package it.turin.hermesserver.model;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ServerModel {
    private final ObservableList<String> logs;
    public ServerModel() {
        logs = FXCollections.observableArrayList();
    }

    public ObservableList<String> getLogs() {
        return logs;
    }

    public void addLog(String log){
        Platform.runLater(() -> {
            logs.add(log);
        });
    }
}
