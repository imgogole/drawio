package fr.polytech.wid.s7projectskribbl.common.payloads;

import java.nio.charset.StandardCharsets;

public class ServerMessagePayload extends Payload
{
    private String message;

    public String Message()
    {
        return message;
    }

    public ServerMessagePayload(String msg)
    {
        this.message = msg;
    }

    @Override
    public void Parse(byte[] payload)
    {
        message = new String(payload, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] ToBytes()
    {
        return message.getBytes(StandardCharsets.UTF_8);
    }
}
