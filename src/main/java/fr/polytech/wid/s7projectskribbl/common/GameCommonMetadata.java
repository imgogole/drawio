package fr.polytech.wid.s7projectskribbl.common;

/**
 * Classe statique contenant les informations constantes pour le jeu.
 */
public final class GameCommonMetadata
{
    /**
     * Le nom du jeu. Cette valeur doit être utilisée pour tout endroit où le nom du jeu doit être présenté (titre de la fenêtre, écran principal, etc.).
     */
    public static final String GameName = "Draw.io";
    public static final int GamePort = 5555;

    public static final int TimeoutClientSeconds = 10;
    public static final int HeartbeatClientSeconds = 5;

    public static final int TOTAL_ROUND = 1;
    public static final float ROUND_TIME = 90.0f;
    public static final float END_INTERLUDE_TIME = 5.0f;
    public static final float END_GAME_TIME = 10.0f;

    public static final int MAX_POINTS_ROUND = 500;
    public static final int MIN_POINTS_ROUND = 5;

    public static int Tolerance(int length)
    {
        int tolerance = 2;
        if (length <= 3)
        {
            tolerance = 1;
        }
        return tolerance;
    }
}
