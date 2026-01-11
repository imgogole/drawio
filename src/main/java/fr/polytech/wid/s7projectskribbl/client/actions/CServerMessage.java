package fr.polytech.wid.s7projectskribbl.client.actions;

import fr.polytech.wid.s7projectskribbl.client.controller.GameController;
import fr.polytech.wid.s7projectskribbl.common.payloads.ServerMessagePayload;

import java.nio.ByteBuffer;

public class CServerMessage implements ClientAction
{
    @Override
    public void Execute(byte[] data)
    {
        ServerMessagePayload payload = new ServerMessagePayload();
        payload.Parse(data);

        if (GameController.Instance() != null)
        {
            GameController.Instance().AddServerMessageToChat(payload.Message(), payload.Color());
        }
    }
}
