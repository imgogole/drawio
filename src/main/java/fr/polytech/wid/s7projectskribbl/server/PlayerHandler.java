package fr.polytech.wid.s7projectskribbl.server;

import fr.polytech.wid.s7projectskribbl.common.TerminatedConnectionType;

import java.io.IOException;
import java.net.Socket;

/**
 * Classe de la gestion d'un joueur
 */
public class PlayerHandler
{
    private final Socket clientSocket;
    private final GameMaster master;

    private final PlayerHandlerIn inHandler;
    private final PlayerHandlerOut outHandler;

    private boolean ready;
    private String username;

    public PlayerHandler(Socket clientSocket, GameMaster master) throws IOException
    {
        this.clientSocket = clientSocket;
        this.master = master;
        this.inHandler = new PlayerHandlerIn(clientSocket, this);
        this.outHandler = new PlayerHandlerOut(clientSocket, this);
        this.inHandler.start();
        this.username = null;
        this.ready = false;
    }

    public String IP()
    {
        return clientSocket.getInetAddress().getHostAddress();
    }

    public GameMaster Master()
    {
        return master;
    }

    public PlayerHandlerIn In()
    {
        return inHandler;
    }

    public PlayerHandlerOut Out()
    {
        return outHandler;
    }

    public String Username()
    {
        return username == null ? "[Unnamed Player: " + clientSocket.getInetAddress().getHostAddress() + "]" : username;
    }

    /**
     * Indique si le joueur est prêt à jouer.
     * @param ready
     */
    public void SetReady(boolean ready)
    {
        this.ready = ready;
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
     *
     * @param type Motif de la déconnexion {@link TerminatedConnectionType}
     */
    public void TerminateConnection(TerminatedConnectionType type)
    {
        // TODO : Envoyer une commande "DisconnectCommand" avec le type.
        // this.outHandler.SendCommand(new DisconnectCommand(type));
        try
        {
            if (clientSocket != null && !clientSocket.isClosed())
            {
                clientSocket.close();
            }
            if (inHandler != null)
            {
                inHandler.join();
            }
        }
        catch (IOException | InterruptedException e)
        {
            System.err.println(e);
        }
    }
}
