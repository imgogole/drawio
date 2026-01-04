package fr.polytech.wid.s7projectskribbl.client.actions;

import fr.polytech.wid.s7projectskribbl.client.network.ClientHandler;
import fr.polytech.wid.s7projectskribbl.common.CommandCode;
import fr.polytech.wid.s7projectskribbl.common.payloads.PingPayload;

public class CPingAction implements ClientAction
{
    @Override
    public void Execute(byte[] data)
    {
        PingPayload pingPayload = new PingPayload();
        pingPayload.Parse(data);

        System.out.println("Ping from server received.");
        ClientHandler.Singleton().Out().SendCommand(CommandCode.PING.Code(), pingPayload);
    }
}