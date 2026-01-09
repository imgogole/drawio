package fr.polytech.wid.s7projectskribbl.server.actions;

import fr.polytech.wid.s7projectskribbl.common.CommandCode;
import fr.polytech.wid.s7projectskribbl.common.payloads.ChatMessagePayload;
import fr.polytech.wid.s7projectskribbl.server.PlayerHandler;

public class SChatMessageReceived implements ServerAction
{
    @Override
    public void Execute(PlayerHandler player, byte[] data)
    {
        ChatMessagePayload chatMessagePayload = new ChatMessagePayload();
        chatMessagePayload.Parse(data);

        String msg = chatMessagePayload.Message();
        // TODO déterminer une tentative de deviner le mot à trouver

        // En déduire ce qu'on doit faire du message
        // 0 : tentative échoué, considéré comme message normal
        // 1 : tentative échoué mais presque : considéré comme message normal mais envoyer un message pour dire que c'est presque ça
        // 2 : tentative réussi : prévenir le client qu'il a réussi, ne pas envoyer le message
        // 3 : le client avait déjà réussi, envoyer le message uniquement aux clients qui ont réussi
        int attemptResultCode = 0;

        for (PlayerHandler otherPlayer : player.Master().Clients())
        {
            otherPlayer.Out().SendCommand(CommandCode.CHAT_MESSAGE_SENT, chatMessagePayload);
        }
    }
}
