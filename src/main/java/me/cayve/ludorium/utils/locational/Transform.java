package me.cayve.ludorium.utils.locational;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.joml.Vector2f;
import org.joml.Vector3f;

import me.cayve.ludorium.LudoriumException;
import me.cayve.ludorium.utils.events.Event0;
import me.cayve.ludorium.utils.events.Event.Subscriber;

public class Transform implements Cloneable {
	
	private Event0 onUpdateEvent = new Event0();
	
	private Transform offset;
	
	private World world;
	private float x, y, z;
	private float scale = 1;
	private float pitch, yaw;
	
	public Transform() {}
	/**
	 * Creates a true-0 transform (scale is also 0)
	 */
	public Transform(boolean zero) { if (zero) scale = 0; }
	
	public Location getLocation() { 
		if (world == null)
			throw new LudoriumException("World not defined.");
		return new Location(getWorld(), 0,0,0).add(Vector.fromJOML(getPosition())); 
	}
	
	public Vector3f getPosition() { return new Vector3f(getX(), getY(), getZ()); }
	public Vector2f getRotation() { return new Vector2f(getPitch(), getYaw()); }
	
	public World getWorld() { return world; }
	public float getX() { return x + (offset == null ? 0 : offset.getX() * getScale()); }
	public float getY() { return y + (offset == null ? 0 : offset.getY() * getScale()); }
	public float getZ() { return z + (offset == null ? 0 : offset.getZ() * getScale()); }
	public float getScale() { return scale + (offset == null ? 0 : offset.getScale()); }
	public float getPitch() { return pitch + (offset == null ? 0 : offset.getPitch() * getScale()); }
	public float getYaw() { return yaw + (offset == null ? 0 : offset.getYaw() * getScale()); }
	
	public void resetOffsets() {
		offset = new Transform(true);
		
		onUpdateEvent.run();
	}
	
	/**
	 * Sets the true position to include the offsets, and then resets the offsets
	 */
	public Transform applyOffsets() {
		if (offset != null)
			add(offset);
		
		resetOffsets();
		
		onUpdateEvent.run();
		return this;
	}
	
	/**
	 * Sets the positional offset
	 * @param offset
	 * @return
	 */
	public Transform setOffset(Vector3f offset) {
		return setOffset(new Transform(true).setPosition(offset));
	}
	
	/**
	 * Sets the transform offset
	 * @param offset
	 * @return
	 */
	public Transform setOffset(Transform offset) {
		this.offset = offset;
		
		onUpdateEvent.run();
		return this;
	}
	
	public Transform setLocation(Location location) {
		world = location.getWorld();
		x = (float) location.getX();
		y = (float) location.getY();
		z = (float) location.getZ();

		onUpdateEvent.run();
		return this;
	}
	
	public Transform setPosition(Vector3f position) {
		x = position.x;
		y = position.y;
		z = position.z;
		
		onUpdateEvent.run();
		return this;
	}
	
	public Transform setRotation(Vector2f rotation) {
		pitch = rotation.x;
		yaw = rotation.y;
		
		onUpdateEvent.run();
		return this;
	}
	
	public Transform setPitch(float pitch) {
		this.pitch = pitch;
		
		onUpdateEvent.run();
		return this;
	}
	
	public Transform setYaw(float yaw) {
		this.yaw = yaw;
		
		onUpdateEvent.run();
		return this;
	}
	
	/**
	 * Scales the positional offset, if any
	 * @param scale
	 * @return
	 */
	public Transform setScale(float scale) {
		this.scale = scale;
		
		onUpdateEvent.run();
		return this;
	}
	
	public Transform setX(float x) {
		this.x = x;
		
		onUpdateEvent.run();
		return this;
	}
	
	public Transform setY(float y) {
		this.y = y;
		
		onUpdateEvent.run();
		return this;
	}
	
	public Transform setZ(float z) {
		this.z = z;
		
		onUpdateEvent.run();
		return this;
	}
	
	/**
	 * Sets this to match the given transform. This offset also uses set() on the other's offset
	 * @param transform
	 */
	public Transform set(Transform transform) {
		world = transform.world;
		x = transform.x;
		y = transform.y;
		z = transform.z;
		scale = transform.scale;
		pitch = transform.pitch;
		yaw = transform.yaw;
		
		if (transform.offset != null)
		{
			if (offset == null)
				offset = new Transform(true);
			offset.set(transform.offset);
		}
		
		onUpdateEvent.run();
		return this;
	}
	
	/**
	 * Adds a transform to this. The offset of the other is baked into its position and is included in the addition
	 * @param toOffset
	 */
	public Transform add(Transform toOffset) {
		if (world == null)
			world = toOffset.world;
		
		x = x + toOffset.getX();
		y = y + toOffset.getY();
		z = z + toOffset.getZ();
		scale = scale + toOffset.getScale();
		pitch = pitch + toOffset.getPitch();
		yaw = yaw + toOffset.getYaw();
		
		onUpdateEvent.run();
		return this;
	}
	
	@Override
	public String toString() {
		return "[ " + x + ", " + y +  ", " + z + " Scale: " + scale + " Pitch: " + pitch + " Yaw: " + yaw + " ]";
	}
	
	@Override
	public Transform clone() {
		return new Transform().set(this);
	}
	
	public Subscriber<Runnable> onUpdated() { return onUpdateEvent.getSubscriber(); }
}