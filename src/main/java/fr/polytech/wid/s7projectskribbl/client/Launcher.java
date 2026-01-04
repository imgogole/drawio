package fr.polytech.wid.s7projectskribbl.client;

import fr.polytech.wid.s7projectskribbl.client.network.ClientHandler;
import javafx.application.Application;

public class Launcher
{
    public static void main(String[] args)
    {
        ClientHandler client = ClientHandler.Singleton();
        client.Connect("5.tcp.eu.ngrok.io", 11259);
        //Application.launch(ClientApplication.class, args);
    }
}
