package fr.polytech.wid.s7projectskribbl.common;

@FunctionalInterface
public interface CommandAction
{
    void Execute(byte[] data);
}