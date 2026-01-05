package fr.polytech.wid.s7projectskribbl.common.payloads;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ServerMessagePayload extends Payload
{
    private String message;

    public ServerMessagePayload()
    {
        this.message = "";
    }

    public ServerMessagePayload( String msg)
    {
        this.message = (msg != null) ? msg : "";
    }

    public String Message()
    {
        return message;
    }

    @Override
    public void Parse(byte[] payload)
    {
        if (payload == null || payload.length < Integer.BYTES) return;

        ByteBuffer bb = ByteBuffer.wrap(payload);
        int byteLength = bb.getInt();

        if (bb.remaining() < byteLength) return;

        byte[] msgBytes = new byte[byteLength];
        bb.get(msgBytes);
        this.message = new String(msgBytes, StandardCharsets.UTF_8);
    }

    @Override
    public ByteBuffer ToBytes()
    {
        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);

        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES + msgBytes.length);

        bb.putInt(msgBytes.length);
        bb.put(msgBytes);

        bb.flip();

        return bb;
    }
}
