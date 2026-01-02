package fr.polytech.wid.s7projectskribbl.client.actions;

@FunctionalInterface
public interface ClientAction
{
    void Execute(byte[] data);
}
