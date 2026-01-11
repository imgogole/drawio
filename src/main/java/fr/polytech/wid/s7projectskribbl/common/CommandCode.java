package fr.polytech.wid.s7projectskribbl.common;

public enum CommandCode
{
    HEARTBEAT(0),
    PING(1),
    ID_ATTRIBUTION(2),
    REQUEST_PLAYER_INFO(3),
    SERVER_MESSAGE(4),
    READY(5),
    UPDATE_CLIENT_IMAGE(6),
    ENTER_WAITING_ROOM(7),
    ENTER_GAME(8),
    CHAT_MESSAGE_SENT(9),
    DRAW_ACTION(10),
    NT_DECISION(11),
    NT_BEGIN(12),
    END_ROUND_RESUME(13),
    FOUND_WORD(14)
    ;

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
