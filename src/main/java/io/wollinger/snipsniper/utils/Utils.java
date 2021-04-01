package io.wollinger.snipsniper.utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.io.PrintStream;

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
		final int size = args.length;
		String newMessage = message;
		for(int i = 0; i < size; i++) {
			final String id = "\\{" + i + "\\}";
			newMessage = newMessage.replaceAll(id, args[i].toString());
		}
		out.println(newMessage);
	}

	public static String hsvToRgb(float hue, float saturation, float value) {

		int h = (int)(hue * 6);
		float f = hue * 6 - h;
		float p = value * (1 - saturation);
		float q = value * (1 - f * saturation);
		float t = value * (1 - (1 - f) * saturation);

		switch (h) {
			case 0: return rgbToString(value, t, p);
			case 1: return rgbToString(q, value, p);
			case 2: return rgbToString(p, value, t);
			case 3: return rgbToString(p, q, value);
			case 4: return rgbToString(t, p, value);
			case 5: return rgbToString(value, p, q);
			default: throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + hue + ", " + saturation + ", " + value);
		}
	}

	public static String rgbToString(float r, float g, float b) {
		String rs = Integer.toHexString((int)(r * 256));
		String gs = Integer.toHexString((int)(g * 256));
		String bs = Integer.toHexString((int)(b * 256));
		return rs + gs + bs;
	}
	public static String rgb2hex(Color _color) {
		return String.format("#%02x%02x%02x", _color.getRed(), _color.getGreen(), _color.getBlue()); 
	}
	
	public static BufferedImage resizeImage(BufferedImage _original, int _width, int _height) {
		BufferedImage newImage = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = newImage.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(_original, 0,0,_width, _height, null);
		g.dispose();
		return newImage;
	}
	
	public static Color hex2rgb(String colorStr) {
	    return new Color(Integer.valueOf(colorStr.substring(1, 3), 16), Integer.valueOf( colorStr.substring(3, 5), 16), Integer.valueOf(colorStr.substring(5, 7), 16));
	}

	public static BufferedImage copyImage(BufferedImage source){
	    BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
	    Graphics g = b.getGraphics();
	    g.drawImage(source, 0, 0, source.getWidth(), source.getHeight(), null);
	    return b;
	}
}
