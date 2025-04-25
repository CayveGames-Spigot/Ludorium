package me.cayve.ludorium.utils.entities;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;

import me.cayve.ludorium.utils.animations.Animator;
import me.cayve.ludorium.utils.locational.Transform;
import me.cayve.ludorium.utils.locational.Vector2D;
import me.cayve.ludorium.utils.locational.Vector3D;

public class DisplayEntity<T extends Display> {
	
	protected T display;
	
	protected Transform transform;
	private Class<T> type;

	private Animator animator;
	private boolean isSpawned; //For animation consistency
	private ArrayList<Consumer<DisplayEntity<T>>> onAnimatorCompleteEvent = new ArrayList<>();
	private ArrayList<Consumer<DisplayEntity<T>>> onDestroyEvent = new ArrayList<>();
	
	public DisplayEntity(Class<T> type, Location location) {
		this.type = type;
		this.transform = new Transform();
		this.transform.setLocation(location);
		
		animator = new Animator(this::onAnimatorUpdate, this::onAnimatorComplete);
	}
	
	/**
	 * Moves this entity to the location
	 * @param location
	 */
	public void move(Location location) {
		animator.cancelAnimations();
		transform.setLocation(location);
		
		displayTransform(transform);
	}
	
	/**
	 * Moves this entity to the position
	 * @param position
	 */
	public void move(Vector3D position) {
		transform.setPosition(position);
		
		move(transform.getLocation());
	}
	
	/**
	 * Alters this entity's scale
	 * @param scale
	 */
	public void scale(float scale) {
		animator.cancelAnimations();
		transform.scale = scale;
		
		displayTransform(transform);
	}
	
	/**
	 * Alters this entity's rotation
	 * @param rotation the pitch (x) and yaw (y) of the rotation
	 */
	public void rotate(Vector2D rotation) {
		animator.cancelAnimations();
		transform.pitch = rotation.x;
		transform.yaw = rotation.y;
		
		displayTransform(transform);
	}
	
	/**
	 * Alters this entity's entire transform
	 * @param transform
	 */
	public void transform(Transform transform) {
		scale(transform.scale);
		rotate(new Vector2D(transform.pitch, transform.yaw));
		
		move(transform.getLocation());
	}
	
	/**
	 * Transforms the display (rather than the entity origin)
	 * @param transform
	 */
	public void displayTransform(Transform transform) {
		if (display == null) return;

		display.teleport(transform.getLocation());
		display.setRotation(transform.yaw, transform.pitch);
		
		Transformation displayTransformation = display.getTransformation();
		displayTransformation.getScale().set(transform.scale);
		display.setTransformation(displayTransformation);
	}
	
	/**
	 * Spawns this entity's display 
	 * @return
	 */
	public final T spawn() { return spawn(false); }
	
	protected T spawn(boolean rawSpawn) {
		
		//If just the spawning logic should be run alone, then skip the rest
		if (!rawSpawn) {
			remove();
			isSpawned = true;
		}
			
		display = LudoriumEntity.spawn(transform.getLocation(), type);
		
		return display;
	}
	
	/**
	 * @return The entity's display
	 */
	public T get() { return display; }
	
	/**
	 * Removes this entity's display
	 */
	public void remove() {
		isSpawned = false;
		
		animator.cancelAnimations();
		if (display != null)
		{
			LudoriumEntity.remove(display);
			display = null;
		}
	}
	
	/**
	 * Registers a listener for when the animator completes all animations
	 * @param listener
	 */
	public void registerOnAnimatorComplete(Consumer<DisplayEntity<T>> listener) {
		onAnimatorCompleteEvent.add(listener);
	}
	
	public void registerOnDestroy(Consumer<DisplayEntity<T>> listener) {
		onDestroyEvent.add(listener);
	}
	/**
	 * @return The entity's animator
	 */
	public Animator getAnimator() { return animator; }
	
	private void onAnimatorUpdate(Transform offset) {
		if (display == null) //If the display isn't active, temporarily activate it without tagging isSpawned
			spawn(true);
		
		Transform locationOffset = Transform.relativeTransform(this.transform, offset);
		displayTransform(locationOffset);
	}
	
	private void onAnimatorComplete() {
		if (!isSpawned) //If the display isn't supposed to be activated, remove it after animator completes
			remove();
		
		for (Consumer<DisplayEntity<T>> listener : onAnimatorCompleteEvent)
			listener.accept(this);
	}
	
	/**
	 * Destroys and cleans up this entity
	 */
	public void destroy() {
		remove();
		animator.cancelAnimations();
		
		for (Consumer<DisplayEntity<T>> listener : onDestroyEvent)
			listener.accept(this);
	}
}
