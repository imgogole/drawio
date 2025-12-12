package fr.polytech.wid.s7projectskribbl.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import fr.polytech.wid.s7projectskribbl.common.*;

public class ClientApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ClientView.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 400, 300);
        String cssPath = getClass().getResource("styles.css").toExternalForm();
        scene.getStylesheets().add(cssPath);
        primaryStage.setTitle(GameCommonMetadata.GameName);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}