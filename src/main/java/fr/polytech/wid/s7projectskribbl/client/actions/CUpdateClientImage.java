package fr.polytech.wid.s7projectskribbl.client.actions;

import fr.polytech.wid.s7projectskribbl.client.controller.GameController;
import fr.polytech.wid.s7projectskribbl.client.controller.WaitingRoomController;
import fr.polytech.wid.s7projectskribbl.client.network.ClientHandler;
import fr.polytech.wid.s7projectskribbl.client.network.ClientImage;
import fr.polytech.wid.s7projectskribbl.common.payloads.ClientImagePayload;

public class CUpdateClientImage implements ClientAction
{
    @Override
    public void Execute(byte[] data)
    {
        ClientImagePayload payload = new ClientImagePayload();
        payload.Parse(data);

        int state = payload.State();

        if (state == 0)
        {
            ClientImage client = ClientHandler.Singleton().GetClientImage(payload.ID());
            GameController.Instance().AddDisconnectionMessage(client.Username());
            ClientHandler.Singleton().Remove(payload.ID());
        }
        else
        {
            ClientImage clientImage = new ClientImage(
                    payload.ID(),
                    payload.Username(),
                    payload.Image(),
                    state == 2
            );

            ClientHandler.Singleton().AddOrUpdate(clientImage);
        }

        WaitingRoomController.Instance().UpdatePlayerList();
    }
}