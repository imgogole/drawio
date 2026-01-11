package fr.polytech.wid.s7projectskribbl.server.actions;

import fr.polytech.wid.s7projectskribbl.common.CommandCode;
import fr.polytech.wid.s7projectskribbl.common.GameCommonMetadata;
import fr.polytech.wid.s7projectskribbl.common.payloads.ChatMessagePayload;
import fr.polytech.wid.s7projectskribbl.common.payloads.FoundWordPayload;
import fr.polytech.wid.s7projectskribbl.common.payloads.ServerMessagePayload;
import fr.polytech.wid.s7projectskribbl.server.GameLogic;
import fr.polytech.wid.s7projectskribbl.server.PlayerHandler;

public class SChatMessageReceived implements ServerAction
{
    @Override
    public void Execute(PlayerHandler player, byte[] data)
    {
        ChatMessagePayload chatPayload = new ChatMessagePayload();
        chatPayload.Parse(data);

        String msg = chatPayload.Message();
        GameLogic logic = player.Master().Logic();

        if (logic == null || logic.ChoosenWord() == null)
        {
            BroadcastChatMessage(player, chatPayload, logic, false);
            return;
        }

        if (logic.Drawer().ID() == player.ID())
        {
            //BroadcastChatMessage(player, chatPayload);
            ServerMessagePayload warnMsg = new ServerMessagePayload(
                    "You can't use the chat during your turn!",
                    "#c24b36"
            );
            player.Out().SendCommand(CommandCode.SERVER_MESSAGE, warnMsg);
            return;
        }

        if (logic.HasFoundWord(player.ID()))
        {
            BroadcastChatMessage(player, chatPayload, logic, true);
            return;
        }

        int distance = logic.WordDistance(msg);

        if (distance == 0)
        {
            logic.OnPlayerFoundWord(player);

            for (PlayerHandler p : player.Master().Clients())
            {
                FoundWordPayload successMsg = new FoundWordPayload(
                        player.ID(),
                        player.ID() == p.ID() ? logic.ChoosenWord() : null
                );

                p.Out().SendCommand(CommandCode.FOUND_WORD, successMsg);
            }
        }
        else if (distance <= GameCommonMetadata.Tolerance(logic.ChoosenWord().length()))
        {
            ServerMessagePayload closeMsg = new ServerMessagePayload(
                    "'" + msg + "' is close!",
                    "#f1c40f" // Jaune
            );
            player.Out().SendCommand(CommandCode.SERVER_MESSAGE, closeMsg);
        }
        else
        {
            BroadcastChatMessage(player, chatPayload, logic, false);
        }
    }

    /**
     * Envoie le message de chat à tous les clients connectés.
     */
    private void BroadcastChatMessage(PlayerHandler sender, ChatMessagePayload payload, GameLogic logic, boolean onlyFounders)
    {
        for (PlayerHandler otherPlayer : sender.Master().Clients())
        {
            if (!onlyFounders || logic.HasFoundWord(otherPlayer.ID()))
            {
                otherPlayer.Out().SendCommand(CommandCode.CHAT_MESSAGE_SENT, payload);
            }
        }
    }
}