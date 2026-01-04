package fr.polytech.wid.s7projectskribbl.client.actions;

import fr.polytech.wid.s7projectskribbl.common.payloads.ServerMessagePayload;

import java.nio.ByteBuffer;

public class CServerMessage implements ClientAction
{
    @Override
    public void Execute(byte[] data)
    {
        ServerMessagePayload payload = new ServerMessagePayload();
        payload.Parse(data);

        System.out.println("Message reçu du serveur: \"" + payload.Message() + "\"");
    }
}
