package fr.polytech.wid.s7projectskribbl.server;

public record CommandRecord(PlayerHandler player, int code, byte[] payload)
{
}
