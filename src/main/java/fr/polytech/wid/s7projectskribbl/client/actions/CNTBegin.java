package fr.polytech.wid.s7projectskribbl.client.actions;

import fr.polytech.wid.s7projectskribbl.client.controller.GameController;
import fr.polytech.wid.s7projectskribbl.client.service.SoundManager;
import fr.polytech.wid.s7projectskribbl.common.payloads.NTBeginPayload;

public class CNTBegin implements ClientAction
{
    @Override
    public void Execute(byte[] data)
    {
        NTBeginPayload payload = new NTBeginPayload();
        payload.Parse(data);

        SoundManager.getInstance().playSound("NTBegin");

        GameController.Instance().Clear();
        GameController.Instance().SetWordTitle(payload.Word());
        GameController.Instance().BeginRound(payload.Seconds());
    }
}
