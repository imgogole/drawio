package fr.polytech.wid.s7projectskribbl.client.controller;

import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PopupController {

    @FXML private Label titleLabel;
    @FXML private Label messageLabel;
    @FXML private Button okButton;

    private Stage stage;


    private static final Interpolator CUSTOM_ELASTIC = new Interpolator() {
        @Override
        protected double curve(double t) {
            return (3 * Math.pow(1 - t, 2) * t * -0.6) +
                    (3 * (1 - t) * Math.pow(t, 2) * 1.6) +
                    Math.pow(t, 3);
        }
    };

    public void setContent(String title, String message) {
        titleLabel.setText(title);
        messageLabel.setText(message);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void initialize() {
        okButton.setOnAction(event -> closeWithAnimation());
    }

    private void closeWithAnimation() {
        // On récupère le root node (la VBox de la popup)
        // okButton.getParent() renvoie la VBox si le bouton est directement dedans
        Node root = okButton.getParent();

        // Animation de fermeture
        ScaleTransition closeTransition = new ScaleTransition(Duration.seconds(0.2), root);
        closeTransition.setFromX(1);
        closeTransition.setFromY(1);
        closeTransition.setToX(0);
        closeTransition.setToY(0);

        closeTransition.setInterpolator(Interpolator.EASE_OUT);

        // On ferme le stage seulement quand l'animation est finie
        closeTransition.setOnFinished(e -> {
            if (stage != null) {
                stage.close();
            } else {
                // Fallback si setStage n'a pas été appelé
                ((Stage) okButton.getScene().getWindow()).close();
            }
        });

        closeTransition.play();
    }
}