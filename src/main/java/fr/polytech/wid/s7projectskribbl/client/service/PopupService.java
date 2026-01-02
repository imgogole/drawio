package fr.polytech.wid.s7projectskribbl.client.service;

import fr.polytech.wid.s7projectskribbl.client.controller.PopupController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class PopupService {

    /**
     * Affiche une popup centrée.
     */
    public static void showError(String title, String message, Window ownerWindow) {
        try {
            // Chemins
            String fxmlPath = "/fr/polytech/wid/s7projectskribbl/client/PopupView.fxml";
            String cssPath  = "/fr/polytech/wid/s7projectskribbl/client/styles.css";

            // Chargement FXML
            FXMLLoader loader = new FXMLLoader(PopupService.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Configuration du message
            PopupController controller = loader.getController();
            if (controller != null) {
                controller.setContent(title, message);
            }

            // Création de la scène
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            scene.getStylesheets().add(PopupService.class.getResource(cssPath).toExternalForm());

            // Création de la fenêtre
            Stage popupStage = new Stage();
            popupStage.initOwner(ownerWindow);
            popupStage.initModality(Modality.APPLICATION_MODAL); // Bloque la fenêtre derrière
            popupStage.initStyle(StageStyle.TRANSPARENT);        // Sans barre Windows
            popupStage.setScene(scene);


            // On calcule la position exacte au moment où la fenêtre s'apprête à s'afficher
            popupStage.setOnShown(event -> {
                // Centre X = X_Parent + (Largeur_Parent / 2) - (Largeur_Popup / 2)
                double x = ownerWindow.getX() + (ownerWindow.getWidth() / 2) - (popupStage.getWidth() / 2);

                // Centre Y = Y_Parent + (Hauteur_Parent / 2) - (Hauteur_Popup / 2)
                double y = ownerWindow.getY() + (ownerWindow.getHeight() / 2) - (popupStage.getHeight() / 2);

                popupStage.setX(x);
                popupStage.setY(y);
            });

            // Affichage
            popupStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}