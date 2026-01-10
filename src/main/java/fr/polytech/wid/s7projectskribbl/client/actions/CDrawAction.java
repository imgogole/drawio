package fr.polytech.wid.s7projectskribbl.client.actions;

import fr.polytech.wid.s7projectskribbl.client.controller.GameController;
import fr.polytech.wid.s7projectskribbl.common.payloads.DrawPayload;

public class CDrawAction implements ClientAction
{
    @Override
    public void Execute(byte[] data)
    {
        DrawPayload payload = new DrawPayload();
        payload.Parse(data);

        if (GameController.Instance() != null)
        {
            GameController.Instance().ApplyDrawAction(payload);
        }
    }
}
