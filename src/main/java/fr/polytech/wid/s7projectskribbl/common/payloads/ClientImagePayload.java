package fr.polytech.wid.s7projectskribbl.common.payloads;

import fr.polytech.wid.s7projectskribbl.common.CommonUtilities;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ClientImagePayload extends Payload
{
    private int id;
    private String username;
    private int state; // 0: disconnected, 1: connected & not ready, 2: connected & ready
    private BufferedImage image;

    public ClientImagePayload(int id, String username, int state, BufferedImage image)
    {
        if (username == null) this.username = "";
        else this.username = username;
        this.state = state;
        this.image = image;
        this.id = id;
    }

    public ClientImagePayload()
    {

    }

    public int ID()
    {
        return id;
    }

    public String Username()
    {
        return username;
    }

    public int State()
    {
        return state;
    }

    public BufferedImage Image()
    {
        return image;
    }

    @Override
    public void Parse(byte[] payload)
    {
        ByteBuffer bb = ByteBuffer.wrap(payload);

        id = bb.getInt();
        state = bb.getInt();
        int usernameLength = bb.getInt();
        byte[] usernameBytes = new byte[usernameLength];
        bb.get(usernameBytes);
        username = new String(usernameBytes, StandardCharsets.UTF_8);
        int imageLength = bb.getInt();
        byte[] imageBytes;

        try
        {
            imageBytes = new byte[imageLength];
            bb.get(imageBytes);
            image = CommonUtilities.BytesToImage(imageBytes);
        }
        catch (Exception e)
        {
            image = null;
            e.printStackTrace();
        }
    }

    @Override
    public ByteBuffer ToBytes()
    {
        byte[] usrnm = username.getBytes(StandardCharsets.UTF_8);
        int usrnmLength = usrnm.length;
        byte[] img = new byte[0];
        try
        {
            img = CommonUtilities.ImageToBytes(image);
        }
        catch (Exception e)
        {
        }
        int imageLength = img.length;

        ByteBuffer bb = ByteBuffer.allocate(4 * Integer.BYTES + usrnmLength + imageLength);
        bb.putInt(id);
        bb.putInt(state);
        bb.putInt(usrnmLength);
        bb.put(usrnm);
        bb.putInt(imageLength);
        bb.put(img);

        bb.flip();
        return bb;
    }
}
