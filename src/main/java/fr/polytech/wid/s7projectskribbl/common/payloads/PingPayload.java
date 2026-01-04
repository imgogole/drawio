package fr.polytech.wid.s7projectskribbl.common.payloads;

import java.nio.ByteBuffer;
import java.time.Instant;

public class PingPayload extends Payload
{
    private long timestamp;

    @Override
    public void Parse(byte[] payload)
    {
        ByteBuffer bb = ByteBuffer.wrap(payload);
        this.timestamp = bb.getLong();
    }

    @Override
    public ByteBuffer ToBytes()
    {
        ByteBuffer bb = ByteBuffer.allocate(Long.BYTES);
        bb.putLong(this.timestamp);
        bb.flip();
        return bb;
    }

    public void SetTimestamp()
    {
        this.timestamp = Instant.now().toEpochMilli();
    }

    public long GetElapsed()
    {
        return Instant.now().toEpochMilli() - this.timestamp;
    }
}
