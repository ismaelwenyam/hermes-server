package it.turin.hermesserver;

import it.turin.hermesserver.controller.ServerViewController;
import it.turin.hermesserver.model.ServerModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HermesServerApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HermesServerApplication.class.getResource("server-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        ServerViewController controller = fxmlLoader.getController();
        controller.init(new ServerModel(), 8080);
        stage.setTitle("Hermes Server");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> controller.shutdown());
        stage.show();
    }
}
