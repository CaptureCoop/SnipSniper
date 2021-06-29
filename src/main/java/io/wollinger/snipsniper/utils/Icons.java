package io.wollinger.snipsniper.utils;

import java.awt.image.BufferedImage;
import java.util.logging.Level;
import javax.imageio.ImageIO;

import io.wollinger.snipsniper.SnipSniper;

public class Icons {
	
	public static BufferedImage icon;
	public static BufferedImage icon_taskbar;
	public static BufferedImage icon_editor;
	public static BufferedImage icon_viewer;
	public static BufferedImage icon_config;
	public static BufferedImage icon_console;
	public static BufferedImage[] icons;
	public static BufferedImage[] alt_icons;

	public static BufferedImage stamp_preview_dark;
	public static BufferedImage stamp_preview_light;

	public static void loadResources() {
		try {
			icon = ImageIO.read(SnipSniper.class.getResource("/res/icon.png"));
			icon_taskbar = ImageIO.read(SnipSniper.class.getResource("/res/SnSn.png"));
			icon_editor = ImageIO.read(SnipSniper.class.getResource("/res/SnSnEd.png"));
			icon_viewer = ImageIO.read(SnipSniper.class.getResource("/res/SnSnVi.png"));
			icon_config = ImageIO.read(SnipSniper.class.getResource("/res/SnSnCo.png"));
			icon_console = ImageIO.read(SnipSniper.class.getResource("/res/SnSnCs.png"));

			stamp_preview_dark = ImageIO.read(SnipSniper.class.getResource("/res/stamp_preview_dark.png"));
			stamp_preview_light = ImageIO.read(SnipSniper.class.getResource("/res/stamp_preview_light.png"));

			icons = new BufferedImage[8];
			alt_icons = new BufferedImage[8];
			for(int i = 0; i < 8; i++) {
				icons[i] = ImageIO.read(SnipSniper.class.getResource("/res/icon" + i + ".png"));
				alt_icons[i] = ImageIO.read(SnipSniper.class.getResource("/res/alt_icon" + i + ".png"));
			}
		} catch (Exception e) {
			LogManager.log("ICON", "There was an error loading the icons! Message: " + e.getMessage(), Level.SEVERE);
			SnipSniper.exit(false);
		}
	}
}
