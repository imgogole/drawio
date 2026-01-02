package fr.polytech.wid.s7projectskribbl.server;

import fr.polytech.wid.s7projectskribbl.common.CommandAction;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Map;
import java.util.Queue;

public class ServerCommandHandler extends Thread
{
    private final GameMaster master;
    private final Map<Integer, CommandAction> codeToCommand;
    private final Queue<ServerCommandRecord> incomeCommandQueue;

    private volatile boolean running = true;

    public ServerCommandHandler(GameMaster master)
    {
        this.master = master;
        this.incomeCommandQueue = new ConcurrentLinkedQueue<>();
        this.codeToCommand = new HashMap<>();
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

        if (codeToCommand.containsKey(code))
        {
            incomeCommandQueue.add(new ServerCommandRecord(player, code, payload));
        }
    }

    public void run()
    {

    }
}