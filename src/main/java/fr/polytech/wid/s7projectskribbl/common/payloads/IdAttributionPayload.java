package fr.polytech.wid.s7projectskribbl.common.payloads;

import java.nio.ByteBuffer;

public class IdAttributionPayload extends Payload
{
    private int id;

    public int ID() {return this.id;}

    public IdAttributionPayload(int id)
    {
        this.id = id;
    }

    public IdAttributionPayload()
    {

    }

    @Override
    public void Parse(byte[] payload)
    {
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        this.id = buffer.getInt();
    }

    @Override
    public ByteBuffer ToBytes()
    {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(this.id);
        buffer.flip();
        return buffer;
    }
}
