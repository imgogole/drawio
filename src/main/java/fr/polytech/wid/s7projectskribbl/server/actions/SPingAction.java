package fr.polytech.wid.s7projectskribbl.server.actions;

import fr.polytech.wid.s7projectskribbl.common.payloads.PingPayload;
import fr.polytech.wid.s7projectskribbl.server.PlayerHandler;

public class SPingAction implements ServerAction
{
    @Override
    public void Execute(PlayerHandler player, byte[] data)
    {
        PingPayload pingPayload = new PingPayload();
        pingPayload.Parse(data);
        System.out.println("Ping " + player.Username() + ": client->server " + pingPayload.FromNowClient() + "ms");
    }
}
