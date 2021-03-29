package io.wollinger.snipsniper.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

import io.wollinger.snipsniper.Main;

public class Icons {
	
	public static BufferedImage icon;
	public static BufferedImage icon_highres;
	public static BufferedImage icon_taskbar;
	public static BufferedImage[] icons;
	
	public static void loadResources() {
		try {
			icon = ImageIO.read(Main.class.getResource("/res/icon.png"));
			icon_taskbar = ImageIO.read(Main.class.getResource("/res/SnSn.png"));
			icon_highres = ImageIO.read(Main.class.getResource("/res/icon_highres.png"));
			icons = new BufferedImage[8];
			for(int i = 0; i < 8; i++)
				icons[i] = ImageIO.read(Main.class.getResource("/res/icon" + i + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
