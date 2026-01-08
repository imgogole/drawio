package fr.polytech.wid.s7projectskribbl.common;

public enum CommandCode
{
    PING(0),
    REQUEST_PLAYER_INFO(1),
    SERVER_MESSAGE(2),
    READY(3),
    UPDATE_CLIENT_IMAGE(4),
    ENTER_WAITING_ROOM(5);

    private final int code;

    public static CommandCode From(int code)
    {
        for (CommandCode c : values())
        {
            if (c.code == code) return c;
        }
        throw new IllegalArgumentException();
    }

    CommandCode(int code)
    {
        this.code = code;
    }

    public int Code()
    {
        return code;
    }
}
