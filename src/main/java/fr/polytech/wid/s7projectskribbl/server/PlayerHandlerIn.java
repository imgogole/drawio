package fr.polytech.wid.s7projectskribbl.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Classe pour recevoir des commandes et des informations au client.
 */
public class PlayerHandlerIn extends Thread
{
    private final BufferedReader in;
    private final PlayerHandler handler;
    private final Socket clientSocket;

    private String username;

    public PlayerHandlerIn(Socket clientSocket, PlayerHandler handler) throws IOException
    {
        this.clientSocket = clientSocket;
        this.handler = handler;
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public BufferedReader In()
    {
        return this.in;
    }

    public void run()
    {
        try
        {
            String message;
            while (in != null && (message = in.readLine()) != null)
            {
                handler.Master().Logger().LogLn("Message de username: " + message);
            }
        }
        catch (IOException e)
        {
            handler.Master().Logger().LogLn("Erreur: " + e.getMessage());
        }
    }

    public void Close()
    {
        try
        {
            if (this.in != null)
            {
                this.in.close();
            }
        }
        catch (IOException e)
        {
            this.handler.Master().Logger().LogLn("Erreur: " + e.getMessage());
        }
    }
}
