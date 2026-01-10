package fr.polytech.wid.s7projectskribbl.client;

import fr.polytech.wid.s7projectskribbl.client.network.ClientHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

import fr.polytech.wid.s7projectskribbl.common.*;

public class ClientApplication extends Application
{
    private static Stage stage;

    @Override
    public void start(Stage primaryStage) throws IOException
    {
        stage = primaryStage;
        LoadScene("GameView.fxml");
        stage.setTitle(GameCommonMetadata.GameName);
        stage.setMaximized(true);

        stage.setOnCloseRequest(event ->
        {
            try
            {
                ClientHandler.Singleton().DisconnectAndStop();
            }
            catch (Exception e)
            {
                System.err.println("Erreur lors de la déconnexion : " + e.getMessage());
            }
            System.exit(0);
        });
        stage.show();
    }

    public static void LoadScene(String fxml) throws IOException
    {
        LoadScene(fxml, "styles.css");
    }

    public static void LoadScene(String fxml, String css) throws IOException
    {
        FXMLLoader loader = new FXMLLoader(
                ClientApplication.class.getResource(fxml)
        );
        Parent root = loader.load();

        Scene scene = stage.getScene();
        if (scene == null)
        {
            scene = new Scene(root);
            stage.setScene(scene);
        }
        else
        {
            scene.setRoot(root);
        }

        URL cssURL = ClientApplication.class.getResource(css);
        if (cssURL != null)
        {
            String cssPath = cssURL.toExternalForm();;
            scene.getStylesheets().setAll(cssPath);
        }
    }
}
