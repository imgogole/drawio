package fr.polytech.wid.s7projectskribbl.client.actions;

import fr.polytech.wid.s7projectskribbl.client.controller.GameController;
import fr.polytech.wid.s7projectskribbl.client.network.ClientHandler;
import fr.polytech.wid.s7projectskribbl.common.GameCommonMetadata;
import fr.polytech.wid.s7projectskribbl.common.payloads.NTDecisionPayload;

public class CNTDecision implements ClientAction
{
    @Override
    public void Execute(byte[] data)
    {
        NTDecisionPayload payload = new NTDecisionPayload();
        payload.Parse(data);

        ClientHandler.Singleton().SetDrawer(payload.DrawerID());

        GameController.Instance().UpdatePlayerList();
        GameController.Instance().CloseEndRound(false);
        GameController.Instance().CloseDecisionPanel(false);
        GameController.Instance().SetRound(payload.Round(), GameCommonMetadata.TOTAL_ROUND);
        GameController.Instance().PrepareForNewRound();

        if (ClientHandler.Singleton().ID() == payload.DrawerID())
        {
            GameController.Instance().OpenWordsSelectionPanel(payload.WordOne(), payload.WordTwo(), payload.WordThree(), true);
        }
        else
        {
            GameController.Instance().OpenDecisionPanel(payload.DrawerID(), true);
        }
    }
}
