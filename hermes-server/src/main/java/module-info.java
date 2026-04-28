module it.turin.hermesserver {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens it.turin.hermesserver to javafx.fxml;
    opens it.turin.hermesserver.controller to javafx.fxml;
    exports it.turin.hermesserver;
}