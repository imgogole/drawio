package fr.polytech.wid.s7projectskribbl.client.controller;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import java.util.Set;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class GameController {

    @FXML
    private TextField messageField;

    @FXML
    private Label statusLabel;

    @FXML
    private VBox mainContainer;

    @FXML
    private Button sendChat;

    @FXML
    private Circle circleTailleIcone;

    @FXML
    private MenuButton menuTaille;

    @FXML
    private Slider sliderTaille;

    @FXML
    private Canvas drawingCanvas;

    private GraphicsContext gc; // L'outil de dessin

    @FXML
    private void initialize(){

        // Initialisation du gc
        // On récupère l'outil de dessin du canvas
        gc = drawingCanvas.getGraphicsContext2D();

        // Configuration initiale (couleur noire, taille par défaut du slider)
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(sliderTaille.getValue());


        Platform.runLater(() -> {

            applyAnimations();

            sliderTaille.valueProperty().addListener((obs, oldVal, newVal) -> {
                double taille = newVal.doubleValue();
                gc.setLineWidth(taille);

                //rétrécissement de la taille du cercle
                double radius = Math.min(15, Math.max(2, taille / 3));
                circleTailleIcone.setRadius(radius);
            });
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

        //radius d'un bouton standard
        CornerRadii stdRadius = new CornerRadii(4);
        //radius du bouton chat
        CornerRadii chatRadius = new CornerRadii(0, 13, 13,0, false);

        for (Node node : buttons){
            if (node instanceof Button){
                Button btn = (Button) node;

                btn.hoverProperty().addListener((obs, oldState, newState) -> {

                    //cas hovered
                    if (newState){
                        animateColor(btn, baseColor, hoverColor, stdRadius);
                    }
                    //cas non hovered
                    else{
                        animateColor(btn, hoverColor, baseColor ,stdRadius);
                    }

                });

            }
        }

        sendChat.hoverProperty().addListener((obs, oldState, newState) -> {
            //cas hovered
            if (newState){
                animateColor(sendChat, baseColor, hoverColor, chatRadius);
            }
            //cas non hovered
            else{
                animateColor(sendChat, hoverColor, baseColor, chatRadius);
            }


        });
    }


    private void animateColor(Button btn, Color oldColor, Color newColor, CornerRadii radii){
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
                        radii,
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

