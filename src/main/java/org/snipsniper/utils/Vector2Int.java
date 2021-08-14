package org.snipsniper.utils;

import java.awt.Point;

public class Vector2Int {
	private int x;
	private int y;
	
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

	public Vector2Int add(Vector2Int vec2int) {
		this.x += vec2int.getX();
		this.y += vec2int.getY();
		return this;
	}

	public Vector2Int sub(Vector2Int vec2int) {
		this.x -= vec2int.getX();
		this.y -= vec2int.getY();
		return this;
	}

	public static Vector2Int add(Vector2Int vec2int1, Vector2Int vec2int2) {
		return new Vector2Int(vec2int1).add(vec2int2);
	}

	public static Vector2Int sub(Vector2Int vec2int1, Vector2Int vec2int2) {
		return new Vector2Int(vec2int1).sub(vec2int2);
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getX() {
		return x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getY() {
		return y;
	}

	public Point toPoint() {
		return new Point(x,y);
	}

	public String toString() {
		return "Vector2Int(" + x + "/" + y + ")";
	}
}
