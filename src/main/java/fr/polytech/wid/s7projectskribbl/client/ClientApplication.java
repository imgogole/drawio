package fr.polytech.wid.s7projectskribbl.client;

import fr.polytech.wid.s7projectskribbl.client.network.ClientHandler;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import fr.polytech.wid.s7projectskribbl.common.*;

public class ClientApplication extends Application
{
    private static Stage stage;

    // CACHE : Stocke les vues déjà chargées (Nom du FXML -> Racine de la scène)
    private static final Map<String, Parent> sceneCache = new HashMap<>();

    @Override
    public void start(Stage primaryStage) throws IOException
    {
        stage = primaryStage;
        stage.setTitle(GameCommonMetadata.GameName);
        stage.setMaximized(true);

        try
        {
            PreloadScene("JoinRoomView.fxml", "styles.css");
            PreloadScene("WaitingRoomView.fxml", "styles.css");
            PreloadScene("GameView.fxml", "styles.css");
        }
        catch (Exception e)
        {
            System.err.println("Erreur fatal lors du préchargement : " + e.getMessage());
            e.printStackTrace();
        }

        LoadScene("JoinRoomView.fxml");

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
    public static void PreloadScene(String fxml, String css) throws IOException
    {
        if (sceneCache.containsKey(fxml)) return;
        FXMLLoader loader = new FXMLLoader(ClientApplication.class.getResource(fxml));
        Parent root = loader.load();
        URL cssURL = ClientApplication.class.getResource(css);
        if (cssURL != null) {
            root.getStylesheets().add(cssURL.toExternalForm());
        }

        sceneCache.put(fxml, root);
    }

    public static void LoadScene(String fxml) throws IOException
    {
        LoadScene(fxml, "styles.css");
    }

    public static void LoadScene(String fxml, String css) throws IOException
    {
        Parent root;

        if (sceneCache.containsKey(fxml))
        {
            root = sceneCache.get(fxml);
        }
        else
        {
            FXMLLoader loader = new FXMLLoader(ClientApplication.class.getResource(fxml));
            root = loader.load();

            URL cssURL = ClientApplication.class.getResource(css);
            if (cssURL != null) {
                root.getStylesheets().add(cssURL.toExternalForm());
            }
        }

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
    }
}