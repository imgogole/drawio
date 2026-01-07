package fr.polytech.wid.s7projectskribbl.server.actions;

import fr.polytech.wid.s7projectskribbl.common.CommandCode;
import fr.polytech.wid.s7projectskribbl.common.CommonUtilities;
import fr.polytech.wid.s7projectskribbl.common.payloads.PlayerInfoPayload;
import fr.polytech.wid.s7projectskribbl.server.PlayerHandler;
import fr.polytech.wid.s7projectskribbl.server.actions.ServerAction;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Reception des informations du joueur
 */
public class SPlayerInfoRequestAction implements ServerAction
{
    @Override
    public void Execute(PlayerHandler player, byte[] data)
    {
        PlayerInfoPayload payload = new PlayerInfoPayload();
        payload.Parse(data);

        BufferedImage bufferedImage;
        try
        {
            bufferedImage = CommonUtilities.BytesToImage(payload.AvatarPng());
        }
        catch (IOException e)
        {
            bufferedImage = null;
        }

        player.SetUsername(payload.Username());
        player.SetAvatar(bufferedImage);

        // Update to everyone
        player.Master().UpdateClientImages();
        player.Out().SendCommand(CommandCode.ENTER_WAITING_ROOM, null);
    }
}
