package fr.polytech.wid.s7projectskribbl.common.payloads;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class DrawPayload extends Payload
{
    public enum DrawAction
    {
        START,
        DRAG,
        FILL
    }

    private DrawAction action;
    private double x;
    private double y;
    private String color; // Format Hex (ex: "#FF0000")
    private double size;

    public DrawPayload(DrawAction action, double x, double y, String color, double size)
    {
        this.action = action;
        this.x = x;
        this.y = y;
        this.color = (color != null) ? color : "#000000";
        this.size = size;
    }

    public DrawPayload()
    {
    }

    public DrawAction Action() { return action; }
    public double X() { return x; }
    public double Y() { return y; }
    public String Color() { return color; }
    public double Size() { return size; }

    @Override
    public void Parse(byte[] payload)
    {
        ByteBuffer buffer = ByteBuffer.wrap(payload);

        int actionOrd = buffer.getInt();
        if (actionOrd >= 0 && actionOrd < DrawAction.values().length)
        {
            this.action = DrawAction.values()[actionOrd];
        }
        else
        {
            this.action = DrawAction.START;
        }

        this.x = buffer.getDouble();
        this.y = buffer.getDouble();

        this.size = buffer.getDouble();

        int colorLength = buffer.getInt();
        byte[] colorBytes = new byte[colorLength];
        buffer.get(colorBytes);
        this.color = new String(colorBytes, StandardCharsets.UTF_8);
    }

    @Override
    public ByteBuffer ToBytes()
    {
        byte[] colorBytes = color.getBytes(StandardCharsets.UTF_8);
        int colorLength = colorBytes.length;

        int capacity = 4 + 8 + 8 + 8 + 4 + colorLength;

        ByteBuffer buffer = ByteBuffer.allocate(capacity);

        buffer.putInt(action.ordinal());
        buffer.putDouble(x);
        buffer.putDouble(y);
        buffer.putDouble(size);

        buffer.putInt(colorLength);
        buffer.put(colorBytes);

        buffer.flip();
        return buffer;
    }
}