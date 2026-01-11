package fr.polytech.wid.s7projectskribbl.common.payloads;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class NTDecisionPayload extends Payload
{
    private int drawerID;
    private int round;
    private String wordOne;
    private String wordTwo;
    private String wordThree;

    public int DrawerID()
    {
        return drawerID;
    }
    public String WordOne()
    {
        return wordOne;
    }
    public String WordTwo()
    {
        return wordTwo;
    }
    public String WordThree()
    {
        return wordThree;
    }
    public int Round()
    {
        return round;
    }

    public NTDecisionPayload(int id, int round, String wOne, String wTwo, String wThree)
    {
        this.drawerID = id;
        this.round = round;
        this.wordOne = wOne;
        this.wordTwo = wTwo;
        this.wordThree = wThree;
    }

    public NTDecisionPayload()
    {

    }

    @Override
    public void Parse(byte[] payload)
    {
        ByteBuffer bb = ByteBuffer.wrap(payload);
        drawerID = bb.getInt();
        round = bb.getInt();
        int wOneLength =  bb.getInt();
        byte[] wOneBytes = new byte[wOneLength];
        bb.get(wOneBytes);
        wordOne = new String(wOneBytes, StandardCharsets.UTF_8);
        int wTwoLength =  bb.getInt();
        byte[] wTwoBytes = new byte[wTwoLength];
        bb.get(wTwoBytes);
        wordTwo = new String(wTwoBytes, StandardCharsets.UTF_8);
        int wThreeLength =  bb.getInt();
        byte[] wThreeBytes = new byte[wThreeLength];
        bb.get(wThreeBytes);
        wordThree = new String(wThreeBytes, StandardCharsets.UTF_8);
    }

    @Override
    public ByteBuffer ToBytes()
    {
        byte[] wOneBytes = wordOne != null ? wordOne.getBytes(StandardCharsets.UTF_8) : new byte[0];
        byte[] wTwoBytes = wordTwo != null ? wordTwo.getBytes(StandardCharsets.UTF_8) : new byte[0];
        byte[] wThreeBytes = wordThree != null ? wordThree.getBytes(StandardCharsets.UTF_8) : new byte[0];
        int wOneByteLength = wOneBytes.length;
        int wTwoByteLength = wTwoBytes.length;
        int wThreeByteLength = wThreeBytes.length;

        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES * 5 + wOneByteLength + wTwoByteLength + wThreeByteLength);
        bb.putInt(drawerID);
        bb.putInt(round);
        bb.putInt(wOneByteLength);
        bb.put(wOneBytes);
        bb.putInt(wTwoByteLength);
        bb.put(wTwoBytes);
        bb.putInt(wThreeByteLength);
        bb.put(wThreeBytes);

        bb.flip();
        return bb;
    }
}
