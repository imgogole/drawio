package fr.polytech.wid.s7projectskribbl.client.actions;

import fr.polytech.wid.s7projectskribbl.client.controller.WaitingRoomController;
import fr.polytech.wid.s7projectskribbl.client.network.ClientHandler;
import fr.polytech.wid.s7projectskribbl.client.network.ClientImage;
import fr.polytech.wid.s7projectskribbl.common.payloads.ClientImagesPayload;
import fr.polytech.wid.s7projectskribbl.common.payloads.records.ClientImageItem;

public class CUpdateClientImages implements ClientAction
{
    @Override
    public void Execute(byte[] data)
    {
        ClientImagesPayload payload = new ClientImagesPayload();
        payload.Parse(data);

        for (ClientImageItem item : payload.ClientImages())
        {
            ClientImage clientImage = new ClientImage(
                    item.id(),
                    item.username(),
                    item.image(),
                    item.ready()
            );

            ClientHandler.Singleton().AddIfNotExist(clientImage);
        }

        WaitingRoomController.Instance().UpdatePlayerList();
    }
}