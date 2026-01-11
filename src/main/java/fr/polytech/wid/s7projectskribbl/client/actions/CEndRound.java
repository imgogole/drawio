package fr.polytech.wid.s7projectskribbl.client.actions;

import fr.polytech.wid.s7projectskribbl.client.controller.GameController;
import fr.polytech.wid.s7projectskribbl.client.network.ClientHandler;
import fr.polytech.wid.s7projectskribbl.client.network.ClientImage;
import fr.polytech.wid.s7projectskribbl.client.service.SoundManager;
import fr.polytech.wid.s7projectskribbl.common.payloads.EndRoundPayload;

import java.util.ArrayList;
import java.util.List;

public class CEndRound implements ClientAction
{
    @Override
    public void Execute(byte[] data)
    {
        EndRoundPayload payload = new EndRoundPayload();
        payload.Parse(data);
        if (GameController.Instance() == null) return;
        List<GameController.PlayerRoundScore> scoresForUI = new ArrayList<>();

        boolean atLeastOneFound = false;

        for (EndRoundPayload.PlayerScoreInfo info : payload.PlayerScores())
        {
            String username = "Unknown";
            ClientImage client = ClientHandler.Singleton().GetClientImage(info.Id());
            if (client != null)
            {
                username = client.Username();
                client.AddPoints(info.GainedPoints());
                if (info.GainedPoints() > 0)
                {
                    atLeastOneFound = true;
                }
            }
            scoresForUI.add(new GameController.PlayerRoundScore(username, info.GainedPoints()));
        }
        GameController.Instance().ShowEndRound(payload.WordToGuess(), scoresForUI, true);
        GameController.Instance().UpdatePlayerList();

        if (atLeastOneFound)
        {
            SoundManager.getInstance().playSound("EndRoundAF.mp3");
        }
        else
        {
            SoundManager.getInstance().playSound("EndRoundNOF.mp3");
        }
    }
}