module it.turin.hermesserver {
    requires javafx.controls;
    requires javafx.fxml;


    opens it.turin.hermesserver to javafx.fxml;
    exports it.turin.hermesserver;
}