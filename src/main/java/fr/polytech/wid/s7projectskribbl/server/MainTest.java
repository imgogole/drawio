package fr.polytech.wid.s7projectskribbl.server;

public class MainTest
{
    public static void main(String[] args) throws Exception
    {
        GameMaster master = new GameMaster(5555);
        String ip = master.Begin();
        System.out.println("Server IP: " + ip);
        master.Terminate();
    }
}
