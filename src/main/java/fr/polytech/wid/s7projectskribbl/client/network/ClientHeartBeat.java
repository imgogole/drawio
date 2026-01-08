package fr.polytech.wid.s7projectskribbl.client.network;


public class ClientHeartBeat extends Thread
{
    private volatile boolean beating;
    private final long beatRate;
    private final ClientHandler handler;

    public ClientHeartBeat(ClientHandler handler, long beatRate)
    {
        this.beating = true;
        this.beatRate = beatRate;
        this.handler = handler;
    }

    public void run()
    {
        while (beating)
        {
            try
            {
                if (beating)
                {
                    handler.Out().Beat();
                }
                Thread.sleep((beatRate * 1000));
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
                this.beating = false;
            }
        }
    }

    public void Kill()
    {
        this.beating = false;
    }
}
