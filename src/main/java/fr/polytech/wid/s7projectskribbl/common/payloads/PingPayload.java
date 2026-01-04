package fr.polytech.wid.s7projectskribbl.common.payloads;

import java.nio.ByteBuffer;
import java.time.Instant;

public class PingPayload extends Payload
{
    private long pingTimeServer;
    private long pingTimeClient;

    public PingPayload()
    {
        this.pingTimeServer = -1;
        this.pingTimeClient = -1;
    }

    @Override
    public void Parse(byte[] payload)
    {
        ByteBuffer bb = ByteBuffer.wrap(payload);
        pingTimeServer = bb.getLong();
        pingTimeClient = bb.getLong();
    }

    @Override
    public byte[] ToBytes()
    {
        ByteBuffer bb = ByteBuffer.allocate(Long.BYTES * 2);
        bb.putLong(pingTimeServer);
        bb.putLong(pingTimeClient);
        return bb.array();
    }

    public void PingAsServer()
    {
        this.pingTimeServer = Instant.now().toEpochMilli();
    }

    public void PingAsClient()
    {
        this.pingTimeClient = Instant.now().toEpochMilli();
    }

    public long FromNowServer()
    {
        return Instant.now().toEpochMilli() - this.pingTimeClient;
    }

    public long FromNowClient()
    {
        return Instant.now().toEpochMilli() - this.pingTimeServer;
    }
}
