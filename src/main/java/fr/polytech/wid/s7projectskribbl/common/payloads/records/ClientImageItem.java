package fr.polytech.wid.s7projectskribbl.common.payloads.records;

import java.awt.image.BufferedImage;

public record ClientImageItem(int id, String username, BufferedImage image, boolean ready)
{
}
