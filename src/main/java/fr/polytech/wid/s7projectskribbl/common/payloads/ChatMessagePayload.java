package fr.polytech.wid.s7projectskribbl.common.payloads;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ChatMessagePayload extends Payload
{
    private String message;
    private int id;

    public String Message()
    {
        return message;
    }
    public int ID() { return id; }

    public ChatMessagePayload()
    {

    }

    public ChatMessagePayload(int id, String message)
    {
        this.message = message != null ? message : "";
        this.id = id;
    }

    @Override
    public void Parse(byte[] payload)
    {
        if (payload == null || payload.length < Integer.BYTES) return;

        ByteBuffer bb = ByteBuffer.wrap(payload);
        int id = bb.getInt();
        int byteLength = bb.getInt();

        if (bb.remaining() < byteLength) return;

        byte[] msgBytes = new byte[byteLength];
        bb.get(msgBytes);
        this.id = id;
        this.message = new String(msgBytes, StandardCharsets.UTF_8);
    }

    @Override
    public ByteBuffer ToBytes()
    {
        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);

        ByteBuffer bb = ByteBuffer.allocate(2*Integer.BYTES + msgBytes.length);

        bb.putInt(id);
        bb.putInt(msgBytes.length);
        bb.put(msgBytes);

        bb.flip();

        return bb;
    }
}
