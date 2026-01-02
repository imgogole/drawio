package fr.polytech.wid.s7projectskribbl.server.actions;

import fr.polytech.wid.s7projectskribbl.server.PlayerHandler;

@FunctionalInterface
public interface ServerAction
{
    void Execute(PlayerHandler player, byte[] data);
}
