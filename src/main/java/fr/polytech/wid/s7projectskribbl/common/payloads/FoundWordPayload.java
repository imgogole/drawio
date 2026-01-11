package fr.polytech.wid.s7projectskribbl.common.payloads;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class FoundWordPayload extends Payload
{
    private int finderId;
    private String word;

    public FoundWordPayload()
    {
        this.finderId = -1;
        this.word = "";
    }

    public FoundWordPayload(int finderId, String word)
    {
        this.finderId = finderId;
        this.word = (word != null) ? word : "";
    }

    public int FinderId()
    {
        return this.finderId;
    }

    public String Word()
    {
        return this.word;
    }

    @Override
    public void Parse(byte[] payload)
    {
        ByteBuffer buffer = ByteBuffer.wrap(payload);

        this.finderId = buffer.getInt();

        int length = buffer.getInt();

        byte[] wordBytes = new byte[length];
        buffer.get(wordBytes);

        this.word = new String(wordBytes, StandardCharsets.UTF_8);
    }

    @Override
    public ByteBuffer ToBytes()
    {
        byte[] wordBytes = this.word.getBytes(StandardCharsets.UTF_8);
        int length = wordBytes.length;

        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + Integer.BYTES + length);

        buffer.putInt(this.finderId);
        buffer.putInt(length);
        buffer.put(wordBytes);

        buffer.flip();
        return buffer;
    }
}