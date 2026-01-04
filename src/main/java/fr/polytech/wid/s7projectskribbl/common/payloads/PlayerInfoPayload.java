package fr.polytech.wid.s7projectskribbl.common.payloads;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class PlayerInfoPayload extends Payload
{
    /*
    Encodage:
        USERNAME_LENGTH | USERNAME_UTF8                 | PNG_LENGTH    | PNG_DATA
        4 OCTETS        | 8 * USERNAME_LENGTH OCTETS    | 4 OCTETS      | PNG_LENGTH
     */

    private String username;
    private byte[] avatarPng;

    public PlayerInfoPayload(String username, byte[] avatarPng)
    {
        this.username = username;
        this.avatarPng = avatarPng;
    }

    public PlayerInfoPayload() {}

    @Override
    public void Parse(byte[] payload)
    {
        ByteBuffer bb = ByteBuffer.wrap(payload);

        int lengthUsername = bb.getInt();
        byte[] nameBytes = new byte[lengthUsername];
        bb.get(nameBytes);
        this.username = new String(nameBytes, StandardCharsets.UTF_8);

        int lengthPng = bb.getInt();
        this.avatarPng = new byte[lengthPng];
        bb.get(this.avatarPng);
    }

    @Override
    public ByteBuffer ToBytes()
    {
        byte[] nameBytes = username.getBytes(StandardCharsets.UTF_8);

        int totalSize = Integer.BYTES + nameBytes.length + Integer.BYTES + avatarPng.length;

        ByteBuffer bb = ByteBuffer.allocate(totalSize);

        bb.putInt(nameBytes.length);
        bb.put(nameBytes);

        bb.putInt(avatarPng.length);
        bb.put(avatarPng);

        bb.flip();
        return bb;
    }

    public String Username() { return username; }
    public byte[] AvatarPng() { return avatarPng; }
}
