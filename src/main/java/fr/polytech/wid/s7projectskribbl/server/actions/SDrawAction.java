package fr.polytech.wid.s7projectskribbl.server.actions;

import fr.polytech.wid.s7projectskribbl.common.CommandCode;
import fr.polytech.wid.s7projectskribbl.server.PlayerHandler;

public class SDrawAction implements ServerAction
{
    @Override
    public void Execute(PlayerHandler player, byte[] data)
    {
        for (PlayerHandler p : player.Master().Clients())
        {
            if (p.ID() != player.ID())
            {
                p.Out().SendCommandRawPayload(CommandCode.DRAW_ACTION, data);
            }
        }
    }
}
