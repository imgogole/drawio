package fr.polytech.wid.s7projectskribbl.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Classe pour envoyer des commandes et des informations au client.
 */
public class PlayerHandlerOut
{
    private final PrintWriter out;
    private final PlayerHandler handler;
    private final Socket clientSocket;

    public PlayerHandlerOut(Socket clientSocket, PlayerHandler handler) throws IOException
    {
        this.clientSocket = clientSocket;
        this.handler = handler;
        this.out = new PrintWriter(clientSocket.getOutputStream(), false);
    }

    public PrintWriter Out()
    {
        return this.out;
    }

    public void Close()
    {

    }
}
