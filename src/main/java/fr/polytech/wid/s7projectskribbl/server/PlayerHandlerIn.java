package fr.polytech.wid.s7projectskribbl.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.*;

/**
 * Classe pour recevoir des commandes et des informations au client.
 */
public class PlayerHandlerIn extends Thread
{
    private final InputStream in;
    private final PlayerHandler handler;
    private final Socket clientSocket;
    private volatile boolean running;

    private String username;

    public PlayerHandlerIn(Socket clientSocket, PlayerHandler handler) throws IOException
    {
        this.clientSocket = clientSocket;
        this.handler = handler;
        this.in = clientSocket.getInputStream();
        this.running = true;
    }

    public InputStream In()
    {
        return this.in;
    }

    public void run()
    {
        try
        {
            while (running && !clientSocket.isClosed())
            {
                int code = in.read();

                if (code == -1)
                {
                    running = false;
                    handler.TerminateConnection(TerminatedConnectionType.CLIENT_LOGIC);
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
            if (running)
            {
                handler.Master().Logger().LogLn("Connexion interrompue pour " + handler.IP() + " : " + e.getMessage());
            }
        }
    }

    public void Close()
    {
        this.running = false;
        try
        {
            if (this.in != null)
            {
                this.in.close();
            }
        }
        catch (IOException e)
        {
            this.handler.Master().Logger().LogLn("Erreur: " + e.getMessage());
        }
    }
}
