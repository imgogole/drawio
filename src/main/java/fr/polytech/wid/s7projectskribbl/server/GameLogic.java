package fr.polytech.wid.s7projectskribbl.server;

import fr.polytech.wid.s7projectskribbl.common.CommandCode;
import fr.polytech.wid.s7projectskribbl.common.GameCommonMetadata;
import fr.polytech.wid.s7projectskribbl.common.payloads.EndRoundPayload;
import fr.polytech.wid.s7projectskribbl.common.payloads.NTBeginPayload;
import fr.polytech.wid.s7projectskribbl.common.payloads.NTDecisionPayload;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static fr.polytech.wid.s7projectskribbl.common.GameCommonMetadata.*;

public class GameLogic extends Thread
{
    private final GameMaster master;
    private PlayerHandler drawer;
    private String chosenWord;
    private List<String> wordsToChoose = new ArrayList<>();

    private Queue<String> wordQueue = new LinkedList<>();

    private int round;
    private volatile boolean isWaitingForDrawerChoice;
    private volatile boolean canDraw;

    private volatile boolean roundRunning;
    private final Set<Integer> playersWhoFoundWord = new HashSet<>();
    private final Map<Integer, Long> playerFindTimes = new HashMap<>();
    private long roundStartTime;
    boolean cancelRound = false;

    public GameLogic(GameMaster master)
    {
        this.master = master;
        this.round = 1;
        this.canDraw = false;
        this.isWaitingForDrawerChoice = false;

        LoadAndShuffleWords();
    }

    public PlayerHandler Drawer()
    {
        return drawer;
    }

    public String ChoosenWord()
    {
        return chosenWord;
    }

    public boolean HasFoundWord(int playerID)
    {
        return playersWhoFoundWord.contains(playerID);
    }

    private void LoadAndShuffleWords()
    {
        List<String> tempWordList = new ArrayList<>();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("words.txt"))
        {
            if (is == null)
            {
                System.err.println("ERREUR CRITIQUE : words.txt introuvable dans les resources !");
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    if (!line.trim().isEmpty())
                    {
                        tempWordList.add(line.trim());
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Collections.shuffle(tempWordList);
        wordQueue.addAll(tempWordList);
        System.out.println("Dictionnaire chargé : " + wordQueue.size() + " mots.");
    }

    public GameMaster Master()
    {
        return master;
    }

    public boolean CanDraw(PlayerHandler player)
    {
        return canDraw && drawer != null && player.ID() == this.drawer.ID();
    }

    public synchronized void OnPlayerFoundWord(PlayerHandler player)
    {
        if (!roundRunning || drawer == null) return;

        if (player.ID() == drawer.ID()) return;

        if (!playersWhoFoundWord.contains(player.ID()))
        {
            playersWhoFoundWord.add(player.ID());
            playerFindTimes.put(player.ID(), System.currentTimeMillis());

            System.out.println(player.Username() + " a trouvé le mot !");

            int totalGuessers = Math.max(0, master.Clients().size() - 1);

            if (playersWhoFoundWord.size() >= totalGuessers)
            {
                System.out.println("Tout le monde a trouvé ! Fin du round anticipée.");
                this.roundRunning = false;
            }
        }
    }

    public synchronized void OnPlayerDisconnected(PlayerHandler player)
    {
        if (!roundRunning) return;

        if (drawer != null && player.ID() == drawer.ID())
        {
            this.roundRunning = false;
            this.cancelRound = true;
        }
        else
        {
            int currentClientCount = master.Clients().size();
            if (master.Clients().contains(player))
            {
                currentClientCount--;
            }

            int totalGuessers = Math.max(0, currentClientCount - 1);

            playersWhoFoundWord.remove(player.ID());
            playerFindTimes.remove(player.ID());

            if (playersWhoFoundWord.size() >= totalGuessers && totalGuessers > 0)
            {
                this.roundRunning = false;
            }
        }
    }

    @Override
    public void run()
    {
        try
        {
            Thread.sleep(100);
        }
        catch (InterruptedException e)
        {
        }

        while (round <= TOTAL_ROUND)
        {
            System.out.println("=== DÉBUT DU ROUND " + round + " ===");

            List<PlayerHandler> playersForThisRound = master.Clients();
            int playerIndex = 0;

            while (playerIndex < playersForThisRound.size())
            {
                drawer = playersForThisRound.get(playerIndex);

                if (drawer == null || !master.Clients().contains(drawer))
                {
                    playerIndex++;
                    continue;
                }

                System.out.println("It's " + drawer.Username() + " turn.");

                wordsToChoose.clear();
                for (int i = 0; i < 3; i++)
                {
                    if (wordQueue.isEmpty()) break;
                    String w = wordQueue.poll();
                    wordsToChoose.add(w);
                    wordQueue.add(w);
                }

                isWaitingForDrawerChoice = true;

                for (PlayerHandler p : master.Clients())
                {
                    boolean isP_Drawer = (p.ID() == drawer.ID());
                    String wOne = isP_Drawer ? wordsToChoose.get(0) : null;
                    String wTwo = isP_Drawer ? wordsToChoose.get(1) : null;
                    String wThree = isP_Drawer ? wordsToChoose.get(2) : null;
                    NTDecisionPayload payload = new NTDecisionPayload(drawer.ID(), round, wOne, wTwo, wThree);
                    p.Out().SendCommand(CommandCode.NT_DECISION, payload);
                }

                while (isWaitingForDrawerChoice)
                {
                    if (drawer == null || !master.Clients().contains(drawer))
                    {
                        isWaitingForDrawerChoice = false;
                        drawer = null;
                        break;
                    }
                    try
                    {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }

                if (drawer == null)
                {
                    playerIndex++;
                    continue;
                }

                System.out.println("Choosen word : " + chosenWord);
                String maskedWord = MaskWord(chosenWord);

                for (PlayerHandler p : master.Clients())
                {
                    boolean isP_Drawer = (p.ID() == drawer.ID());
                    String wordToSend = isP_Drawer ? chosenWord : maskedWord;
                    NTBeginPayload payload = new NTBeginPayload(GameCommonMetadata.ROUND_TIME, wordToSend);
                    p.Out().SendCommand(CommandCode.NT_BEGIN, payload);
                }

                this.canDraw = true;
                this.roundRunning = true;
                this.cancelRound = false;
                this.playersWhoFoundWord.clear();
                this.playerFindTimes.clear();
                this.roundStartTime = System.currentTimeMillis();

                long durationMs = (long)(GameCommonMetadata.ROUND_TIME * 1000);

                while (roundRunning && (System.currentTimeMillis() - roundStartTime < durationMs))
                {
                    try
                    {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e)
                    {
                    }
                }

                this.canDraw = false;
                this.roundRunning = false;

                List<EndRoundPayload.PlayerScoreInfo> scoresInfo = new ArrayList<>();
                int drawerPoints = 0;
                int totalPlayers = master.Clients().size();

                if (drawer != null && master.Clients().contains(drawer))
                {
                    drawerPoints = playersWhoFoundWord.size() * MAX_POINTS_ROUND / totalPlayers;
                    drawer.AddScore(drawerPoints);
                }

                for (PlayerHandler p : master.Clients())
                {
                    int gained = 0;

                    if (!cancelRound)
                    {
                        if (drawer != null && p.ID() == drawer.ID())
                        {
                            gained = drawerPoints;
                        }
                        else if (playersWhoFoundWord.contains(p.ID()))
                        {
                            long timeTaken = playerFindTimes.getOrDefault(p.ID(), roundStartTime) - roundStartTime;
                            float ratio = 1.0f - ((float)timeTaken / durationMs);
                            gained = (int)(ratio * MAX_POINTS_ROUND);
                            if (gained < MIN_POINTS_ROUND) gained = MIN_POINTS_ROUND;

                            p.AddScore(gained);
                        }
                    }

                    scoresInfo.add(new EndRoundPayload.PlayerScoreInfo(p.ID(), gained, p.Score()));
                }

                EndRoundPayload endPayload = new EndRoundPayload(chosenWord, scoresInfo);
                for (PlayerHandler p : master.Clients())
                {
                    p.Out().SendCommand(CommandCode.END_ROUND_RESUME, endPayload);
                }

                try
                {
                    Thread.sleep((int)END_INTERLUDE_TIME * 1000);
                }
                catch (InterruptedException e)
                {
                }

                playerIndex++;
            }
            round++;
        }

        for (PlayerHandler p : master.Clients())
        {
            p.Out().SendCommand(CommandCode.END_GAME, null);
        }

        System.out.println("End game");
    }

    public void SetWordChoice(int choice)
    {
        if (!isWaitingForDrawerChoice) return;

        if (choice >= 0 && choice < wordsToChoose.size())
        {
            chosenWord = wordsToChoose.get(choice);
            isWaitingForDrawerChoice = false;
        }
    }

    private String MaskWord(String input)
    {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder();
        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++)
        {
            char c = chars[i];
            if (Character.isLetter(c)) sb.append("_" + (i != (chars.length - 1) ? " " : ""));
            else sb.append(c);
        }
        return sb.toString();
    }

    public int WordDistance(String guess)
    {
        if (chosenWord == null || guess == null) return Integer.MAX_VALUE;

        String target = chosenWord.toLowerCase().trim();
        String attempt = guess.toLowerCase().trim();

        int[] costs = new int[attempt.length() + 1];

        for (int j = 0; j < costs.length; j++)
            costs[j] = j;

        for (int i = 1; i <= target.length(); i++)
        {
            costs[0] = i;
            int nw = i - 1;

            for (int j = 1; j <= attempt.length(); j++)
            {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        target.charAt(i - 1) == attempt.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }

        return costs[attempt.length()];
    }
}