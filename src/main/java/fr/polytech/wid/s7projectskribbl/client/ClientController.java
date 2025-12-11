package fr.polytech.wid.s7projectskribbl.client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class ClientController {

    @FXML
    private TextField messageField;

    @FXML
    private Label statusLabel;

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
}