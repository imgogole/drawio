package fr.polytech.wid.s7projectskribbl.server;

import java.io.IOException;
import java.util.*;
import java.net.*;

public class WaitForPlayersHandler extends Thread
{
    private final ServerSocket serverSocket;
    private final ArrayList<PlayerHandler> clients = new ArrayList<PlayerHandler>();
    private volatile boolean finish = false;
    private final GameMaster master;

    public WaitForPlayersHandler(ServerSocket serverSocket, GameMaster master)
    {
        this.serverSocket = serverSocket;
        this.master = master;
    }

    public void run()
    {
        while (!finish)
        {
            PlayerHandler handler = null;
            try
            {
                Socket client = serverSocket.accept();

                if (finish)
                {
                    System.out.println("Terminaison de WaitForPlayersHandler.");
                }
                else
                {
                    handler = new PlayerHandler(client, master);
                    System.out.println("Un joueur s'est connecté.");
                }
            }
            catch (IOException e)
            {
                if (serverSocket.isClosed())
                {
                    finish = true;
                    break;
                }
                else
                {
                    // TODO: évaluer la bonne condition pour savoir si un client a voulu se connecter.
                    System.out.println("Une connexion a été refusée: " + e.getMessage());
                }
            }
            if (handler != null && !finish)
            {
                clients.add(handler);
            }
        }
    }

    /**
     * Termine le processus d'attente des joueurs.
     */
    public void Finish()
    {
        finish = true;
    }

    /**
     * Retourne la liste des clients connectés actuels.
     * @return La liste des PlayerHandler
     */
    public ArrayList<PlayerHandler> Results()
    {
        return clients;
    }
}
