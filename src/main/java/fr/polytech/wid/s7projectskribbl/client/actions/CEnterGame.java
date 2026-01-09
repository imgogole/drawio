package fr.polytech.wid.s7projectskribbl.client.actions;

import fr.polytech.wid.s7projectskribbl.client.ClientApplication;
import fr.polytech.wid.s7projectskribbl.client.controller.WaitingRoomController;
import javafx.application.Platform;

import java.io.IOException;

public class CEnterGame implements ClientAction
{
    public void Execute(byte[] data)
    {
        Platform.runLater(() ->
        {
            try
            {
                ClientApplication.LoadScene("GameView.fxml");
                WaitingRoomController.Instance().UpdatePlayerList();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        });
    }
}
