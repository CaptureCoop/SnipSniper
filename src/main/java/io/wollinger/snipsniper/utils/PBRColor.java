package io.wollinger.snipsniper.utils;

import java.awt.Color;

public class PBRColor {
	public Color c;
	public PBRColor(Color _c) {
		c = _c;
	}
	//TODO: Support HSV
    public PBRColor(int r, int g, int b, int a) {
    	c = new Color(r, g, b, a);
	}
    //Pass by reference color class. Nothing more.
}
