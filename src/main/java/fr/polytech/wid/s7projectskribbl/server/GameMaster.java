package fr.polytech.wid.s7projectskribbl.server;

import fr.polytech.wid.s7projectskribbl.common.CommandCode;
import fr.polytech.wid.s7projectskribbl.common.TerminatedConnectionType;
import fr.polytech.wid.s7projectskribbl.common.payloads.ClientImagePayload;

import java.io.*;
import java.net.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
        this.serverCommandHandler.start();
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
    public WaitForPlayersHandler WForPlayersHandler() { return waitForPlayersHandler; }

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

        boolean allReady = clients.stream().allMatch(PlayerHandler::IsReady);

        if (!allReady)
        {
            System.out.println("Erreur: Pas tous le monde est prêt.");
            return;
        }

        this.clients = new ArrayList<PlayerHandler>(clients);
        TerminateWaitForPlayers();

        System.out.println("La partie a débuté.");

        for (PlayerHandler player : this.clients)
        {
            player.Out().SendCommand(CommandCode.ENTER_GAME, null);
        }
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

    /**
     * Envoie une commande de mise à jour des clients pour tous les clients.
     */
    public void UpdateClientImages()
    {
        List<ClientImagePayload> payload = new ArrayList<ClientImagePayload>();
        for (PlayerHandler player : Clients())
        {
            payload.add(new ClientImagePayload(player.ID(), player.Username(), player.IsReady()?2:1, player.Avatar()));
        }

        for (PlayerHandler player : Clients())
        {
            for (ClientImagePayload p : payload)
            {
                player.Out().SendCommand(CommandCode.UPDATE_CLIENT_IMAGE, p);
            }
        }
    }

    /**
     * Envoie une commande de mise à jour d'un client pour tous les clients.
     */
    public void UpdateClientImage(PlayerHandler player)
    {
        ClientImagePayload playerPayload = new ClientImagePayload(player.ID(), player.Username(), player.IsReady()?2:1, player.Avatar());

        for (PlayerHandler p : Clients())
        {
            p.Out().SendCommand(CommandCode.UPDATE_CLIENT_IMAGE, playerPayload);
        }
    }

    /**
     * Préviens tous les autres joueurs de la déconnexion d'un client.
     */
    public void WarnDisconnectedClient(int id)
    {
        ClientImagePayload playerPayload = new ClientImagePayload(id, null, 0, null);

        for (PlayerHandler p : Clients())
        {
            p.Out().SendCommand(CommandCode.UPDATE_CLIENT_IMAGE, playerPayload);
        }
    }

    public synchronized void OnPlayerDisconnect(PlayerHandler player, TerminatedConnectionType type)
    {
        boolean removed = false;

        if (clients != null && clients.contains(player))
        {
            removed = clients.remove(player);
        }
        else if (waitForPlayersHandler != null)
        {
            waitForPlayersHandler.RemovePlayer(player);
            removed = true;
        }

        System.out.println("Déconnexion détectée : " + player.Username() + " (ID: " + player.ID() + ")" + ", Reason: " + type);

        WarnDisconnectedClient(player.ID());
        player.TerminateConnection(type);
    }
}
