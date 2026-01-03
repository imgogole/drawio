package fr.polytech.wid.s7projectskribbl.client;

import fr.polytech.wid.s7projectskribbl.client.network.ClientHandler;
import javafx.application.Application;

public class Launcher
{
    public static void main(String[] args)
    {
        ClientHandler client = ClientHandler.Singleton();
        client.Connect("10.193.32.14", 5555);
        //Application.launch(ClientApplication.class, args);
    }
}
