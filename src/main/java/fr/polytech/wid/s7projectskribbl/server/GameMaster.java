package fr.polytech.wid.s7projectskribbl.server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * Instance d'une partie de jeu côté serveur.
 * Il va attendre la connexion des joueurs, puis réaliser la gestion de jeu pour chaque joueur.
 * <p>
 * Chaque fois qu'une partie veut être créé, il suffit de lancer un programme main qui instancie cette classe, puis d'appeler {@code GameMaster.Begin()}.
 * </p>
 *
 * @author Dalil NAAMNA
 */
public class GameMaster
{
    private ServerSocket serverSocket;
    private int port;

    private ArrayList<PlayerHandler> clients;

    private WaitForPlayersHandler waitForPlayersHandler;
    private Thread waitForPlayersThread;

    private int minimumPlayers;

    public GameMaster(int port)
    {
        this.port = port;
    }

    /**
     * Initialise le GameMaster.
     * <p>Le GameMaster va attendre les joueurs, quand il y en aura suffisamment pour démarrer la partie, la méthode {@code GameMaster.Start()} peut être appelé sans souci.</p>
     *
     * @return L'adresse IP de ce serveur.
     */
    public String Begin()
    {
        String ip = null;
        try
        {
            serverSocket = new ServerSocket(this.port);
            ip = InetAddress.getLocalHost().getHostAddress() + ":" + this.port;
        }
        catch (IOException e)
        {
            System.err.println("Erreur: " + e.getMessage());
        }

        minimumPlayers = 2;

        waitForPlayersHandler = new WaitForPlayersHandler(serverSocket, this);
        waitForPlayersThread = new Thread(waitForPlayersHandler);
        waitForPlayersThread.start();

        return ip;
    }

    /**
     * Termine le processus d'attente des joueurs et démarre la partie.
     * @throws InterruptedException
     */
    public void Start() throws InterruptedException
    {
        ArrayList<PlayerHandler> clients = waitForPlayersHandler.Results();
        if (clients.size() < minimumPlayers)
        {
            System.out.printf("Erreur: Pas assez de joueurs pour commencer la partie. Minimum : %d%n", minimumPlayers);
            return;
        }

        this.clients = new ArrayList<>(clients);
        waitForPlayersHandler.Finish();
        waitForPlayersThread.join();

        waitForPlayersHandler = null;
        waitForPlayersThread = null;

        System.out.println("La partie a débuté.");
    }

    /**
     * Préviens les joueurs connectés de la terminaison de la partie, les déconnecte et termine la partie.
     */
    public void Terminate() throws IOException
    {
        for (PlayerHandler player : this.clients)
        {
            player.TerminateConnection(TerminatedConnectionType.SERVER_LOGIC);
        }
        serverSocket.close();
    }
}
