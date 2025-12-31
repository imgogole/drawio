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

    public PlayerHandler(Socket clientSocket, GameMaster master) throws IOException
    {
        this.clientSocket = clientSocket;
        this.master = master;
        this.inHandler = new PlayerHandlerIn(clientSocket, this);
        this.outHandler = new PlayerHandlerOut(clientSocket, this);
        this.inHandler.start();
    }

    public String IP()
    {
        return clientSocket.getInetAddress().getHostAddress();
    }

    public GameMaster Master()
    {
        return master;
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
        // this.inHandler.SendCommand(new DisconnectCommand(type));
        try
        {
            if (inHandler != null)
            {
                inHandler.Close();
                inHandler.join();
            }
            if (outHandler != null)
            {
                outHandler.Close();
            }
        }
        catch (InterruptedException e)
        {
            System.err.println(e);
        }
        try
        {
            if (clientSocket != null && !clientSocket.isClosed())
            {
                clientSocket.close();
            }
        }
        catch (IOException e)
        {
            System.err.println(e);
        }
    }
}
