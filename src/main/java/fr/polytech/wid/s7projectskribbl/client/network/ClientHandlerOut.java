package fr.polytech.wid.s7projectskribbl.client.network;

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

    public <T extends Payload> void SendCommand(int code, T p)
    {
        try
        {
            ByteBuffer buffer;
            if (p != null)
            {
                byte[] payload = p.ToBytes();
                int payloadSize = payload.length;

                buffer = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + payloadSize);
                buffer.put((byte)code);
                buffer.putInt(payloadSize);
                buffer.put(payload);
            }
            else
            {
                // La requête ne nécessite pas de payload.
                buffer = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES);
                buffer.put((byte)code);
                buffer.putInt(0);
            }

            out.write(buffer.array());
            out.flush();
        }
        catch (IOException e)
        {
            System.err.println("Erreur d'envoi : " + e.getMessage());
        }
    }
}
