package fr.polytech.wid.s7projectskribbl.client.actions;

import fr.polytech.wid.s7projectskribbl.client.controller.GameController;

public class CShowEndGameResult implements ClientAction
{
    @Override
    public void Execute(byte[] data)
    {
        GameController.Instance().ShowGameResults();
    }
}
