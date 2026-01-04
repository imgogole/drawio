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


    @Override
    public void Parse(byte[] payload)
    {
        ByteBuffer bb = ByteBuffer.wrap(payload);
        int lengthUsername = bb.getInt();
        ByteBuffer usernamebb = bb.slice();
        usernamebb.limit(lengthUsername);
        this.username = new String(usernamebb.array(), StandardCharsets.UTF_8);

    }

    @Override
    public ByteBuffer ToBytes()
    {
        return null;
    }
}
