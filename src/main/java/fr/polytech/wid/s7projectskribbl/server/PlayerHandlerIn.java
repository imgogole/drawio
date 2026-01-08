package fr.polytech.wid.s7projectskribbl.server;

import fr.polytech.wid.s7projectskribbl.common.TerminatedConnectionType;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
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
                    handler.Master().OnPlayerDisconnect(handler, TerminatedConnectionType.CLIENT_LOGIC);
                    break;
                }
                if (code == 0)
                {
                    // On a reçu un battement de cœur, cela veut dire que le client est toujours connecté
                    // On peut totalement ignorer la suite du code.
                    continue;
                }

                byte[] timestampBuf = in.readNBytes(8);
                if (timestampBuf.length < 8) break;

                long timestamp = ByteBuffer.wrap(timestampBuf).order(ByteOrder.BIG_ENDIAN).getLong();

                byte[] sizeBuf = in.readNBytes(4);
                if (sizeBuf.length < 4) break;

                int length = ByteBuffer.wrap(sizeBuf).order(ByteOrder.BIG_ENDIAN).getInt();

                byte[] payload;

                if (length > 0)
                {
                    payload = in.readNBytes(length);
                    if (payload.length < length) break;
                }
                else
                {
                    payload = new byte[0];
                }

                handler.Master().CommandHandler().QueueIncomeCommand(this.handler, code, timestamp, payload);
            }
        }
        catch (SocketTimeoutException e)
        {
            handler.Master().OnPlayerDisconnect(handler, TerminatedConnectionType.TIMEOUT);
        }
        catch (IOException e)
        {
            handler.Master().OnPlayerDisconnect(handler, TerminatedConnectionType.CLIENT_LOGIC);
        }
    }
}
