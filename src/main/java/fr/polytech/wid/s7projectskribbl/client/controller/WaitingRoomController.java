package fr.polytech.wid.s7projectskribbl.client.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import java.util.ArrayList;
import java.util.List;

// Importe ici ton modèle Player ou User
// import fr.polytech.wid.s7projectskribbl.model.Player;

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

    @FXML
    public void initialize() {
        // Initialisation du bouton Ready
        btnReadyState.setOnAction(event -> toggleReadyState());

        // Exemple : Bouton quitter
        btnQuit.setOnAction(event -> {
            System.out.println("Quitting lobby...");
            Platform.exit();
            System.exit(0);
        });

        // SIMULATION : Ajoutons 8 joueurs pour tester le retour à la ligne
        List<PlayerStub> dummyPlayers = new ArrayList<>();
        dummyPlayers.add(new PlayerStub("Dada", true));
        dummyPlayers.add(new PlayerStub("Bruce Wayne", false));
        dummyPlayers.add(new PlayerStub("Joker", true));
        dummyPlayers.add(new PlayerStub("Penguin", false));
        dummyPlayers.add(new PlayerStub("Riddler", true));
        dummyPlayers.add(new PlayerStub("Catwoman", false));
        dummyPlayers.add(new PlayerStub("Robin", true));
        dummyPlayers.add(new PlayerStub("Alfred", true));


        updatePlayerList(dummyPlayers);

    }

    /**
      Méthode principale pour mettre à jour l'affichage des joueurs
      à appeler à chaque fois que le serveur envoie une mise à jour du lobby.
     **/
    public void updatePlayerList(List<PlayerStub> players) {
        // On vide la liste actuelle
        playerListContainer.getChildren().clear();

        // On met à jour les compteurs en haut
        int readyCount = (int) players.stream().filter(PlayerStub::isReady).count();
        readyPlayers.setText(String.valueOf(readyCount));
        playerNumber.setText(String.valueOf(players.size()));

        // On recrée les boîtes pour chaque joueur
        for (PlayerStub player : players) {
            VBox playerBox = createPlayerBox(player);
            playerListContainer.getChildren().add(playerBox);
        }
    }

    /**
     Génère une interface de box pour chaque joueur
     **/
    private VBox createPlayerBox(PlayerStub player) {
        // --- Conteneur Principal  ---
        VBox box = new VBox();
        box.getStyleClass().add("userBox");
        box.setAlignment(Pos.CENTER);

        // Ajout du padding
        box.setPadding(new Insets(8, 8, 8, 8));

        // --- Label Nom du Joueur ---
        Label nameLabel = new Label(player.getName());
        nameLabel.getStyleClass().add("orangeText");

        // --- Label Statut  ---
        String statusText = player.isReady() ? "READY" : "WAITING...";
        Label statusLabel = new Label(statusText);


        statusLabel.getStyleClass().add("grayText");

        // on applique la statut associé à la bonne situation du joueur
        if (player.isReady()) {
            statusLabel.getStyleClass().add("status-ready");
        } else {
            statusLabel.getStyleClass().add("status-waiting");
        }

        // Assemblage
        box.getChildren().addAll(nameLabel, statusLabel);

        return box;
    }

    private void toggleReadyState() {
        isReady = !isReady;
        if (isReady) {
            btnReadyState.setText("CANCEL");
            btnReadyState.getStyleClass().add("cancelButton");
        } else {
            btnReadyState.setText("READY");
            btnReadyState.getStyleClass().remove("cancelButton");
        }
        // TODO: Envoyer l'état "Ready" au serveur ici
    }

    // Classe interne temporaire pour l'exemple (à remplacer par ton vrai modèle Player)
    public static class PlayerStub {
        private String name;
        private boolean ready;

        public PlayerStub(String name, boolean ready) {
            this.name = name;
            this.ready = ready;
        }
        public String getName() { return name; }
        public boolean isReady() { return ready; }
    }
}