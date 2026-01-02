package fr.polytech.wid.s7projectskribbl.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class JoinRoomController {

    @FXML
    private Circle avatarCircle;

    @FXML
    private Label avatarLabel; 

    @FXML
    private TextField chooseNameImput;

    @FXML
    private TextField roomIPinput;

    @FXML
    private Button joinButton;


    // Variable pour stocker le fichier image choisi par l'utilisateur
    private File selectedImageFile;

    @FXML
    public void initialize() {
        // Installation du Tooltip
        setupTooltip();

        // Configuration du clic pour choisir l'image
        setupAvatarSelection();

        // Action du bouton Rejoindre
        joinButton.setOnAction(event -> handleJoin());
    }

    private void setupTooltip() {
        Tooltip tooltip = new Tooltip("Click to pick a profile picture");
        tooltip.setShowDelay(Duration.millis(200));

        Tooltip.install(avatarCircle, tooltip);
    }

    private void setupAvatarSelection() {
        // Gestion du clic sur le cercle ou sur le texte "Profile Picture"
        avatarCircle.setOnMouseClicked(event -> openFileChooser());

        if (avatarLabel != null) {
            avatarLabel.setOnMouseClicked(event -> openFileChooser());
        }
    }

    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pick a profile picture");

        // On n'autorise que les images
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        // On récupère la fenêtre actuelle pour centrer la boîte de dialogue
        Window stage = avatarCircle.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        // Si l'utilisateur n'a pas annulé
        if (file != null) {
            updateAvatarDisplay(file);
        }
    }

    private void updateAvatarDisplay(File file) {
        try {
            String imagePath = file.toURI().toString();
            Image image = new Image(imagePath);

            // Cela empêche le CSS de repeindre le cercle en gris quand la souris bouge
            avatarCircle.getStyleClass().remove("profile-circle-clickable");

            // Remplissage du cercle avec l'image (format rond)
            avatarCircle.setFill(new ImagePattern(image));

            // Changement de style visuel quand c'est bon
            avatarCircle.setStyle("-fx-stroke: #f0c50d; -fx-stroke-width: 3px; -fx-stroke-dash-array: null;");

            // On cache le texte qui est par dessus
            if (avatarLabel != null) {
                avatarLabel.setVisible(false);
            }

            // On sauvegarde le fichier pour l'envoyer au serveur plus tard
            this.selectedImageFile = file;

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur chargement image : " + file.getAbsolutePath());
        }
    }

    private void handleJoin() {
        String username = chooseNameImput.getText();
        String IP = roomIPinput.getText();

        // On récupère la fenêtre actuelle pour pouvoir centrer la popup dessus
        Window currentWindow = joinButton.getScene().getWindow();

        boolean hasError = false;
        StringBuilder errorMsg = new StringBuilder();

        // --- Validation du Pseudo ---
        if (username == null || username.trim().isEmpty()) {
            // Style rouge
            chooseNameImput.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 13px; -fx-background-insets: 0;");
            hasError = true;
            errorMsg.append("You must enter a username !\n");
        } else {
            // Reset du style si c'est bon
            chooseNameImput.setStyle(null);
        }

        // --- Validation de l'IP ---
        if (IP == null || IP.trim().isEmpty()) {
            // Style rouge
            roomIPinput.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 13px; -fx-background-insets: 0;");
            hasError = true;
            errorMsg.append("You must enter a Room IP !");
        } else {
            // Reset du style si c'est bon
            roomIPinput.setStyle(null);
        }

        // --- Si erreur, on affiche la POPUP et on arrête ---
        if (hasError) {

            fr.polytech.wid.s7projectskribbl.client.service.PopupService.showError(
                    "Missing data!",
                    errorMsg.toString(),
                    currentWindow
            );
            return;
        }

        // ---  TOUT EST BON : Connexion ---
        System.out.println("--- JOIN REQUEST ---");
        System.out.println("Pseudo : " + username);
        System.out.println("IP : " + IP);

        if (selectedImageFile != null) {
            System.out.println("Avatar : " + selectedImageFile.getName());
            // TODO: Logique d'envoi de l'image
        } else {
            System.out.println("Avatar : Default");
        }

        // TODO: Appeler ici ta méthode de connexion réseau
        // ex: client.connect(IP, username, selectedImageFile);
    }

}