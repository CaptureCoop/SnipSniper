package io.wollinger.snipsniper.utils;

import java.awt.Color;

public class PBRColor {
	public Color c;
	public PBRColor(Color c) {
		this.c = c;
	}

	public PBRColor(Color c, int alpha) {
		this.c = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
	}

    public PBRColor(int r, int g, int b, int a) {
    	c = new Color(r, g, b, a);
	}
    //Pass by reference color class. Nothing more.
}
