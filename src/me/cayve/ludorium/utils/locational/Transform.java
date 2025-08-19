package me.cayve.ludorium.utils.locational;

import org.bukkit.Location;
import org.bukkit.World;
import org.joml.Vector2f;
import org.joml.Vector3f;

import me.cayve.ludorium.main.LudoriumException;
import me.cayve.ludorium.utils.functionals.Event.Subscriber;
import me.cayve.ludorium.utils.functionals.Event0;

public class Transform {
	
	private Event0 onUpdateEvent = new Event0();
	
	private World world;
	private float x, y, z;
	private float scale = 1;
	private float pitch, yaw;
	
	public Location getLocation() { 
		if (world == null)
			throw new LudoriumException("World not defined.");
		return new Location(world, x, y, z); 
	}
	
	public World getWorld() { return world; }
	public float getX() { return x; }
	public float getY() { return y; }
	public float getZ() { return z; }
	public float getScale() { return scale; }
	public float getPitch() { return pitch; }
	public float getYaw() { return yaw; }
	
	public void setLocation(Location location) {
		world = location.getWorld();
		x = (float) location.getX();
		y = (float) location.getY();
		z = (float) location.getZ();
		
		onUpdateEvent.run();
	}
	
	public void setPosition(Vector3f position) {
		x = position.x;
		y = position.y;
		z = position.z;
		
		onUpdateEvent.run();
	}
	
	public void setRotation(Vector2f rotation) {
		pitch = rotation.x;
		yaw = rotation.y;
		
		onUpdateEvent.run();
	}
	
	public void setPitch(float pitch) {
		this.pitch = pitch;
		
		onUpdateEvent.run();
	}
	
	public void setYaw(float yaw) {
		this.yaw = yaw;
		
		onUpdateEvent.run();
	}
	
	public void setScale(float scale) {
		this.scale = scale;
		
		onUpdateEvent.run();
	}
	
	public void setX(float x) {
		this.x = x;
		
		onUpdateEvent.run();
	}
	
	public void setY(float y) {
		this.y = y;
		
		onUpdateEvent.run();
	}
	
	public void setZ(float z) {
		this.z = z;
		
		onUpdateEvent.run();
	}
	
	public void set(Transform transform) {
		world = transform.world;
		x = transform.x;
		y = transform.y;
		z = transform.z;
		scale = transform.scale;
		pitch = transform.pitch;
		yaw = transform.yaw;
		
		onUpdateEvent.run();
	}
	
	public void add(Transform toOffset) {
		world = toOffset.world;
		x = x + toOffset.x;
		y = y + toOffset.y;
		z = z + toOffset.z;
		scale = scale + toOffset.scale;
		pitch = pitch + toOffset.pitch;
		yaw = yaw + toOffset.yaw;
		
		onUpdateEvent.run();
	}
	
	@Override
	public String toString() {
		return "[ " + x + ", " + y +  ", " + z + " Scale: " + scale + " Pitch: " + pitch + " Yaw: " + yaw + " ]";
	}
	
	public Subscriber<Runnable> onUpdated() { return onUpdateEvent.getSubscriber(); }
}