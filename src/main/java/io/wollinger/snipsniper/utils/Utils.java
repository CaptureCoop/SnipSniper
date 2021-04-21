package io.wollinger.snipsniper.utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class Utils {
	
	public static boolean isInteger(String string) {
	    try {
	        Integer.valueOf(string);
	        return true;
	    } catch (NumberFormatException e) {
	        return false;
	    }
	}

	public static void printArgs(PrintStream out, final String message, final Object... args) {
		out.println(formatArgs(message, args));
	}

	public static String formatArgs(final String message, final Object ...args) {
		final int size = args.length;
		String newMessage = message;
		for(int i = 0; i < size; i++) {
			final String id = "\\{" + i + "}";
			newMessage = newMessage.replaceAll(id, args[i].toString());
		}
		return newMessage;
	}

	public static String rgb2hex(Color color) {
		return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
	}
	
	public static BufferedImage resizeImage(BufferedImage original, int width, int height) {
		BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = newImage.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(original, 0,0,width, height, null);
		g.dispose();
		return newImage;
	}
	
	public static Color hex2rgb(String colorStr) {
	    return new Color(Integer.valueOf(colorStr.substring(1, 3), 16), Integer.valueOf( colorStr.substring(3, 5), 16), Integer.valueOf(colorStr.substring(5, 7), 16));
	}

	public static synchronized BufferedImage copyImage(BufferedImage source){
	    BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
	    Graphics g = b.getGraphics();
	    g.drawImage(source, 0, 0, source.getWidth(), source.getHeight(), null);
	    return b;
	}

	public static String loadFileFromJar(String file) throws IOException {
		StringBuilder content = new StringBuilder();
		InputStream inputStream = ClassLoader.getSystemResourceAsStream(file);
		if(inputStream == null)
			throw new FileNotFoundException(Utils.formatArgs("Could not load file {0} from jar!", file));
		InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
		BufferedReader in = new BufferedReader(streamReader);

		for (String line; (line = in.readLine()) != null;)
			content.append(line);

		inputStream.close();
		streamReader.close();
		return content.toString();
	}
}
