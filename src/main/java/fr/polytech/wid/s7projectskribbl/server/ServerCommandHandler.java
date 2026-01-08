package fr.polytech.wid.s7projectskribbl.server;

import fr.polytech.wid.s7projectskribbl.common.CommandCode;
import fr.polytech.wid.s7projectskribbl.server.actions.SPingAction;
import fr.polytech.wid.s7projectskribbl.server.actions.SPlayerInfoRequestAction;
import fr.polytech.wid.s7projectskribbl.server.actions.SReadyAction;
import fr.polytech.wid.s7projectskribbl.server.actions.ServerAction;

import java.util.*;
import java.util.concurrent.*;

public class ServerCommandHandler extends Thread
{
    private final GameMaster master;
    private final Map<Integer, ServerAction> codeToAction;

    private final Map<PlayerHandler, PriorityQueue<ServerCommandRecord>> perPlayerQueues = new ConcurrentHashMap<>();

    private final BlockingQueue<PlayerHandler> playersWithPendingCommands = new LinkedBlockingQueue<>();

    private volatile boolean running = true;

    public ServerCommandHandler(GameMaster master)
    {
        this.master = master;
        this.codeToAction = new HashMap<>();
        this.codeToAction.put(CommandCode.PING.Code(), new SPingAction());
        this.codeToAction.put(CommandCode.READY.Code(), new SReadyAction());
        this.codeToAction.put(CommandCode.REQUEST_PLAYER_INFO.Code(), new SPlayerInfoRequestAction());
    }

    /**
     * Ajoute une commande à la file d'attente des commandes à executer.
     *
     * @param player  Le joueur qui a envoyé la commande
     * @param code    Le code de la commande
     * @param payload Les paramètres de la commande
     */
    public void QueueIncomeCommand(PlayerHandler player, int code, long timestamp, byte[] payload)
    {
        if (!running) return;

        PriorityQueue<ServerCommandRecord> queue = perPlayerQueues.computeIfAbsent(player, k -> new PriorityQueue<>());

        synchronized (queue)
        {
            boolean wasEmpty = queue.isEmpty();
            queue.add(new ServerCommandRecord(player, code, timestamp, payload));

            if (wasEmpty)
            {
                playersWithPendingCommands.offer(player);
            }
        }
    }

    @Override
    public void run()
    {
        try
        {
            while (running)
            {
                PlayerHandler player = playersWithPendingCommands.take();

                PriorityQueue<ServerCommandRecord> queue = perPlayerQueues.get(player);
                if (queue == null) continue;

                ServerCommandRecord record;
                synchronized (queue)
                {
                    record = queue.poll();

                    if (!queue.isEmpty())
                    {
                        playersWithPendingCommands.offer(player);
                    }
                }

                if (record != null)
                {
                    ServerAction action = codeToAction.get(record.code());
                    if (action != null)
                    {
                        action.Execute(record.player(), record.payload());
                        System.out.println("[CMD Received] From: " + record.player().Username() + ", Code: " + record.code() + ", Payload: " + Arrays.toString(record.payload()));
                    }
                }
            }
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

