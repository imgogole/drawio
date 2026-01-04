package fr.polytech.wid.s7projectskribbl.common.payloads;

import java.nio.ByteBuffer;

/**
 * Classe abstraite pour les données des commandes réseaux
 */
public abstract class Payload
{
    /**
     * Converti les données brutes en un payload utilisable.
     * @param payload
     */
    public abstract void Parse(byte[] payload);

    /**
     * Cenverti le payload en des données brutes pour l'envoi en socket.
     * @return
     */
    public abstract ByteBuffer ToBytes();

}
