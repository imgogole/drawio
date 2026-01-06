package fr.polytech.wid.s7projectskribbl.common.payloads;

import java.nio.ByteBuffer;

public class ReadyPayload extends Payload
{
    private boolean ready;

    public ReadyPayload(boolean ready)
    {
        this.ready = ready;
    }

    public ReadyPayload()
    {
    }

    @Override
    public void Parse(byte[] payload)
    {
        this.ready = payload[0] == 1;
    }

    @Override
    public ByteBuffer ToBytes()
    {
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.put((byte)(ready ? 1 : 0));
        buffer.flip();
        return buffer;
    }

    public boolean Ready()
    {
        return ready;
    }
}
