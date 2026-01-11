package fr.polytech.wid.s7projectskribbl.common.payloads;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ServerMessagePayload extends Payload
{
    private String message;
    private String color;

    public ServerMessagePayload()
    {
        this.message = "";
        this.color = "#000000";
    }

    public ServerMessagePayload(String msg)
    {
        this.message = (msg != null) ? msg : "";
        this.color = "#000000";
    }

    public ServerMessagePayload(String msg, String color)
    {
        this.message = (msg != null) ? msg : "";
        this.color = (color != null) ? color : "#000000";
    }

    public String Message()
    {
        return message;
    }

    public String Color()
    {
        return color;
    }

    @Override
    public void Parse(byte[] payload)
    {
        if (payload == null || payload.length < Integer.BYTES) return;

        ByteBuffer bb = ByteBuffer.wrap(payload);

        int msgLength = bb.getInt();
        if (bb.remaining() < msgLength) return;

        byte[] msgBytes = new byte[msgLength];
        bb.get(msgBytes);
        this.message = new String(msgBytes, StandardCharsets.UTF_8);

        if (bb.remaining() >= Integer.BYTES)
        {
            int colorLength = bb.getInt();
            if (bb.remaining() >= colorLength)
            {
                byte[] colorBytes = new byte[colorLength];
                bb.get(colorBytes);
                this.color = new String(colorBytes, StandardCharsets.UTF_8);
            }
        }
        else
        {
            this.color = "#000000";
        }
    }

    @Override
    public ByteBuffer ToBytes()
    {
        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] colorBytes = color.getBytes(StandardCharsets.UTF_8);
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES + msgBytes.length + Integer.BYTES + colorBytes.length);
        bb.putInt(msgBytes.length);
        bb.put(msgBytes);
        bb.putInt(colorBytes.length);
        bb.put(colorBytes);

        bb.flip();

        return bb;
    }
}