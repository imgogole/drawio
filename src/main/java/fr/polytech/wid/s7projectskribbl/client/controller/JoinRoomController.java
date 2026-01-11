package fr.polytech.wid.s7projectskribbl.client.controller;

import fr.polytech.wid.s7projectskribbl.client.ClientApplication;
import fr.polytech.wid.s7projectskribbl.client.network.ClientHandler;
import fr.polytech.wid.s7projectskribbl.common.GameCommonMetadata;
import fr.polytech.wid.s7projectskribbl.client.service.PopupService;
import javafx.application.Platform;
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

    private static JoinRoomController instance;

    public static JoinRoomController Singleton()
    {
        return instance;
    }

    // Variable pour stocker le fichier image choisi par l'utilisateur
    private File selectedImageFile;

    public File GetAvatarFile()
    {
        return selectedImageFile;
    }

    public String GetUsername()
    {
        return chooseNameImput.getText();
    }

    @FXML
    public void initialize()
    {
        instance = this;

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
        avatarCircle.setOnMouseClicked(event -> openFileChooser());

        if (avatarLabel != null)
        {
            avatarLabel.setOnMouseClicked(event -> openFileChooser());
        }
    }

    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pick a profile picture");

        // On n'autorise que les images
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );

        // --- AJOUT : Définir le dossier par défaut ---
        try {
            String userHome = System.getProperty("user.home");
            File defaultDir = new File("C:\\Users\\imgog\\Desktop\\Info\\Projet S7");

            // 2. IMPORTANT : On vérifie qu'il existe avant de l'assigner
            // (Sinon JavaFX lance une erreur et le FileChooser ne s'ouvre pas)
            if (defaultDir.exists() && defaultDir.isDirectory()) {
                fileChooser.setInitialDirectory(defaultDir);
            } else {
                // Fallback : Si "Pictures" n'existe pas, on ouvre le dossier Home
                fileChooser.setInitialDirectory(new File(userHome));
            }
        } catch (Exception e) {
            // En cas de souci de permission, on ne fait rien, le FileChooser s'ouvrira par défaut
            System.out.println("Impossible de définir le répertoire initial : " + e.getMessage());
        }
        // ---------------------------------------------

        // On récupère la fenêtre actuelle pour centrer la boîte de dialogue
        Window stage = avatarCircle.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        // Si l'utilisateur n'a pas annulé
        if (file != null) {
            updateAvatarDisplay(file);
        }
    }

    private void updateAvatarDisplay(File file)
    {
        try
        {
            String imagePath = file.toURI().toString();
            Image image = new Image(imagePath);

            avatarCircle.getStyleClass().remove("profile-circle-clickable");

            avatarCircle.setFill(new ImagePattern(image));

            avatarCircle.setStyle("-fx-stroke: #f0c50d; -fx-stroke-width: 3px; -fx-stroke-dash-array: null;");

            if (avatarLabel != null) {
                avatarLabel.setVisible(false);
            }

            this.selectedImageFile = file;

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("Erreur chargement image : " + file.getAbsolutePath());
        }
    }

    public void JoinDisconnectionMsg()
    {
        Platform.runLater(() ->
        {
            Window currentWindow = joinButton.getScene().getWindow();
            PopupService.showPopup(
                    "Déconnexion",
                    "Veuillez patienter...",
                    currentWindow,
                    true
            );
        });
    }

    private void handleJoin()
    {
        String username = chooseNameImput.getText();
        String IP = roomIPinput.getText();

        Window currentWindow = joinButton.getScene().getWindow();

        boolean hasError = false;
        StringBuilder errorMsg = new StringBuilder();

        if (username == null || username.trim().isEmpty())
        {
            chooseNameImput.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2px; -fx-border-radius: 13px; -fx-background-insets: 0;");
            hasError = true;
            errorMsg.append("You must enter a username !\n");
        }
        else
        {
            chooseNameImput.setStyle(null);
        }

        roomIPinput.setStyle(null);

        if (IP == null || IP.trim().isEmpty())
        {
            IP = "localhost";
        }

        if (hasError)
        {
            PopupService.showPopup(
                    "Missing data!",
                    errorMsg.toString(),
                    currentWindow,
                    true
            );
            return;
        }

        PopupController loadingPopup = null;

        try
        {

            loadingPopup = PopupService.showPopup(
                    "Connecting to game...",
                    "Please Wait...",
                    currentWindow,
                    false
            );

            String ipToUse;
            int port = GameCommonMetadata.GamePort;
            String[] ipInputSliced = IP.split(":");

            ipToUse = ipInputSliced[0];

            if (ipInputSliced.length == 2)
            {
                port = Integer.parseInt(ipInputSliced[1]);
            }

            System.out.println("Connecting to " + ipToUse + ":" + port);

            ClientHandler client = ClientHandler.Singleton();
            client.Connect(ipToUse, port);

            if(loadingPopup != null)
            {
                loadingPopup.closeWithAnimation();
            }

            ClientApplication.LoadScene("WaitingRoomView.fxml");
        }
        catch (Exception e)
        {
            System.err.println("Erreur connecting to [" + IP.trim() + "]: " + e.getMessage());

            if(loadingPopup != null)
            {
                loadingPopup.closeWithAnimation();
            }

            PopupService.showPopup(
                    "Unable to connect to server",
                    e.getMessage(),
                    currentWindow,
                    true
            );
        }
    }

}