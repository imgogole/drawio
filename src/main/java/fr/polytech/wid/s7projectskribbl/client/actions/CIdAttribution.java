package fr.polytech.wid.s7projectskribbl.client.actions;

import fr.polytech.wid.s7projectskribbl.client.network.ClientHandler;
import fr.polytech.wid.s7projectskribbl.common.payloads.IdAttributionPayload;

public class CIdAttribution implements ClientAction
{
    @Override
    public void Execute(byte[] data)
    {
        IdAttributionPayload payload = new IdAttributionPayload();
        payload.Parse(data);

        ClientHandler.Singleton().SetID(payload.ID());
    }
}
