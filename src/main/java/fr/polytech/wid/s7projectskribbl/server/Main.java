package fr.polytech.wid.s7projectskribbl.server;

import fr.polytech.wid.s7projectskribbl.common.GameCommonMetadata;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        GameMaster master = new GameMaster(GameCommonMetadata.GamePort);
        String ip = master.Begin();
        System.out.println("Server is open! Port: " + String.valueOf(GameCommonMetadata.GamePort));
    }
}
