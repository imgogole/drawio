package fr.polytech.wid.s7projectskribbl.client;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.util.Set;
import javafx.scene.Node;
import javafx.util.Duration;

public class ClientController {

    @FXML
    private TextField messageField;

    @FXML
    private Label statusLabel;


    @FXML
    private VBox mainContainer;

    @FXML
    private void initialize(){
        Platform.runLater(() -> {

            applyAnimations();
        });
    }



    @FXML
    private void handleSendAction() {
        String message = messageField.getText();

        if (message.isEmpty()) {
            statusLabel.setText("Statut : Veuillez entrer un message.");
            return;
        }

        System.out.println("Message à envoyer : " + message);
        statusLabel.setText("Statut : Envoi de " + message + "...");
        messageField.clear();
    }


    private void applyAnimations() {

        //on récupère tous les noeuds qui ont la classe "bouton-primaire"
        Set<Node> buttons = mainContainer.lookupAll(".bouton-primaire");

        Color baseColor = Color.web("#f0c50d");
        Color hoverColor = Color.web("#e59700");

        for (Node node : buttons){
            if (node instanceof Button){
                Button btn = (Button) node;

                btn.hoverProperty().addListener((obs, oldState, newState) -> {

                    //cas hovered
                    if (newState){
                        animateColor(btn, baseColor, hoverColor);
                    }
                    //cas non hovered
                    else{
                        animateColor(btn, hoverColor, baseColor);
                    }

                });

            }
        }
    }


    private void animateColor(Button btn, Color oldColor, Color newColor){
        final Transition transition = new Transition() {
            {
                setCycleDuration(Duration.millis(200));
                setInterpolator(Interpolator.EASE_OUT);
            }

            @Override
            protected void interpolate(double v) {
                Color currentCol = oldColor.interpolate(newColor, v);
                btn.setBackground(new Background(new BackgroundFill(
                        currentCol,
                        new CornerRadii(4),
                        new Insets(0)
                )));
            }
        };

        transition.play();

    }




    private void DoSomethingFromJavaThread()
    {
        Platform.runLater(() ->
            {
                statusLabel.setText("Mise à jour du status.");
            }
        );
    }
}

