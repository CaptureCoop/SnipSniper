package org.snipsniper.utils;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.ArrayList;

public class SSColor {
	private Color primaryColor;
	private Color secondaryColor;
	private Vector2Float point1;
	private Vector2Float point2;
	private boolean isGradient = false;

	private final ArrayList<ChangeListener> listeners = new ArrayList<>();

	public SSColor() {

	}

	public SSColor(Color color) {
		this.primaryColor = color;
	}

	public SSColor(SSColor color) {
		primaryColor = color.primaryColor;
		secondaryColor = color.secondaryColor;
		isGradient = color.isGradient;
		if(color.point1 != null) point1 = new Vector2Float(color.point1);
		if(color.point2 != null) point2 = new Vector2Float(color.point2);
	}

	public SSColor(SSColor color, int alpha) {
		primaryColor = new Color(color.primaryColor.getRed(), color.primaryColor.getGreen(), color.primaryColor.getBlue(), alpha);
		if(color.secondaryColor != null) secondaryColor = new Color(color.secondaryColor.getRed(), color.secondaryColor.getGreen(), color.secondaryColor.getBlue(), alpha);
		isGradient = color.isGradient;
		if(color.point1 != null) point1 = new Vector2Float(color.point1);
		if(color.point2 != null) point2 = new Vector2Float(color.point2);
	}

	public SSColor(Color primaryColor, Color secondaryColor, boolean isGradient) {
		this.primaryColor = primaryColor;
		this.secondaryColor = secondaryColor;
		this.isGradient = isGradient;
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

	public void setPrimaryColor(Color color, int alpha) {
		if(color == null) {
			setPrimaryColor(null);
			return;
		}
		setPrimaryColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
	}

	public void setPrimaryColor(Color color) {
		primaryColor = color;
		alertChangeListeners();
	}

	public void setSecondaryColor(Color color, int alpha) {
		if(color == null) {
			setSecondaryColor(null);
			return;
		}
		setSecondaryColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
	}

	public void setSecondaryColor(Color color) {
		secondaryColor = color;
		alertChangeListeners();
	}

	public void setPoint1(Vector2Float point) {
		if(point != null) {
			point1 = new Vector2Float(point);
			point1.limit(0f, 1f);
		} else {
			point1 = null;
		}
		alertChangeListeners();
	}

	public void setPoint2(Vector2Float point) {
		if(point != null) {
			point2 = new Vector2Float(point);
			point2.limit(0f, 1f);
		} else {
			point2 = null;
		}
		alertChangeListeners();
	}

	public Vector2Float getPoint1() {
		return point1;
	}

	public Vector2Float getPoint2() {
		return point2;
	}

	public Paint getGradientPaint(int width, int height, int posX, int posY) {
		if(!isGradient) {
			return primaryColor;
		}

		if(secondaryColor == null)
			secondaryColor = primaryColor;

		if(point1 == null)
			point1 = new Vector2Float(0f, 0f);
		if(point2 == null)
			point2 = new Vector2Float(1f, 1f);

		Vector2Int point1int = new Vector2Int(point1.getX() * width, point1.getY() * height);
		Vector2Int point2int = new Vector2Int(point2.getX() * width, point2.getY() * height);
		return new GradientPaint(point1int.getX() + posX, point1int.getY() + posY, primaryColor, point2int.getX() + posX, point2int.getY() + posY, secondaryColor);
	}

	public Paint getGradientPaint(int width, int height) {
		return getGradientPaint(width, height, 0, 0);
	}

	private void alertChangeListeners() {
		for(ChangeListener listener : listeners) {
			listener.stateChanged(new ChangeEvent(this));
		}
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

		string += "_G" + isGradient();

		if(secondaryColor != null) {
			string += "___" + Utils.rgb2hex(secondaryColor);
			if(secondaryColor.getAlpha() != 255)
				string += "_a" + secondaryColor.getAlpha();
			if (point2 != null)
				string += "_x" + point2.getX() + "_y" + point2.getY();
		}
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
					case 'G': newColor.isGradient = Boolean.parseBoolean(str.substring(1)); break;
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

	public void loadFromSSColor(SSColor otherColor) {
		primaryColor = otherColor.primaryColor;
		secondaryColor = otherColor.secondaryColor;
		point1 = otherColor.point1;
		point2 = otherColor.point2;
		isGradient = otherColor.isGradient;
		alertChangeListeners();
	}

	public void setIsGradient(boolean bool) {
		isGradient = bool;
	}

	public boolean isGradient() {
		return isGradient;
	}

	public boolean isValidGradient() {
		return secondaryColor != null;
	}

	public String toString() {
		return Utils.formatArgs("SSColor primaryColor: %c secondaryColor: %c point1: %c point2: %c isGradient: %c", primaryColor, secondaryColor, point1, point2, isGradient);
	}
}
