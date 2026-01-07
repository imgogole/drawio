package fr.polytech.wid.s7projectskribbl.common;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class CommonUtilities
{
    public static byte[] ImageToBytes(BufferedImage originalImage) throws IOException
    {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        int minSide = Math.min(width, height);

        int x = (width - minSide) / 2;
        int y = (height - minSide) / 2;
        BufferedImage croppedImage = originalImage.getSubimage(x, y, minSide, minSide);

        Image scaledImage = croppedImage.getScaledInstance(256, 256, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(outputImage, "png", baos);

        return baos.toByteArray();
    }

    public static BufferedImage BytesToImage(byte[] imageData) throws IOException
    {
        if (imageData == null || imageData.length == 0)
        {
            return null;
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        BufferedImage image = ImageIO.read(bais);

        if (image == null)
        {
            throw new IOException("Le décodage de l'image a échoué (format non supporté ou données corrompues).");
        }

        return image;
    }
}