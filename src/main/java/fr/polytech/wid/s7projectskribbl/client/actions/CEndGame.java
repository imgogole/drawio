package fr.polytech.wid.s7projectskribbl.client.actions;

import fr.polytech.wid.s7projectskribbl.client.ClientApplication;
import fr.polytech.wid.s7projectskribbl.client.controller.WaitingRoomController;
import fr.polytech.wid.s7projectskribbl.client.network.ClientHandler;
import fr.polytech.wid.s7projectskribbl.client.service.PopupService;
import javafx.application.Platform;

import java.io.IOException;

public class CEndGame implements ClientAction
{
    public void Execute(byte[] data)
    {
        Platform.runLater(() ->
        {
            try
            {
                ClientHandler.Singleton().Disconnect();
                ClientApplication.LoadScene("JoinRoomView.fxml");
                Platform.runLater(() -> {
                    PopupService.showPopup(
                            "Déconnexion",
                            "La partie est terminée",
                            true
                    );
                });
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });
    }
}