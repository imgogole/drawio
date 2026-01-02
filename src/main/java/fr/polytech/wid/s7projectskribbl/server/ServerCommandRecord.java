package fr.polytech.wid.s7projectskribbl.server;

public record ServerCommandRecord(PlayerHandler player, int code, byte[] payload)
{
}
