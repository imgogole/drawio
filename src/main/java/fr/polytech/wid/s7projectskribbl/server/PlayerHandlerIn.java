package fr.polytech.wid.s7projectskribbl.server;

import fr.polytech.wid.s7projectskribbl.common.TerminatedConnectionType;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.*;

/**
 * Classe pour recevoir des commandes et des informations du client.
 */
public class PlayerHandlerIn extends Thread
{
    private final InputStream in;
    private final PlayerHandler handler;
    private final Socket clientSocket;

    private String username;

    public PlayerHandlerIn(Socket clientSocket, PlayerHandler handler) throws IOException
    {
        this.clientSocket = clientSocket;
        this.handler = handler;
        this.in = clientSocket.getInputStream();
    }

    public InputStream In()
    {
        return this.in;
    }

    public void run()
    {
        try
        {
            while (!clientSocket.isClosed())
            {
                int code = in.read();

                if (code == -1)
                {
                    break;
                }

                byte[] sizeBuf = in.readNBytes(4);
                if (sizeBuf.length < 4) break;

                int length = ByteBuffer.wrap(sizeBuf).order(ByteOrder.BIG_ENDIAN).getInt();

                byte[] payload = in.readNBytes(length);
                if (payload.length < length) break;

                handler.Master().CommandHandler().QueueIncomeCommand(this.handler, code, payload);
            }
        }
        catch (IOException e)
        {
            System.out.println("Connexion terminée pour " + handler.IP() + " : " + e.getMessage());
        }
    }
}
