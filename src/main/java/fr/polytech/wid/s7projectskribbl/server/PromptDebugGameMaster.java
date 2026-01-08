package fr.polytech.wid.s7projectskribbl.server;

import fr.polytech.wid.s7projectskribbl.common.CommandCode;
import fr.polytech.wid.s7projectskribbl.common.TerminatedConnectionType;
import fr.polytech.wid.s7projectskribbl.common.payloads.PingPayload;
import fr.polytech.wid.s7projectskribbl.common.payloads.ServerMessagePayload;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Classe permettant de réaliser des actions à partir de commandes écrites dans l'entrée standard
 */
public class PromptDebugGameMaster extends Thread
{
    private final GameMaster gameMaster;
    private final Scanner scanner;
    private static final List<String> EXIT_COMMANDS = List.of("stop", "exit", "quit");

    public PromptDebugGameMaster(GameMaster gameMaster)
    {
        this.gameMaster = gameMaster;
        this.scanner = new Scanner(System.in);
    }

    public void run()
    {
        try
        {
            System.out.print("[IN] Cmd> ");
            while (!Thread.currentThread().isInterrupted() && scanner != null && scanner.hasNextLine())
            {
                String line = scanner.nextLine();
                if (line != null)
                {
                    ParseAndExecuteCommand(line.trim());
                }
                System.out.print("[IN] Cmd> ");
            }
        }
        catch (IllegalStateException e)
        {
            System.out.println("Le terminal s'est fermé.");
        }
    }

    public void ParseAndExecuteCommand(String command)
    {
        if (command == null || command.trim().isEmpty())
        {
            return;
        }

        String[] parts = command.trim().split("\\s+");

        ArrayList<String> args = new ArrayList<>(Arrays.asList(parts));

        if (!args.isEmpty())
        {
            String action = args.getFirst().toLowerCase();
            args.removeFirst();
            Execute(action, args);
        }
    }

    private void Execute(String action, ArrayList<String> args)
    {
        System.out.println("Commande executée : " + action + " " + String.join(" ", args));
        ArrayList<PlayerHandler> players = gameMaster.Clients();
        if (action.equals("kick"))
        {
            if (args.isEmpty())
            {
                System.err.println("Command kick: kick <ip>");
                return;
            }
            String ip = args.getFirst();
            ArrayList<PlayerHandler> clients = gameMaster.Clients();
            boolean kickedSomeone = false;
            for (PlayerHandler player : clients)
            {
                if (player.IP().equals(ip))
                {
                    player.TerminateConnection(TerminatedConnectionType.KICKED);
                    System.out.println("Client with IP [" + ip + "] has been kicked.");
                    kickedSomeone = true;
                }
            }
            if (!kickedSomeone)
            {
                System.out.println("No client found with IP [" + ip + "].");
            }
        }
        else if (action.equals("start"))
        {
            try
            {
                gameMaster.Start();
            }
            catch (InterruptedException e)
            {
                System.out.println(e.getMessage());
            }
        }
        else if (EXIT_COMMANDS.contains(action))
        {
            try
            {
                gameMaster.Terminate();
            }
            catch (IOException | InterruptedException e)
            {
                System.out.println(e.getMessage());
            }
        }
        else if (action.equals("ping"))
        {
            for (PlayerHandler player : players)
            {
                PingPayload payload = new PingPayload();
                payload.SetTimestamp();
                player.Out().SendCommand(CommandCode.PING, payload);

                System.out.println("Envoi d'un ping à " + player.Username());
            }
        }
        else if (action.equals("msg"))
        {
            for (PlayerHandler player : players)
            {
                ServerMessagePayload payload = new ServerMessagePayload(String.join(" ", args));
                player.Out().SendCommand(CommandCode.SERVER_MESSAGE, payload);

                System.out.println("Envoi d'un message à " + player.Username());
            }
        }
        else if (action.equals("players"))
        {
            System.out.println("--- Joueurs (" + players.size() + ") ---");

            if (!players.isEmpty())
            {
                for (PlayerHandler player : players)
                {
                    System.out.println(player.ID() + ": " + player.Username());
                }
            }
        }
    }

    public void Close()
    {
        scanner.close();
    }
}
