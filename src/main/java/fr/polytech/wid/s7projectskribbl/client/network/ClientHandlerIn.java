package fr.polytech.wid.s7projectskribbl.client.network;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Classe pour recevoir des commandes et des informations du serveur.
 */
public class ClientHandlerIn extends Thread
{
    private final ClientHandler clientHandler;
    private final InputStream in;
    private final Socket clientSocket;

    private volatile boolean running;

    public ClientHandlerIn(ClientHandler handler, Socket socket) throws IOException
    {
        this.clientHandler = handler;
        this.clientSocket = socket;
        this.in = socket.getInputStream();
        this.running = true;
    }

    public void Close()
    {
        this.running = false;
    }

    @Override
    public void run()
    {
        try
        {
            while (running && !clientSocket.isClosed())
            {
                int code = in.read();
                if (code == -1) break;
                if (code == 0) continue;

                byte[] timestampBuf = in.readNBytes(8);
                long timestamp = ByteBuffer.wrap(timestampBuf).order(ByteOrder.BIG_ENDIAN).getLong();

                byte[] sizeBuf = in.readNBytes(4);
                int length = ByteBuffer.wrap(sizeBuf).order(ByteOrder.BIG_ENDIAN).getInt();

                byte[] payload = new byte[0];
                if (length > 0)
                {
                    payload = in.readNBytes(length);
                }

                clientHandler.QueueIncomeCommand(code, timestamp, payload);
            }
        }
        catch (IOException e)
        {
            System.err.println("Erreur IO ClientHandlerIn: " + e.getMessage());
        }
        catch (Exception e)
        {
            System.err.println("Erreur: ");
            e.printStackTrace();
        }
    }
}
