package fr.polytech.wid.s7projectskribbl.server.actions;

import fr.polytech.wid.s7projectskribbl.common.payloads.NTDecisionResultPayload;
import fr.polytech.wid.s7projectskribbl.server.PlayerHandler;

public class SNTDecision implements ServerAction
{
    @Override
    public void Execute(PlayerHandler player, byte[] data)
    {
        if ( player.Master().Logic().Drawer().ID() != player.ID() ) return;

        NTDecisionResultPayload payload = new NTDecisionResultPayload();
        payload.Parse(data);

        player.Master().Logic().SetWordChoice(payload.ChosenWordIndex());
    }
}
