package fr.polytech.wid.s7projectskribbl.client;

import fr.polytech.wid.s7projectskribbl.client.network.ClientHandler;
import fr.polytech.wid.s7projectskribbl.client.service.SoundManager;
import javafx.application.Application;

public class Launcher
{
    public static void main(String[] args)
    {
        SoundManager.getInstance();
        Application.launch(ClientApplication.class, args);
    }
}
