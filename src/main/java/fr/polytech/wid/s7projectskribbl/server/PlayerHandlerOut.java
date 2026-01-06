package fr.polytech.wid.s7projectskribbl.server;

import fr.polytech.wid.s7projectskribbl.common.CommandCode;
import fr.polytech.wid.s7projectskribbl.common.payloads.Payload;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Classe pour envoyer des commandes et des informations au client.
 */
public class PlayerHandlerOut
{
    private final OutputStream out;
    private final PlayerHandler handler;
    private final Socket clientSocket;

    public PlayerHandlerOut(Socket clientSocket, PlayerHandler handler) throws IOException
    {
        this.clientSocket = clientSocket;
        this.handler = handler;
        this.out = clientSocket.getOutputStream();
    }

    public OutputStream Out()
    {
        return this.out;
    }

    public <T extends Payload> void SendCommand(CommandCode code, T p)
    {
        try
        {
            ByteBuffer payloadBuffer = (p != null) ? p.ToBytes() : null;
            long timestamp = System.currentTimeMillis();
            int payloadSize = (payloadBuffer != null) ? payloadBuffer.remaining() : 0;

            ByteBuffer message = ByteBuffer.allocate(Byte.BYTES + Long.BYTES + Integer.BYTES + payloadSize);
            message.put((byte)(code.Code()));
            message.putLong(timestamp);
            message.putInt(payloadSize);

            if (payloadBuffer != null)
            {
                message.put(payloadBuffer);
            }

            out.write(message.array(), 0, message.position());
            out.flush();
        }
        catch (IOException e)
        {
            System.err.println("Erreur d'envoi : " + e.getMessage());
        }
    }
}
