module it.turin.hermesserver {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.google.gson;


    opens it.turin.hermesserver to javafx.fxml;
    opens it.turin.hermesserver.controller to javafx.fxml;
    opens it.turin.hermesserver.dto to com.google.gson;
    opens it.turin.hermesserver.model to com.google.gson, javafx.fxml;
    exports it.turin.hermesserver;
    exports it.turin.hermesserver.service;
    exports it.turin.hermesserver.controller;
    exports it.turin.hermesserver.dto;
    exports it.turin.hermesserver.model;
    exports it.turin.hermesserver.network;
    exports it.turin.hermesserver.persistence;
    exports it.turin.hermesserver.tasks;
    opens it.turin.hermesserver.service to com.google.gson, javafx.fxml;
}