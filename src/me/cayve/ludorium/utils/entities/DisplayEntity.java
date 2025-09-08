package me.cayve.ludorium.utils.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.event.Listener;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import me.cayve.ludorium.utils.functionals.Event.Subscriber;
import me.cayve.ludorium.utils.functionals.Event0;
import me.cayve.ludorium.utils.functionals.Event1;
import me.cayve.ludorium.utils.interfaces.Destroyable;
import me.cayve.ludorium.utils.interfaces.Toggleable;
import me.cayve.ludorium.utils.locational.Transform;

public class DisplayEntity<T extends Display> implements Destroyable, Toggleable, Listener {
	
	public static interface EntityComponent {}
	
	protected T display;
	private String displayID = UUID.randomUUID().toString();
	
	protected Transform originTransform = new Transform(), displayTransform = new Transform();
	private Class<T> type;

	private Map<Class<? extends EntityComponent>, EntityComponent> components = new HashMap<>();
	
	private Event1<DisplayEntity<T>> onDestroyEvent = new Event1<DisplayEntity<T>>();
	private Event0 onEnableEvent = new Event0();
	private Event0 onDisableEvent = new Event0();
	
	protected DisplayEntity() { }
	
	@SafeVarargs
	public DisplayEntity(Class<T> type, Location location, Function<DisplayEntity<T>, EntityComponent>... componentFactory) { 
		construct(type, location, componentFactory); 
	}
	@SafeVarargs
	public DisplayEntity(Class<T> type, Location location, String displayID, Function<DisplayEntity<T>, EntityComponent>... componentFactory) { 
		construct(type, location, displayID, componentFactory); 
	}
	
	@SafeVarargs
	protected final void construct(Class<T> type, Location location, Function<DisplayEntity<T>, EntityComponent>... componentFactory) { 
		construct(type, location, null, componentFactory); 
	}

	@SafeVarargs
	protected final void construct(Class<T> type, Location location, String displayID, Function<DisplayEntity<T>, EntityComponent>... componentFactory) {
		if (type == null) return;
		
		this.type = type;
		
		setPivot(new Vector3f(0, -0.5f, 0));
		
		this.originTransform.setLocation(location);
		this.displayTransform.setLocation(location);
		
		if (displayID != null)
			this.displayID = displayID;
		
		for (Function<DisplayEntity<T>, EntityComponent> factory : componentFactory)
		{
			EntityComponent component = factory.apply(this);
			
			if (!this.components.containsKey(component.getClass()))
				this.components.put(component.getClass(), component);
		}
		
		displayTransform.onUpdated().subscribe(() -> teleportTo(displayTransform));
		
		enable();
	}
	
	public String getID() { return displayID; }
	
	/**
	 * Returns the mutable location of the origin point
	 * @return
	 */
	public Transform getOriginTransform() { return originTransform; }
	
	/**
	 * Returns the mutable location of the current display point
	 * @return
	 */
	public Transform getDisplayTransform() { return displayTransform; }
	
	public void teleportToOrigin() { displayTransform.set(originTransform); }
	
	/**
	 * Resets the origin point to the current display location
	 */
	public void saveOrigin() { originTransform.set(displayTransform); }
	
	/**
	 * Sets the pivot point of this display. Default is (0, -0.5f, 0) or the bottom center
	 * @param pivot
	 */
	public void setPivot(Vector3f pivot) { 
		pivot.mul(-1); //Flip it because it's pivot -> offset
		originTransform.setOffset(pivot);
		displayTransform.setOffset(pivot);
	}

	private void teleportTo(Transform transform) {
		if (!isEnabled()) return;

		display.teleport(transform.getLocation());
		display.setRotation(transform.getYaw(), transform.getPitch());
		
		Transformation displayTransformation = display.getTransformation();
		displayTransformation.getScale().set(transform.getScale());
		display.setTransformation(displayTransformation);
	}
	
	/**
	 * @return The entity's display
	 */
	public T get() { return display; }
	
	public <C extends EntityComponent> C getComponent(Class<C> type) {
		if (!components.containsKey(type))
			return null;
		return type.cast(components.get(type));
	}

	private <C> void forEachComponentWith(Class<C> type, Consumer<C> action) {
		for (EntityComponent component : components.values()) {
			if (!type.isInstance(component)) continue;
			
			action.accept(type.cast(component));
		}
	}
	
	/**
	 * Spawns this entity's display
	 */
	@Override
	public void enable() {
		disable();
			
		display = LudoriumEntity.spawn(originTransform.getLocation(), type);
		
		forEachComponentWith(Toggleable.class, x -> x.enable());
		
		onEnableEvent.run();
	}
	
	/**
	 * Removes this entity's display
	 */
	@Override
	public void disable() {
		if (!isEnabled())
			return;
		
		LudoriumEntity.remove(display);
		display = null;
		
		forEachComponentWith(Toggleable.class, x -> x.disable());
		
		onDisableEvent.run();
	}

	/**
	 * Destroys and cleans up this entity
	 */
	@Override
	public void destroy() {
		disable();
		
		forEachComponentWith(Destroyable.class, x -> x.destroy());
		
		onDestroyEvent.accept(this);
	}
	
	@Override public boolean isEnabled() { return display != null; }
	
	@Override public Subscriber<Runnable> onDisabled() { return onDisableEvent.getSubscriber(); }
	@Override public Subscriber<Runnable> onEnabled() { return onEnableEvent.getSubscriber(); }
	@Override public Subscriber<Consumer<DisplayEntity<T>>> onDestroyed() { return onDestroyEvent.getSubscriber(); }
}
