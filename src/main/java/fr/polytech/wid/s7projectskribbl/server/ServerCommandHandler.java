package fr.polytech.wid.s7projectskribbl.server;

import fr.polytech.wid.s7projectskribbl.common.GameCommonMetadata;
import fr.polytech.wid.s7projectskribbl.server.actions.SPingAction;
import fr.polytech.wid.s7projectskribbl.server.actions.ServerAction;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Map;
import java.util.Queue;

public class ServerCommandHandler extends Thread
{
    private final GameMaster master;
    private final Map<Integer, ServerAction> codeToAction;
    private final Queue<ServerCommandRecord> incomeCommandQueue;

    private volatile boolean running = true;

    public ServerCommandHandler(GameMaster master)
    {
        this.master = master;
        this.incomeCommandQueue = new ConcurrentLinkedQueue<>();
        this.codeToAction = new HashMap<>();

        this.codeToAction.put(GameCommonMetadata.PING_CODE, new SPingAction());
    }

    /**
     * Ajoute une commande à la file d'attente des commandes à executer.
     *
     * @param player  Le joueur qui a envoyé la commande
     * @param code    Le code de la commande
     * @param payload Les paramètres de la commande
     */
    public void QueueIncomeCommand(PlayerHandler player, int code, byte[] payload)
    {
        if (!running)
        {
            return;
        }

        if (codeToAction.containsKey(code))
        {
            incomeCommandQueue.add(new ServerCommandRecord(player, code, payload));
        }
    }

    public void run()
    {
        try
        {
            while (running)
            {
                ServerCommandRecord record = incomeCommandQueue.poll();
                if (record != null)
                {
                    ServerAction action = codeToAction.get(record.code());
                    if (action != null)
                    {
                        action.Execute(record.player(), record.payload());
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("ServerCommandHandler.run() exception: " + e.getMessage());
        }
    }
}