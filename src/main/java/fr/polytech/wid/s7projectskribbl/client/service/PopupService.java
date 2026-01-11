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

import java.util.List;

public class PopupService {

    /**
     * Affiche une popup centrée. La fenêtre parente est détectée automatiquement.
     * ATTENTION : Doit être appelé dans le Thread JavaFX (Platform.runLater).
     */
    public static PopupController showPopup(String title, String message, boolean hasButton) {
        try {
            // --- 1. RECUPERATION AUTOMATIQUE DE LA FENETRE ---
            // On cherche la fenêtre qui est actuellement visible et (idéalement) focus
            Window ownerWindow = Stage.getWindows().stream()
                    .filter(Window::isShowing)
                    .filter(Window::isFocused)
                    .findFirst()
                    .orElse(null);

            // Si aucune fenêtre n'a le focus (ex: clic hors appli), on prend la première visible
            if (ownerWindow == null) {
                ownerWindow = Stage.getWindows().stream()
                        .filter(Window::isShowing)
                        .findFirst()
                        .orElse(null);
            }

            // --- CHARGEMENT ---
            String fxmlPath = "/fr/polytech/wid/s7projectskribbl/client/PopupView.fxml";
            String cssPath  = "/fr/polytech/wid/s7projectskribbl/client/styles.css";

            FXMLLoader loader = new FXMLLoader(PopupService.class.getResource(fxmlPath));
            Parent content = loader.load();

            // Configuration du contrôleur
            PopupController controller = loader.getController();
            if (controller != null) {
                controller.setContent(title, message);
                controller.setButtonVisible(hasButton);
            }

            // ---  CRÉATION DU FOND NOIR (OVERLAY) ---
            Region overlay = new Region();
            overlay.setStyle("-fx-background-color: black;");
            overlay.setOpacity(0);

            // --- ASSEMBLAGE ---
            StackPane rootContainer = new StackPane();
            rootContainer.getChildren().addAll(overlay, content);
            rootContainer.setStyle("-fx-background-color: transparent;");
            rootContainer.setPadding(new Insets(20));

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

            // Si on a trouvé une fenêtre parente, on s'y attache
            if (ownerWindow != null) {
                popupStage.initOwner(ownerWindow);
            }

            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.TRANSPARENT);
            popupStage.setScene(scene);

            if (controller != null) {
                controller.setStage(popupStage);
            }

            // --- ANIMATIONS ---
            content.setScaleX(0);
            content.setScaleY(0);
            content.setOpacity(0);

            ScaleTransition scaleAnim = new ScaleTransition(Duration.seconds(0.3), content);
            scaleAnim.setToX(1);
            scaleAnim.setToY(1);
            scaleAnim.setInterpolator(CUSTOM_ELASTIC);

            FadeTransition fadeAnim = new FadeTransition(Duration.seconds(0.3), overlay);
            fadeAnim.setFromValue(0);
            fadeAnim.setToValue(0.6);

            ParallelTransition openTransition = new ParallelTransition(scaleAnim, fadeAnim);

            // --- GESTION POSITION (Nécessite final ou effective final pour la lambda) ---
            final Window finalOwnerWindow = ownerWindow;

            popupStage.setOnShown(event -> {
                if (finalOwnerWindow != null) {
                    Scene parentScene = finalOwnerWindow.getScene();

                    double absoluteX = finalOwnerWindow.getX() + parentScene.getX();
                    double absoluteY = finalOwnerWindow.getY() + parentScene.getY();

                    popupStage.setX(absoluteX);
                    popupStage.setY(absoluteY);

                    popupStage.setWidth(parentScene.getWidth());
                    popupStage.setHeight(parentScene.getHeight());
                } else {
                    // Fallback si pas de owner : on centre sur l'écran
                    popupStage.centerOnScreen();
                }

                content.setOpacity(1);
                openTransition.play();
            });

            // --- AFFICHAGE ---
            if (hasButton) {
                popupStage.showAndWait();
            } else {
                popupStage.show();
            }

            return controller;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static final Interpolator CUSTOM_ELASTIC = new Interpolator() {
        @Override
        protected double curve(double t) {
            return (3 * (1 - t) * (1 - t) * t * -0.6) +
                    (3 * (1 - t) * t * t * 1.6) +
                    (t * t * t);
        }
    };
}
