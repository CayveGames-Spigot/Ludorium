package me.cayve.ludorium.utils.locational;

import org.bukkit.Location;
import org.bukkit.World;

import me.cayve.ludorium.main.LudoriumException;

public class Transform {
	
	public World world;
	public float x, y, z;
	public float scale = 1;
	public float pitch, yaw;
	
	public Location getLocation() { 
		if (world == null)
			throw new LudoriumException("World not defined.");
		return new Location(world, x, y, z); 
	}
	
	public void setLocation(Location location) {
		world = location.getWorld();
		x = (float) location.getX();
		y = (float) location.getY();
		z = (float) location.getZ();
	}
	
	public void setPosition(Vector3D position) {
		x = position.x;
		y = position.y;
		z = position.z;
	}
	
	public void setRotation(Vector2D rotation) {
		pitch = rotation.x;
		yaw = rotation.y;
	}
	
	public static Transform relativeTransform(Transform transform, Transform toOffset) {
		Transform newTransform = new Transform();
		
		newTransform.world = transform.world;
		newTransform.x = transform.x + toOffset.x;
		newTransform.y = transform.y + toOffset.y;
		newTransform.z = transform.z + toOffset.z;
		newTransform.scale = transform.scale + toOffset.scale;
		newTransform.pitch = transform.pitch + toOffset.pitch;
		newTransform.yaw = transform.yaw + toOffset.yaw;
		
		return newTransform;
	}
	
	@Override
	public String toString() {
		return "[ " + x + ", " + y +  ", " + z + " Scale: " + scale + " Pitch: " + pitch + " Yaw: " + yaw + " ]";
	}
}