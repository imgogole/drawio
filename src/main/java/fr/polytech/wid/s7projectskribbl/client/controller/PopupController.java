package fr.polytech.wid.s7projectskribbl.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class PopupController {

    @FXML private Label titleLabel;
    @FXML private Label messageLabel;
    @FXML private Button okButton;

    public void setContent(String title, String message) {
        titleLabel.setText(title);
        messageLabel.setText(message);
    }

    @FXML
    public void initialize() {
        // Ferme la fenêtre quand on clique sur OK
        okButton.setOnAction(event -> {
            Stage stage = (Stage) okButton.getScene().getWindow();
            stage.close();
        });
    }
}