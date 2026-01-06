package fr.polytech.wid.s7projectskribbl.client.network;

import fr.polytech.wid.s7projectskribbl.common.CommandCode;
import fr.polytech.wid.s7projectskribbl.common.payloads.Payload;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ClientHandlerOut
{
    private final OutputStream out;
    private final ClientHandler handler;
    private final Socket clientSocket;

    public ClientHandlerOut(ClientHandler handler, Socket clientSocket) throws IOException
    {
        this.out = clientSocket.getOutputStream();
        this.handler = handler;
        this.clientSocket = clientSocket;
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
            int payloadSize = (payloadBuffer != null) ? payloadBuffer.remaining() : 0;
            long timestamp = System.currentTimeMillis();

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
