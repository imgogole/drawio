package fr.polytech.wid.s7projectskribbl.client.actions;

import fr.polytech.wid.s7projectskribbl.client.ClientApplication;
import fr.polytech.wid.s7projectskribbl.client.controller.WaitingRoomController;

import java.io.IOException;

public class CEnterWaitingRoom implements ClientAction
{
    @Override
    public void Execute(byte[] data)
    {
        try
        {
            WaitingRoomController.Instance().UpdatePlayerList();
            ClientApplication.LoadScene("JoinRoomView.fxml");
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
