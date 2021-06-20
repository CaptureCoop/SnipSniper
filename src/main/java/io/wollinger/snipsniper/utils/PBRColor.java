package io.wollinger.snipsniper.utils;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.util.ArrayList;

//Pass by reference color class. Nothing more.
public class PBRColor {
	private Color color;
	private final ArrayList<ChangeListener> listeners = new ArrayList<>();

	public PBRColor(Color color) {
		this.color = color;
	}

	public PBRColor(Color c, int alpha) {
		color = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
	}

    public PBRColor(int r, int g, int b, int a) {
		color = new Color(r, g, b, a);
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
		for(ChangeListener listener : listeners) {
			listener.stateChanged(new ChangeEvent(color));
		}
	}

	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}
}
