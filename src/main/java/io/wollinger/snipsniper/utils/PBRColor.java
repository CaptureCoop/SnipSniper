package io.wollinger.snipsniper.utils;

import java.awt.Color;

public class PBRColor {
	private Color color;

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
	}
    //Pass by reference color class. Nothing more.
}
