package fr.polytech.wid.s7projectskribbl.client.actions;

import fr.polytech.wid.s7projectskribbl.client.controller.JoinRoomController;
import fr.polytech.wid.s7projectskribbl.client.network.ClientHandler;
import fr.polytech.wid.s7projectskribbl.common.CommandCode;
import fr.polytech.wid.s7projectskribbl.common.CommonUtilities;
import fr.polytech.wid.s7projectskribbl.common.payloads.PlayerInfoPayload;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CPlayerInfoAction implements ClientAction
{
    @Override
    public void Execute(byte[] data)
    {
        // Server is requesting the client username and the client avatar.
        String username = JoinRoomController.Singleton().GetUsername();
        byte[] image = new byte[0];

        try
        {
            File imgFile = JoinRoomController.Singleton().GetAvatarFile();
            if (imgFile != null && imgFile.exists())
            {
                BufferedImage bufferedImage = ImageIO.read(imgFile);
                image = CommonUtilities.ImageToBytes(bufferedImage);
            }
        }
        catch (IOException e)
        {
            image = new byte[0];
        }

        PlayerInfoPayload payload = new PlayerInfoPayload(username, image);
        ClientHandler.Singleton().Out().SendCommand(CommandCode.REQUEST_PLAYER_INFO, payload);
    }
}
