package me.cayve.ludorium.utils.locational;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class Vector3D implements ConfigurationSerializable {
	public float x;
	public float y;
	public float z;

	public Vector3D(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3D(Vector3D v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Vector3D))
			return false;
		Vector3D v = (Vector3D) o;

		return v.x == x && v.y == y && v.z == z;
	}

	@Override
	public String toString() {
		return "[ " + x + ", " + y + ", " + z + " ]";
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		
		map.put("x", x);
		map.put("y", y);
		map.put("z", z);
		
		return map;
	}
	
	public static Vector3D deserialize(Map<String, Object> map) {
		return new Vector3D((float)((double)map.get("x")), (float)((double)map.get("y")), (float)((double)map.get("z")));
	}
}
