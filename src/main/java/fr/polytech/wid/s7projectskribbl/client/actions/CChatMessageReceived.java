package fr.polytech.wid.s7projectskribbl.client.actions;

import fr.polytech.wid.s7projectskribbl.client.controller.GameController;
import fr.polytech.wid.s7projectskribbl.client.network.ClientHandler;
import fr.polytech.wid.s7projectskribbl.client.network.ClientImage;
import fr.polytech.wid.s7projectskribbl.common.payloads.ChatMessagePayload;

public class CChatMessageReceived implements ClientAction
{
    @Override
    public void Execute(byte[] data)
    {
        ChatMessagePayload chatMessagePayload = new ChatMessagePayload();
        chatMessagePayload.Parse(data);

        ClientImage client = ClientHandler.Singleton().GetClientImage(chatMessagePayload.ID());
        if (client != null)
        {
            GameController.Instance().AddMessageToChat(client.Username(), chatMessagePayload.Message());
        }
    }
}
