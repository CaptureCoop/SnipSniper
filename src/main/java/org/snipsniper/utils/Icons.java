package org.snipsniper.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import javax.imageio.ImageIO;

import org.snipsniper.LogManager;
import org.snipsniper.SnipSniper;
import org.json.JSONArray;

public class Icons {

	private static final HashMap<String, BufferedImage> images = new HashMap<>();

	public static void loadResources() {
		try {
			JSONArray list = new JSONArray(Utils.loadFileFromJar("img.json"));
			for(int i = 0; i < list.length(); i++) {
				images.put(list.getString(i), ImageIO.read(SnipSniper.class.getResource("/org/snipsniper/resources/img/" + list.getString(i))));
			}
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	public static BufferedImage getImage(String path) {
		if(!images.containsKey(path)) {
			LogManager.log("ICONS", "Could not find image under path " + path + "!", Level.SEVERE);
			return null;
		}
		return images.get(path);
	}
}
