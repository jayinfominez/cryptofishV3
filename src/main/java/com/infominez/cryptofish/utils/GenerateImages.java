package com.infominez.cryptofish.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class GenerateImages {
    public static void generateNumberImages(int start, int end) throws Exception {
        for (int i = start; i <= end; i++) {
            BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 100, 100);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.PLAIN, 48));
            g.drawString(String.valueOf(i), 25, 65);
            g.dispose();
            ImageIO.write(image, "png", new File("images/"+i + ".png"));
        }
    }


}
