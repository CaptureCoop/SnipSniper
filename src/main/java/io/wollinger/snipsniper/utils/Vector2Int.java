package io.wollinger.snipsniper.utils;

import java.awt.Point;

public class Vector2Int {
	public int x;
	public int y;
	
	public Vector2Int() {
		x = 0;
		y = 0;
	}
	
	public Vector2Int(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector2Int(Vector2Int vec2int) {
		x = vec2int.x;
		y = vec2int.y;
	}
	
	public Vector2Int(Point point) {
		x = (int) point.getX();
		y = (int) point.getY();
	}

	public Vector2Int(double x, double y) {
		this.x = (int) x;
		this.y = (int) y;
	}

	public Point toPoint() {
		return new Point(x,y);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public String toString() {
		return "Vector2Int(" + x + "/" + y + ")";
	}
}
