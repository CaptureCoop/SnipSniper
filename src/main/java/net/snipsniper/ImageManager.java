package net.snipsniper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.*;

import org.json.JSONArray;
import net.snipsniper.config.ConfigHelper;
import net.snipsniper.utils.FileUtils;
import org.capturecoop.cclogger.LogLevel;

public class ImageManager {

	private static final HashMap<String, BufferedImage> images = new HashMap<>();
	private static final HashMap<String, Image> animatedImages = new HashMap<>();
	private static String[] filenameList;

	private ImageManager() { }

	public static void loadResources() {
		LogManager.log("Loading images...", LogLevel.INFO);
		try {
			JSONArray list = new JSONArray(FileUtils.loadFileFromJar("img.json"));
			for(int i = 0; i < list.length(); i++) {
				if(!list.getString(i).endsWith(".gif")) {
					URL url = SnipSniper.class.getResource("/net/snipsniper/resources/img/" + list.getString(i));
					if (url != null) {
						images.put(list.getString(i), ImageIO.read(url));
					} else {
						LogManager.log("Could not load image %c. This should not happen. Exiting...", LogLevel.ERROR, list.getString(i));
						SnipSniper.exit(false);
					}
				} else {
					URL url = ImageManager.class.getResource("/net/snipsniper/resources/img/" + list.getString(i));
					if(url != null) {
						Image img = new ImageIcon(url).getImage();
						animatedImages.put(list.getString(i), img);
					} else {
						LogManager.log("Could not load image %c. This should not happen. Exiting...", LogLevel.ERROR, list.getString(i));
						SnipSniper.exit(false);
					}
				}
			}
		} catch (IOException ioException) {
			LogManager.log("Could not load resources. Message:", LogLevel.ERROR);
			LogManager.logStacktrace(ioException, LogLevel.ERROR);
		}
		LogManager.log("Done!", LogLevel.INFO);
	}

	public static String[] getListAsString() {
		if(filenameList == null) {
			JSONArray list = new JSONArray(FileUtils.loadFileFromJar("img.json"));
			filenameList = new String[list.length()];
			for (int i = 0; i < list.length(); i++) {
				filenameList[i] = list.getString(i);
			}
		}
		return filenameList;
	}

	public static BufferedImage getImage(String path) {
		if(!images.containsKey(path)) {
			LogManager.log("Could not find image under path " + path + "!", LogLevel.ERROR, false);
			return images.get("missing.png");
		}
		return images.get(path);
	}

	public static Image getAnimatedImage(String path) {
		if(!animatedImages.containsKey(path)) {
			LogManager.log("Could not find image under path " + path + "!", LogLevel.ERROR, false);
			return images.get("missing.png");
		}
		return animatedImages.get(path);
	}

	public static boolean hasImage(String path) {
		if(animatedImages.containsKey(path))
			return true;
		else return images.containsKey(path);
	}

	public static BufferedImage getCodePreview() {
		switch(SnipSniper.getConfig().getString(ConfigHelper.MAIN.theme)) {
			case "light": return ImageManager.getImage("preview/code_light.png");
			case "dark": return ImageManager.getImage("preview/code_dark.png");
		}
		return null;
	}
}
