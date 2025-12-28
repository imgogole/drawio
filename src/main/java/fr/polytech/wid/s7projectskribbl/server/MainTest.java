package fr.polytech.wid.s7projectskribbl.server;

import java.util.Scanner;

public class MainTest
{
    public static void main(String[] args) throws Exception
    {
        GameMaster master = new GameMaster(5555, 9999);
        String ip = master.Begin();
        master.Logger().LogLn("Server IP: " + ip);
    }
}
