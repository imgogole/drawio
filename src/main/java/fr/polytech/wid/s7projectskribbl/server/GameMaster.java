package fr.polytech.wid.s7projectskribbl.server;

import fr.polytech.wid.s7projectskribbl.common.TerminatedConnectionType;

import java.io.*;
import java.net.*;
import java.time.Instant;
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
    private final long timestamp;

    private ServerSocket serverSocket;
    private int port;

    private final PromptDebugGameMaster promptDebugGameMaster;

    private final ServerCommandHandler serverCommandHandler;

    private ArrayList<PlayerHandler> clients;

    private WaitForPlayersHandler waitForPlayersHandler;

    private int minimumPlayers;

    public GameMaster(int port, int loggerPort)
    {
        this.timestamp = Instant.now().getEpochSecond();
        this.port = port;
        this.promptDebugGameMaster = new PromptDebugGameMaster(this);
        this.promptDebugGameMaster.start();
        this.serverCommandHandler = new ServerCommandHandler(this);
    }

    /**
     * Le timestamp à laquelle la partie a été créée.
     */
    public long StartedTime()
    {
        return this.timestamp;
    }

    public int Port()
    {
        return port;
    }

    public ArrayList<PlayerHandler> Clients()
    {
        return (waitForPlayersHandler != null) ? waitForPlayersHandler.Results() : this.clients;
    }

    public ServerCommandHandler CommandHandler()
    {
        return serverCommandHandler;
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
            System.out.println("Erreur: " + e.getMessage());
        }

        if (serverSocket != null)
        {
            minimumPlayers = 2;

            this.waitForPlayersHandler = new WaitForPlayersHandler(serverSocket, this);
            this.waitForPlayersHandler.start();
        }

        return ip;
    }

    /**
     * Termine le processus d'attente des joueurs et démarre la partie.
     * @throws InterruptedException
     */
    public void Start() throws InterruptedException
    {
        ArrayList<PlayerHandler> clients = waitForPlayersHandler.Results();
        int playersAmount = clients.size();
        if (playersAmount < minimumPlayers)
        {
            System.out.println(String.format("Erreur: Pas assez de joueurs pour commencer la partie. Minimum : %d, Actuel : %d.", minimumPlayers, playersAmount));
            return;
        }

        this.clients = new ArrayList<PlayerHandler>(clients);
        TerminateWaitForPlayers();

        System.out.println("La partie a débuté.");
    }

    /**
     * Préviens les joueurs connectés de la terminaison de la partie, les déconnecte et termine la partie.
     */
    public void Terminate() throws IOException, InterruptedException
    {
        TerminateAllPlayers(TerminatedConnectionType.SERVER_LOGIC);

        if (serverSocket != null && !serverSocket.isClosed())
        {
            serverSocket.close();
        }

        TerminateWaitForPlayers();

        promptDebugGameMaster.Close();
        System.out.println("La partie a été fermée.");
    }

    /**
     * Termine le processus d'attente des joueurs s'il existe.
     * @throws InterruptedException
     */
    private void TerminateWaitForPlayers() throws InterruptedException
    {
        if (waitForPlayersHandler != null)
        {
            waitForPlayersHandler.Finish();
            waitForPlayersHandler.join();
        }

        waitForPlayersHandler = null;
    }

    /**
     * Termine la connexion avec tous les clients.
     */
    private void TerminateAllPlayers(TerminatedConnectionType type) throws InterruptedException
    {
        ArrayList<PlayerHandler> clientsToDisconnect = this.Clients();

        if (clientsToDisconnect != null)
        {
            for (PlayerHandler player : clientsToDisconnect)
            {
                player.TerminateConnection(type);
            }
            System.out.println("La connexion avec tous les joueurs est terminée.");
        }
    }

    /**
     * Arrête tous the threads en cours d'exécution.
     */
    private void CleanUp()
    {
        try
        {
            TerminateWaitForPlayers();
        }
        catch (InterruptedException e)
        {
            System.out.println("Erreur lors du CleanUp: " + e.getMessage());
        }
    }
}
