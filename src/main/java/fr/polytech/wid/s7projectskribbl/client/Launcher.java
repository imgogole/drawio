package fr.polytech.wid.s7projectskribbl.client;

import fr.polytech.wid.s7projectskribbl.client.network.ClientHandler;
import javafx.application.Application;

public class Launcher
{
    public static void main(String[] args)
    {
        ClientHandler.Singleton().Connect("10.212.202.56", 5555);
        Application.launch(ClientApplication.class, args);
    }
}
