package me.cayve.ludorium.utils.locational;

public class Vector2DInt {
	public int x;
	public int y;

	public Vector2DInt(Vector2DInt v) {
		x = v.x;
		y = v.y;
	}

	public Vector2DInt(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Vector2DInt))
			return false;
		Vector2DInt v = (Vector2DInt) o;

		return v.x == x && v.y == y;
	}

	@Override
	public String toString() {
		return "[ " + x + ", " + y + " ]";
	}
}
