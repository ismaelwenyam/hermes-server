module it.turin.hermesserver {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.google.gson;


    opens it.turin.hermesserver to javafx.fxml;
    opens it.turin.hermesserver.controller to javafx.fxml;
    opens it.turin.hermesserver.dto to com.google.gson;
    opens it.turin.hermesserver.model to com.google.gson;
    exports it.turin.hermesserver;
}