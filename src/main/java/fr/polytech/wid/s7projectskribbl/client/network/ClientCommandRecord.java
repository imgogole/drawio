package fr.polytech.wid.s7projectskribbl.client.network;

public record ClientCommandRecord(int code, long timestamp, byte[] payload) implements Comparable<ClientCommandRecord>
{
    @Override
    public int compareTo(ClientCommandRecord o)
    {
        return Long.compare(timestamp, o.timestamp);
    }
}
