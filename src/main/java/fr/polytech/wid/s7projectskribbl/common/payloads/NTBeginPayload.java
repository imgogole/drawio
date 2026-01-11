package fr.polytech.wid.s7projectskribbl.common.payloads;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class NTBeginPayload extends Payload
{
    private String word;
    private float seconds;

    public String Word()
    {
        return word;
    }

    public float Seconds()
    {
        return seconds;
    }

    public NTBeginPayload(float seconds, String word)
    {
        this.seconds = seconds;
        this.word = word;
    }

    public NTBeginPayload()
    {

    }

    @Override
    public void Parse(byte[] payload)
    {
        ByteBuffer buffer = ByteBuffer.wrap(payload);
        seconds = buffer.getFloat();
        int wordLength = buffer.getInt();
        byte[] wordBytes = new byte[wordLength];
        buffer.get(wordBytes);
        word = new String(wordBytes, StandardCharsets.UTF_8);
    }

    @Override
    public ByteBuffer ToBytes()
    {
        byte[] wordBytes = word.getBytes(StandardCharsets.UTF_8);
        int wordLength = wordBytes.length;
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES + Float.BYTES + wordLength);
        buffer.putFloat(seconds);
        buffer.putInt(wordLength);
        buffer.put(wordBytes);
        buffer.flip();
        return buffer;
    }
}
