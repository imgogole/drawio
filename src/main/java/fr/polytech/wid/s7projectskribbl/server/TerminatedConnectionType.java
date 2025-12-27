package fr.polytech.wid.s7projectskribbl.server;

public enum TerminatedConnectionType
{
    /** Terminaison de la connexion par défaut */
    DEFAULT,
    /** Le client a volontairement été déconnecté */
    CLIENT_LOGIC,
    /** Le serveur a volontairement été déconnecté */
    SERVER_LOGIC,
    /** Le serveur n'a pas reçu de réponse de la part du client dans le temps imparti */
    TIMEOUT,
    /** Le client a été déconnecté par la décision des autres joueurs */
    KICKED,
}
