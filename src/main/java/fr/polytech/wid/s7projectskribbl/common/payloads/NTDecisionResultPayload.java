package fr.polytech.wid.s7projectskribbl.common.payloads;

import java.nio.ByteBuffer;

public class NTDecisionResultPayload extends Payload
{
    private int chosenWordIndex;

    public int ChosenWordIndex()
    {
        return chosenWordIndex;
    }

    public NTDecisionResultPayload(int index)
    {
        this.chosenWordIndex = index;
    }

    public NTDecisionResultPayload()
    {

    }

    @Override
    public void Parse(byte[] payload)
    {
        ByteBuffer bb = ByteBuffer.wrap(payload);
        chosenWordIndex = bb.getInt();
    }

    @Override
    public ByteBuffer ToBytes()
    {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(chosenWordIndex);
        bb.flip();
        return bb;
    }
}
