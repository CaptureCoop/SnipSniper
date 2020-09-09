package snipsniper;

import java.awt.Point;

public class Vector2Int {

	public int x = 0;
	public int y = 0;
	
	public Vector2Int() {
		x = 0;
		y = 0;
	}
	
	public Vector2Int(int _x, int _y) {
		x = _x;
		y = _y;
	}
	
	public Vector2Int(Vector2Int _vec2int) {
		x = _vec2int.x;
		y = _vec2int.y;
	}
	
	public Vector2Int(Point _point) {
		x = (int) _point.getX();
		y = (int) _point.getY();
	}

	public Vector2Int(double _x, double _y) {
		x = (int) _x;
		y = (int) _y;
	}
}
