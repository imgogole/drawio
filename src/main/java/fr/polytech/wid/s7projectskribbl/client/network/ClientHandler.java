package fr.polytech.wid.s7projectskribbl.client.network;

import fr.polytech.wid.s7projectskribbl.client.actions.*;
import fr.polytech.wid.s7projectskribbl.client.controller.JoinRoomController;
import fr.polytech.wid.s7projectskribbl.common.*;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
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
    private ClientHeartBeat heartBeat;
    private int id;
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
        this.codeToAction.put(CommandCode.ID_ATTRIBUTION.Code(), new CIdAttribution());
        this.codeToAction.put(CommandCode.SERVER_MESSAGE.Code(), new CServerMessage());
        this.codeToAction.put(CommandCode.REQUEST_PLAYER_INFO.Code(), new CPlayerInfoAction());
        this.codeToAction.put(CommandCode.UPDATE_CLIENT_IMAGE.Code(), new CUpdateClientImage());
        this.codeToAction.put(CommandCode.ENTER_WAITING_ROOM.Code(), new CEnterWaitingRoom());
        this.codeToAction.put(CommandCode.CHAT_MESSAGE_SENT.Code(), new CChatMessageReceived());
        this.codeToAction.put(CommandCode.ENTER_GAME.Code(), new CEnterGame());
        this.codeToAction.put(CommandCode.DRAW_ACTION.Code(), new CDrawAction());
        this.codeToAction.put(CommandCode.NT_DECISION.Code(), new CNTDecision());
        this.codeToAction.put(CommandCode.NT_BEGIN.Code(), new CNTBegin());
        this.codeToAction.put(CommandCode.END_ROUND_RESUME.Code(), new CEndRound());
    }

    public void SetDrawer(int id)
    {
        for (ClientImage client : clientImageMap.values())
        {
            client.SetDrawer(client.ID() == id);
        }
    }

    public List<ClientImage> ClientImages()
    {
        return new ArrayList<>(clientImageMap.values());
    }

    public ClientImage GetClientImage(int id)
    {
        return clientImageMap.get(id);
    }

    public int ID() { return this.id; }

    public void SetID(int id)
    {
        this.id = id;
    }

    public ClientHandlerIn In()
    {
        return inHandler;
    }

    public ClientHandlerOut Out()
    {
        return outHandler;
    }

    public ClientImage GetDrawer()
    {
        return clientImageMap.values().stream().filter(ClientImage::IsDrawer).findFirst().orElse(null);
    }

    @Override
    public void run()
    {
        while (running)
        {
            try
            {
                if (connected)
                {
                    ClientCommandRecord record = incomeCommandQueue.take();
                    ClientAction action = codeToAction.get(record.code());
                    if (action != null)
                    {
                        try
                        {
                            action.Execute(record.payload());
                        }
                        catch (Exception e)
                        {
                            System.err.println("Erreur lors de l'exécution du code: " + record.code());
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        System.err.println("Code non pris en charge: " + record.code());
                    }
                }
                else
                {
                    Thread.sleep(100);
                }
            }
            catch (InterruptedException e)
            {
                running = false;
            }
            catch (Exception e)
            {
                System.err.println("Erreur critique dans ClientHandler Loop:");
                e.printStackTrace();
            }
        }
    }

    /**
     * Ajoute ou modifie l'image client s'il n'existe pas.
     */
    public void AddOrUpdate(ClientImage image)
    {
        clientImageMap.put(image.ID(), image);
    }

    /**
     * Retire l'image client s'il existe.
     */
    public void Remove(int id)
    {
        clientImageMap.remove(id);
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

        heartBeat = new ClientHeartBeat(this, GameCommonMetadata.HeartbeatClientSeconds);
        heartBeat.start();

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
            heartBeat.Kill();

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
        JoinRoomController.Singleton().JoinDisconnectionMsg();
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
