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
    private boolean found = false;
    private int points = 0;

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

    public void SetFound(boolean found)
    {
        this.found = found;
    }

    public boolean Found()
    {
        return found;
    }

    public void SetDrawer(boolean drawer)
    {
        this.drawer = drawer;
    }

    public boolean IsReady()
    {
        return ready;
    }

    public void SetReady(boolean ready)
    {
        this.ready = ready;
    }

    public int GetPoints()
    {
        return points;
    }

    public void SetPoints(int points)
    {
        this.points = points;
    }

    public void AddPoints(int points)
    {
        this.points += points;
    }
}
