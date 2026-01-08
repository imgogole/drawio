package fr.polytech.wid.s7projectskribbl.common;

public enum CommandCode
{
    HEARTBEAT(0),
    PING(1),
    REQUEST_PLAYER_INFO(2),
    SERVER_MESSAGE(3),
    READY(4),
    UPDATE_CLIENT_IMAGE(5),
    ENTER_WAITING_ROOM(6);

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
