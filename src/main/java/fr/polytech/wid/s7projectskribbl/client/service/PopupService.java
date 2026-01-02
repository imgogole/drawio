package fr.polytech.wid.s7projectskribbl.client.service;

import fr.polytech.wid.s7projectskribbl.client.controller.PopupController;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

public class PopupService {

    /**
     * Affiche une popup centrée avec animation élastique et fond assombri.
     */
    public static void showError(String title, String message, Window ownerWindow) {
        try {
            // --- CHARGEMENT ---
            String fxmlPath = "/fr/polytech/wid/s7projectskribbl/client/PopupView.fxml";
            String cssPath  = "/fr/polytech/wid/s7projectskribbl/client/styles.css";

            FXMLLoader loader = new FXMLLoader(PopupService.class.getResource(fxmlPath));
            Parent content = loader.load();

            // Configuration du contrôleur
            PopupController controller = loader.getController();
            if (controller != null) {
                controller.setContent(title, message);
            }

            // ---  CRÉATION DU FOND NOIR (OVERLAY) ---
            Region overlay = new Region();
            overlay.setStyle("-fx-background-color: black;");
            overlay.setOpacity(0); // Invisible au début pour l'animation

            // --- ASSEMBLAGE DANS LE STACKPANE ---
            // On ajoute d'abord l'overlay (fond), puis le content (popup) par-dessus
            StackPane rootContainer = new StackPane();
            rootContainer.getChildren().addAll(overlay, content);

            rootContainer.setStyle("-fx-background-color: transparent;");
            rootContainer.setPadding(new Insets(20)); // Marge pour l'effet rebond

            // --- CONFIGURATION RENDU  ---
            if (content instanceof Region) {
                ((Region) content).setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            }

            // --- SCÈNE ET STAGE ---
            Scene scene = new Scene(rootContainer);
            scene.setFill(Color.TRANSPARENT);
            if (PopupService.class.getResource(cssPath) != null) {
                scene.getStylesheets().add(PopupService.class.getResource(cssPath).toExternalForm());
            }

            Stage popupStage = new Stage();
            popupStage.initOwner(ownerWindow);
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.TRANSPARENT);
            popupStage.setScene(scene);

            if (controller != null) {
                controller.setStage(popupStage);
            }

            // --- PRÉPARATION DES ANIMATIONS ---

            // Animation de la Popup
            content.setScaleX(0);
            content.setScaleY(0);
            content.setOpacity(0);

            ScaleTransition scaleAnim = new ScaleTransition(Duration.seconds(0.3), content);
            scaleAnim.setToX(1);
            scaleAnim.setToY(1);
            scaleAnim.setInterpolator(CUSTOM_ELASTIC);

            // Animation du Fond (Fondu vers 60% d'opacité)
            FadeTransition fadeAnim = new FadeTransition(Duration.seconds(0.3), overlay);
            fadeAnim.setFromValue(0);
            fadeAnim.setToValue(0.6);

            // On combine les deux
            ParallelTransition openTransition = new ParallelTransition(scaleAnim, fadeAnim);

            // --- GESTION DE L'APPARITION ET POSITION ---
            popupStage.setOnShown(event -> {
                if (ownerWindow != null) {
                    Scene parentScene = ownerWindow.getScene();

                    double absoluteX = ownerWindow.getX() + parentScene.getX();
                    double absoluteY = ownerWindow.getY() + parentScene.getY();

                    popupStage.setX(absoluteX);
                    popupStage.setY(absoluteY);

                    // On prend la largeur/hauteur du CONTENU (Scene)
                    popupStage.setWidth(parentScene.getWidth());
                    popupStage.setHeight(parentScene.getHeight());
                }

                // On rend le contenu visible et on lance l'animation groupée
                content.setOpacity(1);
                openTransition.play();
            });

            // --- AFFICHAGE ---
            popupStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Interpolateur personnalisé "Elastic Out"
    private static final Interpolator CUSTOM_ELASTIC = new Interpolator() {
        @Override
        protected double curve(double t) {
            return (3 * (1 - t) * (1 - t) * t * -0.6) +
                    (3 * (1 - t) * t * t * 1.6) +
                    (t * t * t);
        }
    };
}