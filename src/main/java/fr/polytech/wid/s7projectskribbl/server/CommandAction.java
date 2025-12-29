package fr.polytech.wid.s7projectskribbl.server;

@FunctionalInterface
public interface CommandAction
{
    void Execute(byte[] data);
}