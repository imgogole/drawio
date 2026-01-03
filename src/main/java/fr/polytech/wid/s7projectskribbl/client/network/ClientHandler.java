package fr.polytech.wid.s7projectskribbl.client.network;

import fr.polytech.wid.s7projectskribbl.client.actions.CPingAction;
import fr.polytech.wid.s7projectskribbl.client.actions.ClientAction;
import fr.polytech.wid.s7projectskribbl.common.*;
import fr.polytech.wid.s7projectskribbl.server.actions.ServerAction;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Arrays;

/**
 * Classe de communication client/serveur.
 */
public class ClientHandler extends Thread
{
    private Socket clientSocket;
    private ClientHandlerIn inHandler;
    private ClientHandlerOut outHandler;
    private static ClientHandler instance;

    private final Map<Integer, ClientAction> codeToAction;
    private final Queue<ClientCommandRecord> incomeCommandQueue;

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
        this.incomeCommandQueue = new ConcurrentLinkedQueue<>();
        this.codeToAction = new HashMap<>();
        this.running = true;

        this.codeToAction.put(CommandCode.PING.Code(), new CPingAction());
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
     * Connecte ce client au serveur de jeu.
     * @param ip L'IP de la partie
     * @param port Le port de la partie
     */
    public void Connect(String ip, int port)
    {
        // On s'assure que le client n'est pas déjà dans une partie
        this.Disconnect();

        try
        {
            this.incomeCommandQueue.clear();

            Socket socket = new Socket(ip, port);
            this.connected = true;
            System.out.println("Connecté au serveur [" + ip + ":" + port + "]");
            clientSocket = socket;

            inHandler = new ClientHandlerIn(this, this.clientSocket);
            inHandler.start();

            outHandler = new ClientHandlerOut(this, this.clientSocket);
        }
        catch (IOException e)
        {
            System.out.println("Erreur de connection à [" + ip + ":" + port + "]: " + e.getMessage());
        }
    }

    /**
     * Déconnecte le client de la partie.
     */
    public void Disconnect()
    {
        if (clientSocket != null)
        {
            try
            {
                clientSocket.close();
                inHandler.Close();
            }
            catch (IOException e)
            {

            }

            clientSocket = null;
        }
        this.connected = false;
    }

    /**
     * Déconnecte le client de la partie et termine ce ClientHandler.
     */
    public void DisconnectAndStop()
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
    public void QueueIncomeCommand(int code, byte[] payload)
    {
        if (codeToAction.containsKey(code))
        {
            incomeCommandQueue.add(new ClientCommandRecord(code, payload));
        }
    }
}
