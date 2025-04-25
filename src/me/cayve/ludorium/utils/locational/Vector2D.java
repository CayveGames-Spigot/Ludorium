package me.cayve.ludorium.utils.locational;

public class Vector2D {
	public float x;
	public float y;

	public Vector2D(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Vector2D(Vector2D v) {
		x = v.x;
		y = v.y;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Vector3D))
			return false;
		Vector3D v = (Vector3D) o;

		return v.x == x && v.y == y;
	}

	@Override
	public String toString() {
		return "[ " + x + ", " + y + " ]";
	}
}
