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
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import java.util.List;

public class WaitingRoomController {

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

    /**
     Génère une interface de box pour chaque joueur
     **/
    private VBox createPlayerBox(ClientImage player) {
        // --- Conteneur Principal  ---
        VBox box = new VBox();
        box.getStyleClass().add("userBox");
        box.setAlignment(Pos.CENTER);

        // Ajout du padding
        box.setPadding(new Insets(8, 8, 8, 8));

        // --- Label Nom du Joueur ---
        Label nameLabel = new Label(player.Username());
        nameLabel.getStyleClass().add("orangeText");

        // --- Label Statut  ---
        String statusText = player.IsReady() ? "READY" : "WAITING...";
        Label statusLabel = new Label(statusText);


        statusLabel.getStyleClass().add("grayText");

        // on applique la statut associé à la bonne situation du joueur
        if (player.IsReady())
        {
            statusLabel.getStyleClass().add("status-ready");
        }
        else
        {
            statusLabel.getStyleClass().add("status-waiting");
        }

        // Assemblage
        box.getChildren().addAll(nameLabel, statusLabel);

        return box;
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