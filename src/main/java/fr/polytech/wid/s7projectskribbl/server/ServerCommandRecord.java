package fr.polytech.wid.s7projectskribbl.server;

public record ServerCommandRecord(PlayerHandler player, int code, long timestamp, byte[] payload)
        implements Comparable<ServerCommandRecord>
{
    @Override
    public int compareTo(ServerCommandRecord other)
    {
        return Long.compare(this.timestamp, other.timestamp);
    }
}
