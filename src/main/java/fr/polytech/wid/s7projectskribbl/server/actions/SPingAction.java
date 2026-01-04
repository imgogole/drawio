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

        long rtt = pingPayload.GetElapsed();
        System.out.println("Ping " + player.Username() + ": " + rtt + "ms (RTT)");
    }
}
