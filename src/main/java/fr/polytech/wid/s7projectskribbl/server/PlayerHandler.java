package fr.polytech.wid.s7projectskribbl.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class PlayerHandler extends Thread
{
    private final Socket clientSocket;
    private final BufferedReader in;
    private final PrintWriter out;
    private volatile boolean finish;
    private final GameMaster master;

    public PlayerHandler(Socket clientSocket, GameMaster master) throws IOException
    {
        this.clientSocket = clientSocket;
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.out = new PrintWriter(clientSocket.getOutputStream(), false);
        this.master = master;
        this.finish = false;
    }

    public void run()
    {

    }

    /**
     * Termine la connexion de ce client.
     */
    public void TerminateConnection()
    {
        this.TerminateConnection(TerminatedConnectionType.DEFAULT);
    }

    /**
     * Termine la connexion de ce client.
     * @param type Motif de la déconnexion {@link TerminatedConnectionType}
     */
    public void TerminateConnection(TerminatedConnectionType type)
    {
        this.finish = true;
    }
}
