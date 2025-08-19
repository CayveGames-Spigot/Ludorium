package me.cayve.ludorium.utils.locational;

import org.joml.Vector2i;

public enum eDirection {
	N(	new Vector2i(0, -1)), 
	NE(	new Vector2i(1, -1)), 
	E(	new Vector2i(1, 0)), 
	SE(	new Vector2i(1, 1)), 
	S(	new Vector2i(0, 1)), 
	SW(	new Vector2i(-1, 1)), 
	W(	new Vector2i(-1, 0)), 
	NW(	new Vector2i(-1, -1));
	
	private final Vector2i vector;
	private eDirection(final Vector2i vector) { this.vector = vector; }
	public boolean isCardinal() { return this.ordinal() % 2 == 0; }
	public boolean isQuadrantal() { return !this.isCardinal(); }
	
	/**
	 * North West = (-1, -1)
	 * South East = (1, 1)
	 * etc.
	 * (In-line with Minecraft directions)
	 * @return
	 */
	public Vector2i getVector() { return this.vector; }
	
	public static eDirection fromVector(Vector2i vector) {
		for (eDirection value : eDirection.values())
			if (value.getVector().equals(vector))
				return value;
		
		throw new IllegalArgumentException("No direction corresponds to vector: " + vector);
	}
	
	public static eDirection opposite(eDirection direction) {
		Vector2i vector = direction.getVector();
		vector.x *= -1;
		vector.y *= -1;
		
		return fromVector(vector);
	}
}
