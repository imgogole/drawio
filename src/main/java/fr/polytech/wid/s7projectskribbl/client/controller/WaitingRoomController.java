package fr.polytech.wid.s7projectskribbl.client.controller;

import fr.polytech.wid.s7projectskribbl.client.network.ClientHandler;
import fr.polytech.wid.s7projectskribbl.client.network.ClientImage;
import fr.polytech.wid.s7projectskribbl.common.CommandCode;
import fr.polytech.wid.s7projectskribbl.common.payloads.ReadyPayload;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.awt.*;
import java.util.List;

public class WaitingRoomController {

    public VBox mainContainer;
    @FXML
    private FlowPane playerListContainer;

    @FXML
    private Label readyPlayers;

    @FXML
    private Label playerNumber;

    @FXML
    private Button btnReadyState;

    @FXML
    private Button btnQuit;

    @FXML
    private ImageView logoImage;

    private boolean isReady = false;

    private static WaitingRoomController instance;

    public static WaitingRoomController Instance()
    {
        return instance;
    }

    @FXML
    public void initialize()
    {
        instance = this;
        btnReadyState.setOnAction(event -> toggleReadyState());

        // Exemple : Bouton quitter
        btnQuit.setOnAction(event -> {
            System.out.println("Quitting lobby...");
            Platform.exit();
            System.exit(0);
        });

        Internal_UpdatePlayerList();

        var imgUrl = getClass().getResource("/images/drawio_glow.png");

        if (imgUrl != null) {
            logoImage.setImage(new Image(imgUrl.toExternalForm()));
        }
    }
    /**
     Méthode principale pour mettre à jour l'affichage des joueurs
     à appeler à chaque fois que le serveur envoie une mise à jour du lobby.
     **/
    public void UpdatePlayerList()
    {
        Platform.runLater(this::Internal_UpdatePlayerList);
    }

    private void Internal_UpdatePlayerList()
    {
        List<ClientImage> clients = ClientHandler.Singleton().ClientImages();
        playerListContainer.getChildren().clear();

        int readyCount = (int)clients.stream().filter(ClientImage::IsReady).count();
        readyPlayers.setText(String.valueOf(readyCount));
        playerNumber.setText(String.valueOf(clients.size()));

        for (ClientImage player : clients)
        {
            VBox playerBox = createPlayerBox(player);
            playerListContainer.getChildren().add(playerBox);
        }
    }

    private VBox createPlayerBox(ClientImage player) {
        HBox userCard = new HBox();
        userCard.setAlignment(Pos.CENTER_LEFT);
        userCard.setSpacing(20); // Espacement augmenté
        userCard.setPadding(new Insets(15));
        userCard.setPrefWidth(300); // Légèrement plus large pour le confort
        userCard.getStyleClass().add("userBox");

        // --- GAUCHE : Avatar (Rayon augmenté à 35) ---
        Circle avatarCircle = new Circle(35);
        avatarCircle.setStroke(Color.web("#707070"));
        avatarCircle.setStrokeWidth(2);

        if (player.Avatar() != null) {
            Image fxImage = SwingFXUtils.toFXImage(player.Avatar(), null);
            avatarCircle.setFill(new ImagePattern(fxImage));
        } else {
            avatarCircle.setFill(Color.GRAY);
        }

        // --- DROITE : Textes centrés ---
        VBox textContainer = new VBox();
        textContainer.setAlignment(Pos.CENTER);
        HBox.setHgrow(textContainer, Priority.ALWAYS);
        textContainer.setSpacing(5);

        // Nom du joueur (Taille augmentée)
        Label nameLabel = new Label(player.Username());
        nameLabel.getStyleClass().add("playerNameLabel");

        // État (Ready / Not Ready)
        Label statusLabel = new Label();
        if (player.IsReady()) {
            statusLabel.setText("READY");
            statusLabel.getStyleClass().add("status-ready-impact");
        } else {
            statusLabel.setText("Not ready...");
            statusLabel.getStyleClass().add("status-waiting-italic");
        }

        textContainer.getChildren().addAll(nameLabel, statusLabel);
        userCard.getChildren().addAll(avatarCircle, textContainer);

        return new VBox(userCard);
    }

    private void toggleReadyState()
    {
        isReady = !isReady;
        if (isReady) {
            btnReadyState.setText("CANCEL");
            btnReadyState.getStyleClass().add("cancelButton");
        } else {
            btnReadyState.setText("READY");
            btnReadyState.getStyleClass().remove("cancelButton");
        }
        ClientHandler.Singleton().Out().SendCommand(CommandCode.READY, new ReadyPayload(isReady));
    }

}