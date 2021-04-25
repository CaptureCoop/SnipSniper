package io.wollinger.snipsniper.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

import io.wollinger.snipsniper.SnipSniper;

public class Icons {
	
	public static BufferedImage icon;
	public static BufferedImage icon_taskbar;
	public static BufferedImage[] icons;
	public static BufferedImage[] alt_icons;
	
	public static void loadResources() {
		try {
			icon = ImageIO.read(SnipSniper.class.getResource("/res/icon.png"));
			icon_taskbar = ImageIO.read(SnipSniper.class.getResource("/res/SnSn.png"));
			icons = new BufferedImage[8];
			alt_icons = new BufferedImage[8];
			for(int i = 0; i < 8; i++) {
				icons[i] = ImageIO.read(SnipSniper.class.getResource("/res/icon" + i + ".png"));
				alt_icons[i] = ImageIO.read(SnipSniper.class.getResource("/res/alt_icon" + i + ".png"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
