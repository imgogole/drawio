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
                    master.Logger().LogLn("Terminaison de WaitForPlayersHandler.");
                }
                else
                {
                    handler = new PlayerHandler(client, master);
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
                    master.Logger().LogLn("Une connexion a été refusée: " + e.getMessage());
                }
            }
            if (handler != null)
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

        // Forcer une connexion si .accept() dans le run() bloque.
        try (Socket wakeup = new Socket("localhost", master.Port())) {}
        catch (IOException e) {}
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
