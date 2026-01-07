package fr.polytech.wid.s7projectskribbl.client.network;

import java.awt.image.BufferedImage;

/**
 * Cette classe représente un joueur de manière simplifié.
 */
public class ClientImage
{
    private final int id;
    private final String username;
    private final BufferedImage avatar;
    private boolean ready = false;
    private boolean drawer = false;

    public ClientImage(int ID, String Username, BufferedImage Avatar, boolean ready)
    {
        this.id = ID;
        this.username = Username;
        this.avatar = Avatar;
        this.ready = ready;
    }

    public int ID()
    {
        return id;
    }

    public String Username()
    {
        return username;
    }

    public BufferedImage Avatar()
    {
        return avatar;
    }

    public boolean IsDrawer()
    {
        return drawer;
    }

    public void SetDrawer(boolean drawer)
    {
        drawer = drawer;
    }

    public boolean IsReady()
    {
        return ready;
    }

    public void SetReady(boolean ready)
    {
        this.ready = ready;
    }
}
