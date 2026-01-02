package fr.polytech.wid.s7projectskribbl.server;

import fr.polytech.wid.s7projectskribbl.common.payloads.Payload;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
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

    public <T extends Payload> void SendCommand(int code, T p)
    {
        try
        {
            byte[] payload = p.ToBytes();
            int payloadSize = payload.length;

            ByteBuffer buffer = ByteBuffer.allocate(Byte.BYTES + Integer.BYTES + payloadSize);
            buffer.put((byte)code);
            buffer.putInt(payloadSize);
            buffer.put(payload);

            out.write(buffer.array());
            out.flush();
        }
        catch (IOException e)
        {
            System.err.println("Erreur d'envoi : " + e.getMessage());
        }
    }
}
