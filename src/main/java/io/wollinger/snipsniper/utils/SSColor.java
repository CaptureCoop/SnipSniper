package io.wollinger.snipsniper.utils;

import io.wollinger.snipsniper.SnipSniper;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.logging.Level;

public class SSColor {
	private Color primaryColor;
	private Color secondaryColor;
	private Vector2Float point1;
	private Vector2Float point2;

	private final ArrayList<ChangeListener> listeners = new ArrayList<>();

	public SSColor() {

	}

	public SSColor(Color color) {
		this.primaryColor = color;
	}

	public SSColor(Color primaryColor, Color secondaryColor) {
		this.primaryColor = primaryColor;
		this.secondaryColor = secondaryColor;
	}

	public SSColor(Color c, int alpha) {
		primaryColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
	}

    public SSColor(int r, int g, int b, int a) {
		primaryColor = new Color(r, g, b, a);
	}

	public Color getPrimaryColor() {
		return primaryColor;
	}

	public Color getSecondaryColor() {
		return secondaryColor;
	}

	public void setPrimaryColor(Color color) {
		primaryColor = color;
		for(ChangeListener listener : listeners) {
			listener.stateChanged(new ChangeEvent(this));
		}
	}

	public void setSecondaryColor(Color color) {
		secondaryColor = color;
		for(ChangeListener listener : listeners) {
			listener.stateChanged(new ChangeEvent(this));
		}
	}

	public void setPoint1(Vector2Float point) {
		point1 = new Vector2Float(point);
		point1.limit(0f, 1f);
	}

	public void setPoint2(Vector2Float point) {
		point2 = new Vector2Float(point);
		point2.limit(0f, 1f);
	}

	public Vector2Float getPoint1() {
		return point1;
	}

	public Vector2Float getPoint2() {
		return point2;
	}

	public GradientPaint getGradientPaint(int width, int height) {
		if(secondaryColor == null || point1 == null || point2 == null) {
			LogManager.log("SSColor", "A variable wasnt set as needed. Status: " + this, Level.SEVERE);
			SnipSniper.exit(false);
		}

		Vector2Int point1int = new Vector2Int(point1.getX() * width, point1.getY() * height);
		Vector2Int point2int = new Vector2Int(point2.getX() * width, point2.getY() * height);
		return new GradientPaint(point1int.getX(), point1int.getY(), primaryColor, point2int.getX(), point2int.getY(), secondaryColor);
	}

	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}

	public String toSaveString() {
		String string = Utils.rgb2hex(primaryColor);
		if(primaryColor.getAlpha() != 255)
			string += "_a" + primaryColor.getAlpha();

		if(point1 != null)
			string += "_x" + point1.getX() + "_y" + point1.getY();

		if(secondaryColor != null || point2 != null)
			string += "___";

		if(secondaryColor != null) {
			string += Utils.rgb2hex(secondaryColor);
			if(secondaryColor.getAlpha() != 255)
				string += "_a" + secondaryColor.getAlpha();
		}

		if (point2 != null)
			string += "_x" + point2.getX() + "_y" + point2.getY();
		return string;
	}

	public static SSColor fromSaveString(String string) {
		SSColor newColor = new SSColor();
		int index = 0;
		for(String part : string.split("___")) {
			int alpha = -1;
			Color color = null;

			float defaultPos = 0;
			if(index != 0) defaultPos = 1;
			Vector2Float pos = new Vector2Float(defaultPos, defaultPos);

			for(String str : part.split("_")) {
				switch(str.charAt(0)) {
					case '#': color = Utils.hex2rgb(str); break;
					case 'a': alpha = Integer.parseInt(str.substring(1)); break;
					case 'x': pos.setX(Float.parseFloat(str.substring(1))); break;
					case 'y': pos.setY(Float.parseFloat(str.substring(1))); break;
				}

				if(alpha == -1 && color != null)
					alpha = color.getAlpha();

				if(color != null) {
					if(index == 0)
						newColor.primaryColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
					else if(index == 1)
						newColor.secondaryColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
				}


				if(index == 0) newColor.point1 = pos;
				else if(index == 1) newColor.point2 = pos;
			}
			index++;
		}
		return newColor;
	}

	public boolean isValidGradient() {
		return secondaryColor != null;
	}

	public String toString() {
		return Utils.formatArgs("SSColor primaryColor: {0} secondaryColor: {1} point1: {2} point2: {3}", primaryColor, secondaryColor, point1, point2);
	}
}
