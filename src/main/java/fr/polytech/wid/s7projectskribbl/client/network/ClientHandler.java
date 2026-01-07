package fr.polytech.wid.s7projectskribbl.client.network;

import fr.polytech.wid.s7projectskribbl.client.actions.*;
import fr.polytech.wid.s7projectskribbl.common.*;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Classe de communication client/serveur.
 */
public class ClientHandler extends Thread
{
    private Socket clientSocket;
    private ClientHandlerIn inHandler;
    private ClientHandlerOut outHandler;
    private static ClientHandler instance;

    private final Map<Integer, ClientImage> clientImageMap;

    private final Map<Integer, ClientAction> codeToAction;
    private final BlockingQueue<ClientCommandRecord> incomeCommandQueue;

    private volatile boolean connected = false;
    private volatile boolean running;

    public static ClientHandler Singleton()
    {
        if (instance == null)
        {
            instance = new ClientHandler();
            instance.start();
        }
        return instance;
    }

    public ClientHandler()
    {
        this.incomeCommandQueue = new PriorityBlockingQueue<>();
        this.codeToAction = new HashMap<>();
        this.clientImageMap = new HashMap<>();
        this.running = true;

        this.codeToAction.put(CommandCode.PING.Code(), new CPingAction());
        this.codeToAction.put(CommandCode.SERVER_MESSAGE.Code(), new CServerMessage());
        this.codeToAction.put(CommandCode.REQUEST_PLAYER_INFO.Code(), new CPlayerInfoAction());
        this.codeToAction.put(CommandCode.UPDATE_CLIENT_IMAGES.Code(), new CUpdateClientImages());
        this.codeToAction.put(CommandCode.ENTER_WAITING_ROOM.Code(), new CEnterWaitingRoom());
    }

    public List<ClientImage> ClientImages()
    {
        return new ArrayList<>(clientImageMap.values());
    }

    public ClientHandlerIn In()
    {
        return inHandler;
    }

    public ClientHandlerOut Out()
    {
        return outHandler;
    }

    public void run()
    {
        try
        {
            while (running)
            {
                if (connected)
                {
                    ClientCommandRecord record = incomeCommandQueue.poll();
                    if (record != null)
                    {
                        ClientAction action = codeToAction.get(record.code());
                        if (action != null)
                        {
                            action.Execute(record.payload());
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {

        }
    }

    /**
     * Ajoute l'image client s'il n'existe pas.
     */
    public void AddIfNotExist(ClientImage image)
    {
        if (!clientImageMap.containsKey(image.ID()))
        {
            clientImageMap.put(image.ID(), image);
        }
    }

    /**
     * Connecte ce client au serveur de jeu.
     *
     * @param ip   L'IP de la partie
     * @param port Le port de la partie
     */
    public void Connect(String ip, int port) throws IOException
    {
        // On s'assure que le client n'est pas déjà dans une partie
        this.Disconnect();

        this.incomeCommandQueue.clear();

        Socket socket = new Socket(ip, port);
        this.connected = true;
        this.clientSocket = socket;

        inHandler = new ClientHandlerIn(this, this.clientSocket);
        inHandler.start();

        outHandler = new ClientHandlerOut(this, this.clientSocket);

        System.out.println("Connecté au serveur [" + ip + ":" + port + "]");
    }

    /**
     * Déconnecte le client de la partie.
     */
    public void Disconnect() throws IOException
    {
        if (clientSocket != null)
        {
            clientSocket.close();
            inHandler.Close();

            clientSocket = null;
        }
        this.connected = false;
    }

    /**
     * Déconnecte le client de la partie et termine ce ClientHandler.
     */
    public void DisconnectAndStop() throws IOException
    {
        this.Disconnect();
        this.running = false;
    }

    /**
     * Ajoute une commande à la file d'attente des commandes à executer.
     *
     * @param code    Le code de la commande
     * @param payload Les paramètres de la commande
     */
    public void QueueIncomeCommand(int code, long timestamp, byte[] payload)
    {
        if (codeToAction.containsKey(code))
        {
            incomeCommandQueue.add(new ClientCommandRecord(code, timestamp, payload));
        }
    }
}
