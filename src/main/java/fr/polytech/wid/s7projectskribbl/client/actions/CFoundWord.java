package fr.polytech.wid.s7projectskribbl.client.actions;

import fr.polytech.wid.s7projectskribbl.client.controller.GameController;
import fr.polytech.wid.s7projectskribbl.client.network.ClientHandler;
import fr.polytech.wid.s7projectskribbl.client.network.ClientImage;
import fr.polytech.wid.s7projectskribbl.client.service.SoundManager;
import fr.polytech.wid.s7projectskribbl.common.payloads.FoundWordPayload;

public class CFoundWord implements ClientAction
{
    @Override
    public void Execute(byte[] data)
    {
        FoundWordPayload payload = new FoundWordPayload();
        payload.Parse(data);

        ClientImage client = ClientHandler.Singleton().GetClientImage(payload.FinderId());
        client.SetFound(true);

        GameController.Instance().UpdatePlayerList();
        if (GameController.Instance() != null)
        {
            GameController.Instance().AddServerMessageToChat(client.Username() + " guessed the word!", "#72f542");
            SoundManager.getInstance().playSound("FoundWord.mp3");
            if (ClientHandler.Singleton().ID() == payload.FinderId())
            {
                GameController.Instance().SetWordTitle(payload.Word());
            }
        }
    }
}
