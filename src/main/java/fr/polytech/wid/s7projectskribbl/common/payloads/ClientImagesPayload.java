package fr.polytech.wid.s7projectskribbl.common.payloads;

import fr.polytech.wid.s7projectskribbl.common.CommonUtilities;
import fr.polytech.wid.s7projectskribbl.common.payloads.records.ClientImageItem;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ClientImagesPayload extends Payload
{
    private List<ClientImageItem> items = new ArrayList<>();

    public ClientImagesPayload(List<ClientImageItem> items)
    {
        this.items = items;
    }

    public ClientImagesPayload()
    {

    }

    public List<ClientImageItem> ClientImages()
    {
        return this.items;
    }

    @Override
    public void Parse(byte[] payload)
    {
        if (payload == null || payload.length < 4) return;

        ByteBuffer bb = ByteBuffer.wrap(payload);
        this.items = new ArrayList<>();

        try
        {
            int itemCount = bb.getInt();

            for (int i = 0; i < itemCount; i++)
            {
                int id = bb.getInt();
                boolean ready = bb.getInt() == 1;
                int usernameLength = bb.getInt();
                byte[] usernameBytes = new byte[usernameLength];
                bb.get(usernameBytes);
                String username = new String(usernameBytes, StandardCharsets.UTF_8);
                int imageLength = bb.getInt();
                byte[] imageBytes = new byte[imageLength];
                bb.get(imageBytes);
                BufferedImage img = CommonUtilities.BytesToImage(imageBytes);
                this.items.add(new ClientImageItem(id, username, img, ready));
            }
        }
        catch (Exception e)
        {
            System.err.println("Erreur lors du parsing du ClientImagesPayload : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public ByteBuffer ToBytes()
    {
        int totalSize = Integer.BYTES;
        ArrayList<ByteBuffer> itemBuffers = new ArrayList<>();

        for (ClientImageItem item : this.items)
        {
            ByteBuffer bb = FromItem(item);
            itemBuffers.add(bb);
            totalSize += bb.limit();
        }

        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        buffer.putInt(this.items.size());
        for (ByteBuffer bb : itemBuffers)
        {
            buffer.put(bb);
        }

        buffer.flip();
        return buffer;
    }

    private ByteBuffer FromItem(ClientImageItem item)
    {
        byte[] username = item.username().getBytes(StandardCharsets.UTF_8);
        int usernameLength = username.length;
        byte[] image = new byte[0];
        try
        {
            image = CommonUtilities.ImageToBytes(item.image());
        }
        catch (Exception e) {}
        int imageLength = image.length;

        ByteBuffer bb =  ByteBuffer.allocate(4 * Integer.BYTES + usernameLength + imageLength);
        bb.putInt(item.id());
        bb.putInt(item.ready() ? 1 : 0);
        bb.putInt(usernameLength);
        bb.put(username);
        bb.putInt(imageLength);
        bb.put(image);

        bb.flip();
        return bb;
    }
}
