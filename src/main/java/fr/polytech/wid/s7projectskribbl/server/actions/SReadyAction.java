package fr.polytech.wid.s7projectskribbl.server.actions;


import fr.polytech.wid.s7projectskribbl.common.payloads.ReadyPayload;
import fr.polytech.wid.s7projectskribbl.server.PlayerHandler;

/**
 * Action à réaliser quand le client envoie son état dans la salle d'attente (prêt ou pas prêt).
 */
public class SReadyAction implements ServerAction
{
    @Override
    public void Execute(PlayerHandler player, byte[] data)
    {
        ReadyPayload readyPayload = new ReadyPayload();
        readyPayload.Parse(data);

        player.SetReady(readyPayload.Ready());
        player.Master().UpdateClientImage(player);
    }
}
