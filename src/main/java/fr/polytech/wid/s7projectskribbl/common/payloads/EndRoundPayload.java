package fr.polytech.wid.s7projectskribbl.common.payloads;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class EndRoundPayload extends Payload
{
    private String wordToGuess;
    private List<PlayerScoreInfo> playerScores;

    public static class PlayerScoreInfo
    {
        private int id;
        private int gainedPoints;
        private int totalPoints;

        public PlayerScoreInfo(int id, int gainedPoints, int totalPoints)
        {
            this.id = id;
            this.gainedPoints = gainedPoints;
            this.totalPoints = totalPoints;
        }

        public int Id() { return id; }
        public int GainedPoints() { return gainedPoints; }
        public int TotalPoints() { return totalPoints; }
    }

    public EndRoundPayload()
    {
        this.wordToGuess = "";
        this.playerScores = new ArrayList<>();
    }

    public EndRoundPayload(String word, List<PlayerScoreInfo> scores)
    {
        this.wordToGuess = (word != null) ? word : "";
        this.playerScores = (scores != null) ? scores : new ArrayList<>();
    }

    public String WordToGuess()
    {
        return wordToGuess;
    }

    public List<PlayerScoreInfo> PlayerScores()
    {
        return playerScores;
    }

    @Override
    public void Parse(byte[] payload)
    {
        ByteBuffer bb = ByteBuffer.wrap(payload);

        int wordLength = bb.getInt();
        byte[] wordBytes = new byte[wordLength];
        bb.get(wordBytes);
        this.wordToGuess = new String(wordBytes, StandardCharsets.UTF_8);

        int listSize = bb.getInt();
        this.playerScores = new ArrayList<>(listSize);

        for (int i = 0; i < listSize; i++)
        {
            int id = bb.getInt();
            int gained = bb.getInt();
            int total = bb.getInt();
            this.playerScores.add(new PlayerScoreInfo(id, gained, total));
        }
    }

    @Override
    public ByteBuffer ToBytes()
    {
        byte[] wordBytes = wordToGuess.getBytes(StandardCharsets.UTF_8);
        int wordLen = wordBytes.length;
        int listSize = playerScores.size();

        int capacity = Integer.BYTES + wordLen + Integer.BYTES + (listSize * (3 * Integer.BYTES));

        ByteBuffer bb = ByteBuffer.allocate(capacity);

        bb.putInt(wordLen);
        bb.put(wordBytes);

        bb.putInt(listSize);
        for (PlayerScoreInfo info : playerScores)
        {
            bb.putInt(info.id);
            bb.putInt(info.gainedPoints);
            bb.putInt(info.totalPoints);
        }

        bb.flip();
        return bb;
    }
}