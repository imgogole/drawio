package fr.polytech.wid.s7projectskribbl.client.actions;

import fr.polytech.wid.s7projectskribbl.common.payloads.DrawPayload;

public class CDrawAction implements ClientAction
{
    @Override
    public void Execute(byte[] data)
    {
        DrawPayload payload = new DrawPayload();
        payload.Parse(data);


    }
}
