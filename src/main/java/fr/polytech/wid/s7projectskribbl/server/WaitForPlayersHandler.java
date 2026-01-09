package fr.polytech.wid.s7projectskribbl.server;

import fr.polytech.wid.s7projectskribbl.common.CommandCode;
import fr.polytech.wid.s7projectskribbl.common.payloads.IdAttributionPayload;

import java.io.IOException;
import java.util.*;
import java.net.*;

public class WaitForPlayersHandler extends Thread
{
    private final ServerSocket serverSocket;
    private final List<PlayerHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private volatile boolean finish = false;
    private final GameMaster master;
    private Socket terminatedSocket;

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
                    if (terminatedSocket != null)
                    {
                        terminatedSocket.close();
                    }
                    System.out.println("Terminaison de WaitForPlayersHandler.");
                }
                else
                {
                    handler = new PlayerHandler(client, master);
                    handler.Out().SendCommand(CommandCode.ID_ATTRIBUTION, new IdAttributionPayload(handler.ID()));
                    handler.Out().SendCommand(CommandCode.REQUEST_PLAYER_INFO, null);
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
        try
        {
            terminatedSocket = new Socket("localhost", 5555);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Retire un joueur de la liste d'attente de manière thread-safe.
     * Utile si un joueur se déconnecte avant que la partie ne commence.
     */
    public void RemovePlayer(PlayerHandler player)
    {
        // La suppression est maintenant sûre.
        // Si elle renvoie true, le joueur est bien parti.
        boolean removed = this.clients.remove(player);
        if (removed)
        {
            System.out.println("[WaitForPlayersHandler] Joueur retiré de la liste : " + player.IP());
        }
    }

    /**
     * Retourne la liste des clients connectés actuels.
     * @return La liste des PlayerHandler
     */
    public ArrayList<PlayerHandler> Results()
    {
        synchronized (clients) {
            return new ArrayList<>(clients);
        }
    }
}
