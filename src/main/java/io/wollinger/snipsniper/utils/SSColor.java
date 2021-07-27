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

	public String toString() {
		return Utils.formatArgs("SSColor primaryColor: {0} secondaryColor: {1} point1: {2} point2: {3}", primaryColor, secondaryColor, point1, point2);
	}
}
